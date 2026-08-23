# Async Download Business Retry Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Wire business retry so a `FAIL` download record can re-enter the existing `execute` / `doExecute` path, automatically and from Admin/API, without mixing trigger compensation.

**Architecture:** One CAS `claimRetry` (`FAIL` → `NONE`, `current_retry+1`) in storage; `retrigger` resets the DB trigger for a new attempt; `reexecute` re-queues through the existing handler. `dispatchRetry` runs after the attempt completes (after trigger `exeSuccess` for schedule/MQ). A scanner is the safety net for crash and remote Admin.

**Tech Stack:** Java 8, Spring Boot 2.3.2, JUnit 4 + Mockito, MyBatis XML (server), JdbcTemplate (local), Maven.

**Spec:** `docs/superpowers/specs/2026-08-23-async-retry-design.md`

## Global Constraints

- Retryable record status is `FAIL` only. Never claim `CANCEL`, `SUCCESS`, `INVALID`, `EXECUTING`, `UPLOADING`, or `NONE`.
- Trigger compensation must not increment `current_retry`. Business retry must not use `trigger_count` as its quota.
- `maxServerRetry <= 0` means no business retry. `maxServerRetry = 1` means one retry after first `FAIL`.
- Successful claim never rolls back `current_retry`.
- `dispatchRetry` must swallow exceptions and must not break callback or trigger `exeSuccess` / `exeFail`.
- First-run `DownloadTriggerService.trigger()` stays insert-if-absent.
- `enableRunning` stays `NONE` → `EXECUTING` only. `cancel` stays `NONE` → `CANCEL` only.
- Do not change `handleFileWithRetry` or import-task code.
- Operator for automatic / server API claim is `Constants.SYSTEM_USER` (`"system"`). Admin UI uses `adminProperty.getAdminUsername()`.
- Java 8 only. Add `spring-boot-starter-test` (test scope) to any module that gets new tests and does not already have it.

## File map

| File | Responsibility |
|---|---|
| `easyfile-common/.../request/RetryDownloadRequest.java` | `registerId` body for server/remote retry |
| `easyfile-common/.../request/ListRetryableRequest.java` | scanner query body |
| `easyfile-storage-api/.../DownloadStorageService.java` | `claimRetry` / `listRetryable` / `retry` |
| `easyfile-storage-local/.../AsyncDownloadRecordMapper.java` + `Impl` | claim SQL + list SQL |
| `easyfile-storage-local/.../LocalDownloadStorageServiceImpl.java` | local claim/list |
| `easyfile-server/.../AsyncDownloadRecordMapper.java` + `.xml` | replace unused `updateRetryById` / align list |
| `easyfile-server/.../AsyncDownloadService.java` + `Impl` | server claim/list |
| `easyfile-server/.../DownloadController.java` | HTTP retry + listRetryable |
| `easyfile-server/.../DefaultDownloadStorageServiceImpl.java` | delegate |
| `easyfile-storage-remote/.../EasyFileClient.java` + `HttpEasyFileClientImpl` + `RemoteUrlConstants` + `RemoteDownloadStorageServiceImpl` | remote claim/list, null-safe |
| `easyfile-storage-local/.../DownloadTriggerService.java` + local impl + trigger mapper | `retrigger` |
| `easyfile-core/.../BaseAsyncFileHandler.java` + `AsyncFileHandlerAdapter.java` | `reexecute` + `dispatchRetry` |
| `easyfile-starter/.../DefaultAsyncFileHandler.java` | after `doExecute`, dispatch |
| `easyfile-core/.../DatabaseAsyncFileHandlerAdapter.java` | after `exeSuccess`/`exeFail`, dispatch |
| `easyfile-starter/.../ScheduleTriggerAsyncFileHandler.java` | `reexecute` = retrigger + async `doTrigger` |
| `easyfile-starter/.../MqTriggerAsyncFileHandler.java` | `reexecute` = retrigger + `doSend` (no `afterCommit`) |
| `easyfile-starter/.../properties/EasyFileDownloadRetryProperties.java` | scan defaults |
| `easyfile-starter/.../DownloadRetryAutoConfiguration.java` + `DownloadRetryScanner.java` | scanner bean |
| `easyfile-ui/.../DownloadTaskController.java` + `download-task.html` + `download-task.js` | Admin retry |

---

### Task 1: Storage claim + list (interface + local)

**Files:**
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/request/RetryDownloadRequest.java`
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/request/ListRetryableRequest.java`
- Modify: `easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/download/DownloadStorageService.java`
- Modify: `easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/mapper/AsyncDownloadRecordMapper.java`
- Modify: `easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/mapper/impl/AsyncDownloadRecordMapperImpl.java`
- Modify: `easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/impl/LocalDownloadStorageServiceImpl.java`
- Modify: `easyfile-storage/easyfile-storage-local/pom.xml` (add `spring-boot-starter-test` test scope)
- Test: `easyfile-storage/easyfile-storage-local/src/test/java/com/openquartz/easyfile/storage/local/impl/LocalDownloadStorageServiceImplClaimRetryTest.java`

**Interfaces:**
- Consumes: existing `UploadStatusEnum.FAIL` / `NONE`, local mapper, `Constants` not required here
- Produces:
  - `boolean DownloadStorageService.claimRetry(Long registerId, String operator)`
  - `List<Long> DownloadStorageService.listRetryable(int lookBackHours, int limit, int orphanGraceSeconds)`
  - `boolean DownloadStorageService.retry(Long registerId, String operator)` (calls `claimRetry`)
  - `int AsyncDownloadRecordMapper.claimRetry(Long id, String operator)`
  - `List<Long> AsyncDownloadRecordMapper.selectRetryableRegisterIds(java.util.Date startTime, java.util.Date orphanBefore, int limit)`

- [ ] **Step 1: Add test dependency and the failing test**

In `easyfile-storage-local/pom.xml` inside `<dependencies>`:

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
```

Create the test:

```java
package com.openquartz.easyfile.storage.local.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadRecordMapper;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTaskMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalDownloadStorageServiceImplClaimRetryTest {

    @Mock
    private AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    @Mock
    private AsyncDownloadRecordMapper asyncDownloadRecordMapper;
    @InjectMocks
    private LocalDownloadStorageServiceImpl service;

    @Test
    public void claimRetry_trueWhenMapperUpdatesOneRow() {
        when(asyncDownloadRecordMapper.claimRetry(9L, "op")).thenReturn(1);
        assertTrue(service.claimRetry(9L, "op"));
        assertTrue(service.retry(9L, "op"));
    }

    @Test
    public void claimRetry_falseWhenMapperUpdatesZeroRows() {
        when(asyncDownloadRecordMapper.claimRetry(9L, "op")).thenReturn(0);
        assertFalse(service.claimRetry(9L, "op"));
    }

    @Test
    public void listRetryable_mapsMapperIds() {
        when(asyncDownloadRecordMapper.selectRetryableRegisterIds(any(), any(), eq(50)))
            .thenReturn(Arrays.asList(1L, 2L));
        List<Long> ids = service.listRetryable(24, 50, 30);
        assertEquals(Arrays.asList(1L, 2L), ids);
        verify(asyncDownloadRecordMapper).selectRetryableRegisterIds(any(), any(), eq(50));
    }

    @Test
    public void listRetryable_emptyWhenMapperEmpty() {
        when(asyncDownloadRecordMapper.selectRetryableRegisterIds(any(), any(), eq(10)))
            .thenReturn(Collections.emptyList());
        assertTrue(service.listRetryable(2, 10, 30).isEmpty());
        verify(asyncDownloadTaskMapper, never()).findById(any());
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails**

Run:

```bash
mvn -pl easyfile-storage/easyfile-storage-local -am test -Dtest=LocalDownloadStorageServiceImplClaimRetryTest
```

Expected: compile failure, `claimRetry` not found on `DownloadStorageService` / mapper.

- [ ] **Step 3: Add DTOs, interface methods, mapper SQL, local service**

`RetryDownloadRequest.java`:

```java
package com.openquartz.easyfile.common.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RetryDownloadRequest {

    @NotNull(message = "注册ID不能为空")
    private Long registerId;

    public static RetryDownloadRequest create(Long registerId) {
        RetryDownloadRequest request = new RetryDownloadRequest();
        request.setRegisterId(registerId);
        return request;
    }
}
```

`ListRetryableRequest.java`:

```java
package com.openquartz.easyfile.common.request;

import lombok.Data;

@Data
public class ListRetryableRequest {

    private Integer lookBackHours;
    private Integer limit;
    private Integer orphanGraceSeconds;

    public static ListRetryableRequest create(int lookBackHours, int limit, int orphanGraceSeconds) {
        ListRetryableRequest request = new ListRetryableRequest();
        request.setLookBackHours(lookBackHours);
        request.setLimit(limit);
        request.setOrphanGraceSeconds(orphanGraceSeconds);
        return request;
    }
}
```

Add to `DownloadStorageService` (with `java.util.Collections` import if needed):

```java
    default boolean claimRetry(Long registerId, String operator) {
        return false;
    }

    default java.util.List<Long> listRetryable(int lookBackHours, int limit, int orphanGraceSeconds) {
        return java.util.Collections.emptyList();
    }

    default boolean retry(Long registerId, String operator) {
        return claimRetry(registerId, operator);
    }
```

Mapper interface additions:

```java
    int claimRetry(Long id, String operator);

    java.util.List<Long> selectRetryableRegisterIds(java.util.Date startTime, java.util.Date orphanBefore, int limit);
```

In `AsyncDownloadRecordMapperImpl`, add constants and methods (table via `EasyFileTableGeneratorSupplier.genAsyncDownloadRecordTable()`):

```java
    private static final String CLAIM_RETRY_SQL =
        "update {0} set upload_status = 'none', current_retry = current_retry + 1, execute_process = 0, "
            + "error_msg = '', last_execute_time = now(), update_by = ? "
            + "where id = ? and upload_status = 'fail' and max_server_retry > current_retry";

    private static final String SELECT_RETRYABLE_SQL =
        "select id from {0} where last_execute_time >= :startTime and ("
            + "(upload_status = 'fail' and max_server_retry > current_retry) or "
            + "(upload_status = 'none' and current_retry > 0 and last_execute_time < :orphanBefore)"
            + ") limit :limit";

    @Override
    public int claimRetry(Long id, String operator) {
        String sql = MessageFormat.format(CLAIM_RETRY_SQL,
            EasyFileTableGeneratorSupplier.genAsyncDownloadRecordTable());
        return jdbcTemplate.update(sql, operator, id);
    }

    @Override
    public List<Long> selectRetryableRegisterIds(Date startTime, Date orphanBefore, int limit) {
        String sql = MessageFormat.format(SELECT_RETRYABLE_SQL,
            EasyFileTableGeneratorSupplier.genAsyncDownloadRecordTable());
        Map<String, Object> params = new HashMap<>(4);
        params.put("startTime", startTime);
        params.put("orphanBefore", orphanBefore);
        params.put("limit", limit);
        return new NamedParameterJdbcTemplate(jdbcTemplate)
            .query(sql, params, (rs, rowNum) -> rs.getLong("id"));
    }
```

Add missing imports: `Date`, `HashMap`, `Map`, `NamedParameterJdbcTemplate`.

`LocalDownloadStorageServiceImpl`:

```java
    @Override
    public boolean claimRetry(Long registerId, String operator) {
        return asyncDownloadRecordMapper.claimRetry(registerId, operator) > 0;
    }

    @Override
    public List<Long> listRetryable(int lookBackHours, int limit, int orphanGraceSeconds) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR_OF_DAY, -lookBackHours);
        Date startTime = calendar.getTime();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, -orphanGraceSeconds);
        Date orphanBefore = calendar.getTime();
        List<Long> ids = asyncDownloadRecordMapper.selectRetryableRegisterIds(startTime, orphanBefore, limit);
        return ids == null ? Collections.emptyList() : ids;
    }
```

- [ ] **Step 4: Re-run the test**

```bash
mvn -pl easyfile-storage/easyfile-storage-local -am test -Dtest=LocalDownloadStorageServiceImplClaimRetryTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add easyfile-common/src/main/java/com/openquartz/easyfile/common/request/RetryDownloadRequest.java \
  easyfile-common/src/main/java/com/openquartz/easyfile/common/request/ListRetryableRequest.java \
  easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/download/DownloadStorageService.java \
  easyfile-storage/easyfile-storage-local/pom.xml \
  easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/mapper/AsyncDownloadRecordMapper.java \
  easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/mapper/impl/AsyncDownloadRecordMapperImpl.java \
  easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/impl/LocalDownloadStorageServiceImpl.java \
  easyfile-storage/easyfile-storage-local/src/test/java/com/openquartz/easyfile/storage/local/impl/LocalDownloadStorageServiceImplClaimRetryTest.java
git commit -m "feat(storage): add download claimRetry and listRetryable"
```

---

### Task 2: Server claim + list + HTTP

**Files:**
- Modify: `easyfile-server/pom.xml` (add `spring-boot-starter-test` test scope)
- Modify: `easyfile-server/src/main/java/com/openquartz/easyfile/server/mapper/AsyncDownloadRecordMapper.java`
- Modify: `easyfile-server/src/main/resources/mapper/AsyncDownloadRecordMapper.xml`
- Modify: `easyfile-server/src/main/java/com/openquartz/easyfile/server/service/AsyncDownloadService.java`
- Modify: `easyfile-server/src/main/java/com/openquartz/easyfile/server/service/impl/AsyncDownloadServiceImpl.java`
- Modify: `easyfile-server/src/main/java/com/openquartz/easyfile/server/service/impl/DefaultDownloadStorageServiceImpl.java`
- Modify: `easyfile-server/src/main/java/com/openquartz/easyfile/server/controller/DownloadController.java`
- Test: `easyfile-server/src/test/java/com/openquartz/easyfile/server/service/impl/AsyncDownloadServiceImplClaimRetryTest.java`

**Interfaces:**
- Consumes: `claimRetry` / `listRetryable` signatures from Task 1; `RetryDownloadRequest`; `ListRetryableRequest`; `Constants.SYSTEM_USER`
- Produces:
  - `boolean AsyncDownloadService.claimRetry(Long registerId, String operator)`
  - `List<Long> AsyncDownloadService.listRetryable(int lookBackHours, int limit, int orphanGraceSeconds)`
  - `POST /easyfile/download/retry` → `ResponseResult<Boolean>`
  - `POST /easyfile/download/listRetryable` → `ResponseResult<List<Long>>`
  - `int AsyncDownloadRecordMapper.claimRetry(@Param("id") Long id, @Param("operator") String operator)`
  - `List<Long> AsyncDownloadRecordMapper.selectRetryableRegisterIds(@Param("startTime") Date startTime, @Param("orphanBefore") Date orphanBefore, @Param("limit") Integer limit)`

- [ ] **Step 1: Write the failing service test**

Add test scope `spring-boot-starter-test` to `easyfile-server/pom.xml`.

```java
package com.openquartz.easyfile.server.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.server.config.BizConfig;
import com.openquartz.easyfile.server.mapper.AsyncDownloadRecordMapper;
import com.openquartz.easyfile.server.mapper.AsyncDownloadTaskMapper;
import com.openquartz.easyfile.server.service.NotifyService;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncDownloadServiceImplClaimRetryTest {

    @Mock
    private AsyncDownloadTaskMapper asyncDownloadTaskMapper;
    @Mock
    private AsyncDownloadRecordMapper asyncDownloadRecordMapper;
    @Mock
    private NotifyService notifyService;
    @Mock
    private BizConfig bizConfig;
    @InjectMocks
    private AsyncDownloadServiceImpl asyncDownloadService;

    @Test
    public void claimRetry_trueWhenOneRowUpdated() {
        when(asyncDownloadRecordMapper.claimRetry(3L, "system")).thenReturn(1);
        assertTrue(asyncDownloadService.claimRetry(3L, "system"));
    }

    @Test
    public void claimRetry_falseWhenNoRow() {
        when(asyncDownloadRecordMapper.claimRetry(3L, "system")).thenReturn(0);
        assertFalse(asyncDownloadService.claimRetry(3L, "system"));
    }

    @Test
    public void listRetryable_delegatesToMapper() {
        when(asyncDownloadRecordMapper.selectRetryableRegisterIds(any(), any(), eq(20)))
            .thenReturn(Collections.singletonList(7L));
        assertEquals(Collections.singletonList(7L), asyncDownloadService.listRetryable(24, 20, 30));
        verify(asyncDownloadRecordMapper).selectRetryableRegisterIds(any(), any(), eq(20));
    }
}
```

Add these test imports: `AsyncDownloadTaskMapper`, `NotifyService`, `BizConfig`. `@InjectMocks` uses the four-arg constructor.

- [ ] **Step 2: Run the test and confirm it fails**

```bash
mvn -pl easyfile-server -am test -Dtest=AsyncDownloadServiceImplClaimRetryTest
```

Expected: FAIL, `claimRetry` missing on service/mapper.

- [ ] **Step 3: Implement server mapper, service, facade, controller**

Replace unused mapper methods. In `AsyncDownloadRecordMapper.java` replace `updateRetryById` / keep `selectExportRecordOfRetryable` unused or replace with:

```java
    int claimRetry(@Param("id") Long id, @Param("operator") String operator);

    List<Long> selectRetryableRegisterIds(@Param("startTime") Date startTime,
        @Param("orphanBefore") Date orphanBefore, @Param("limit") Integer limit);
```

Remove `updateRetryById` and `selectExportRecordOfRetryable` from the Java interface and XML (they have no callers).

XML:

```xml
    <update id="claimRetry">
        UPDATE ef_async_download_record
        SET last_execute_time = now(),
            current_retry     = current_retry + 1,
            upload_status     = 'none',
            execute_process   = 0,
            error_msg         = '',
            update_by         = #{operator}
        WHERE id = #{id}
          AND upload_status = 'fail'
          AND max_server_retry > current_retry
    </update>

    <select id="selectRetryableRegisterIds" resultType="long">
        SELECT id
        FROM ef_async_download_record
        WHERE last_execute_time >= #{startTime}
          AND (
            (upload_status = 'fail' AND max_server_retry > current_retry)
            OR (upload_status = 'none' AND current_retry > 0 AND last_execute_time &lt; #{orphanBefore})
          )
        LIMIT #{limit}
    </select>
```

`AsyncDownloadService` additions:

```java
    boolean claimRetry(Long registerId, String operator);

    java.util.List<Long> listRetryable(int lookBackHours, int limit, int orphanGraceSeconds);
```

`AsyncDownloadServiceImpl` (same date math as local):

```java
    @Override
    public boolean claimRetry(Long registerId, String operator) {
        return asyncDownloadRecordMapper.claimRetry(registerId, operator) > 0;
    }

    @Override
    public List<Long> listRetryable(int lookBackHours, int limit, int orphanGraceSeconds) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR_OF_DAY, -lookBackHours);
        Date startTime = calendar.getTime();
        calendar.setTime(now);
        calendar.add(Calendar.SECOND, -orphanGraceSeconds);
        Date orphanBefore = calendar.getTime();
        List<Long> ids = asyncDownloadRecordMapper.selectRetryableRegisterIds(startTime, orphanBefore, limit);
        return ids == null ? Collections.emptyList() : ids;
    }
```

`DefaultDownloadStorageServiceImpl`:

```java
    @Override
    public boolean claimRetry(Long registerId, String operator) {
        return asyncDownloadService.claimRetry(registerId, operator);
    }

    @Override
    public List<Long> listRetryable(int lookBackHours, int limit, int orphanGraceSeconds) {
        return asyncDownloadService.listRetryable(lookBackHours, limit, orphanGraceSeconds);
    }
```

`DownloadController` (import `RetryDownloadRequest`, `ListRetryableRequest`, `Constants`):

```java
    @PostMapping("/retry")
    public ResponseResult<Boolean> retry(@RequestBody @Valid RetryDownloadRequest request) {
        return ResponseResult.ok(asyncDownloadService.claimRetry(request.getRegisterId(), Constants.SYSTEM_USER));
    }

    @PostMapping("/listRetryable")
    public ResponseResult<List<Long>> listRetryable(@RequestBody @Valid ListRetryableRequest request) {
        int lookBack = request.getLookBackHours() == null ? 24 : request.getLookBackHours();
        int limit = request.getLimit() == null ? 50 : request.getLimit();
        int grace = request.getOrphanGraceSeconds() == null ? 30 : request.getOrphanGraceSeconds();
        return ResponseResult.ok(asyncDownloadService.listRetryable(lookBack, limit, grace));
    }
```

- [ ] **Step 4: Re-run the test**

```bash
mvn -pl easyfile-server -am test -Dtest=AsyncDownloadServiceImplClaimRetryTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add easyfile-server/pom.xml \
  easyfile-server/src/main/java/com/openquartz/easyfile/server/mapper/AsyncDownloadRecordMapper.java \
  easyfile-server/src/main/resources/mapper/AsyncDownloadRecordMapper.xml \
  easyfile-server/src/main/java/com/openquartz/easyfile/server/service/AsyncDownloadService.java \
  easyfile-server/src/main/java/com/openquartz/easyfile/server/service/impl/AsyncDownloadServiceImpl.java \
  easyfile-server/src/main/java/com/openquartz/easyfile/server/service/impl/DefaultDownloadStorageServiceImpl.java \
  easyfile-server/src/main/java/com/openquartz/easyfile/server/controller/DownloadController.java \
  easyfile-server/src/test/java/com/openquartz/easyfile/server/service/impl/AsyncDownloadServiceImplClaimRetryTest.java
git commit -m "feat(server): expose download retry claim and list APIs"
```

---

### Task 3: Remote client claim + list (null-safe)

**Files:**
- Modify: `easyfile-storage/easyfile-storage-remote/pom.xml` (test starter)
- Modify: `easyfile-storage/easyfile-storage-remote/src/main/java/com/openquartz/easyfile/storage/remote/impl/RemoteUrlConstants.java`
- Modify: `easyfile-storage/easyfile-storage-remote/src/main/java/com/openquartz/easyfile/storage/remote/EasyFileClient.java`
- Modify: `easyfile-storage/easyfile-storage-remote/src/main/java/com/openquartz/easyfile/storage/remote/impl/HttpEasyFileClientImpl.java`
- Modify: `easyfile-storage/easyfile-storage-remote/src/main/java/com/openquartz/easyfile/storage/remote/impl/RemoteDownloadStorageServiceImpl.java`
- Test: `easyfile-storage/easyfile-storage-remote/src/test/java/com/openquartz/easyfile/storage/remote/impl/RemoteDownloadStorageServiceImplClaimRetryTest.java`

**Interfaces:**
- Consumes: Task 1 service methods; Task 2 URLs `/easyfile/download/retry` and `/easyfile/download/listRetryable`; `RetryDownloadRequest`; `ListRetryableRequest`
- Produces:
  - `ResponseResult<Boolean> EasyFileClient.claimRetry(RetryDownloadRequest request)`
  - `ResponseResult<List<Long>> EasyFileClient.listRetryable(ListRetryableRequest request)`
  - Remote `claimRetry` returns false when result is null, not success, or `data == null`
  - Remote `listRetryable` returns empty list when result or `data` is null

- [ ] **Step 1: Write the failing test**

```java
package com.openquartz.easyfile.storage.remote.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.request.ListRetryableRequest;
import com.openquartz.easyfile.common.request.RetryDownloadRequest;
import com.openquartz.easyfile.storage.remote.EasyFileClient;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RemoteDownloadStorageServiceImplClaimRetryTest {

    @Mock
    private EasyFileClient easyFileClient;
    @InjectMocks
    private RemoteDownloadStorageServiceImpl service;

    @Test
    public void claimRetry_falseWhenResultNull() {
        when(easyFileClient.claimRetry(any(RetryDownloadRequest.class))).thenReturn(null);
        assertFalse(service.claimRetry(1L, "system"));
    }

    @Test
    public void claimRetry_falseWhenDataNull() {
        ResponseResult<Boolean> result = ResponseResult.ok();
        when(easyFileClient.claimRetry(any(RetryDownloadRequest.class))).thenReturn(result);
        assertFalse(service.claimRetry(2L, "system"));
    }

    @Test
    public void claimRetry_trueWhenDataTrue() {
        when(easyFileClient.claimRetry(any(RetryDownloadRequest.class))).thenReturn(ResponseResult.ok(Boolean.TRUE));
        assertTrue(service.claimRetry(3L, "system"));
    }

    @Test
    public void listRetryable_emptyWhenDataNull() {
        when(easyFileClient.listRetryable(any(ListRetryableRequest.class))).thenReturn(ResponseResult.ok());
        assertTrue(service.listRetryable(24, 50, 30).isEmpty());
    }

    @Test
    public void listRetryable_returnsData() {
        when(easyFileClient.listRetryable(any(ListRetryableRequest.class)))
            .thenReturn(ResponseResult.ok(Arrays.asList(8L, 9L)));
        assertTrue(service.listRetryable(24, 50, 30).containsAll(Arrays.asList(8L, 9L)));
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails**

```bash
mvn -pl easyfile-storage/easyfile-storage-remote -am test -Dtest=RemoteDownloadStorageServiceImplClaimRetryTest
```

Expected: compile failure, `claimRetry` missing on `EasyFileClient`.

- [ ] **Step 3: Implement remote wiring**

`RemoteUrlConstants`:

```java
    public static final String DOWNLOAD_RETRY_URL = "/easyfile/download/retry";
    public static final String DOWNLOAD_LIST_RETRYABLE_URL = "/easyfile/download/listRetryable";
```

`EasyFileClient`:

```java
    ResponseResult<Boolean> claimRetry(RetryDownloadRequest request);

    ResponseResult<java.util.List<Long>> listRetryable(ListRetryableRequest request);
```

`HttpEasyFileClientImpl`:

```java
    @Override
    public ResponseResult<Boolean> claimRetry(RetryDownloadRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_RETRY_URL, request, Boolean.class);
    }

    @Override
    public ResponseResult<List<Long>> listRetryable(ListRetryableRequest request) {
        return httpAgent.httpPost(RemoteUrlConstants.DOWNLOAD_LIST_RETRYABLE_URL, request,
            new TypeReference<ResponseResult<List<Long>>>() {
            });
    }
```

`RemoteDownloadStorageServiceImpl`:

```java
    @Override
    public boolean claimRetry(Long registerId, String operator) {
        ResponseResult<Boolean> result = easyFileClient.claimRetry(RetryDownloadRequest.create(registerId));
        if (result == null || !result.isSuccess()) {
            return false;
        }
        return Boolean.TRUE.equals(result.getData());
    }

    @Override
    public List<Long> listRetryable(int lookBackHours, int limit, int orphanGraceSeconds) {
        ResponseResult<List<Long>> result = easyFileClient.listRetryable(
            ListRetryableRequest.create(lookBackHours, limit, orphanGraceSeconds));
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return Collections.emptyList();
        }
        return result.getData();
    }
```

(`operator` is unused on the wire because the server API always claims as `"system"`; keep the method signature.)

- [ ] **Step 4: Re-run the test**

```bash
mvn -pl easyfile-storage/easyfile-storage-remote -am test -Dtest=RemoteDownloadStorageServiceImplClaimRetryTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add easyfile-storage/easyfile-storage-remote
git commit -m "feat(remote): add null-safe download retry client calls"
```

---

### Task 4: Trigger retrigger

**Files:**
- Modify: `easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/download/DownloadTriggerService.java`
- Modify: `easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/mapper/AsyncDownloadTriggerMapper.java`
- Modify: `easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/mapper/impl/AsyncDownloadTriggerMapperImpl.java`
- Modify: `easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/impl/LocalDownloadTriggerServiceImpl.java`
- Test: `easyfile-storage/easyfile-storage-local/src/test/java/com/openquartz/easyfile/storage/local/impl/LocalDownloadTriggerServiceImplRetriggerTest.java`

**Interfaces:**
- Consumes: existing `trigger(DownloadTriggerRequest)` insert-if-absent; `selectByRegisterId`; `insert`
- Produces:
  - `void DownloadTriggerService.retrigger(Long registerId)`
  - `int AsyncDownloadTriggerMapper.resetForRetry(Long registerId)` — sets `trigger_status='init'`, `trigger_count=0`, `last_execute_time=now()` for that `register_id` regardless of current status

- [ ] **Step 1: Write the failing test**

```java
package com.openquartz.easyfile.storage.local.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.request.DownloadTriggerRequest;
import com.openquartz.easyfile.storage.local.entity.AsyncDownloadTrigger;
import com.openquartz.easyfile.storage.local.mapper.AsyncDownloadTriggerMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LocalDownloadTriggerServiceImplRetriggerTest {

    @Mock
    private AsyncDownloadTriggerMapper asyncDownloadTriggerMapper;
    @InjectMocks
    private LocalDownloadTriggerServiceImpl service;

    @Test
    public void retrigger_resetsExistingRowAndDoesNotInsert() {
        AsyncDownloadTrigger existing = new AsyncDownloadTrigger();
        existing.setRegisterId(5L);
        when(asyncDownloadTriggerMapper.selectByRegisterId(5L)).thenReturn(existing);
        when(asyncDownloadTriggerMapper.resetForRetry(5L)).thenReturn(1);

        service.retrigger(5L);

        verify(asyncDownloadTriggerMapper).resetForRetry(5L);
        verify(asyncDownloadTriggerMapper, never()).insert(any());
    }

    @Test
    public void retrigger_insertsWhenMissing() {
        when(asyncDownloadTriggerMapper.selectByRegisterId(6L)).thenReturn(null);

        service.retrigger(6L);

        verify(asyncDownloadTriggerMapper, never()).resetForRetry(6L);
        verify(asyncDownloadTriggerMapper).insert(any(AsyncDownloadTrigger.class));
    }

    @Test
    public void trigger_stillSkipsWhenRowExists() {
        AsyncDownloadTrigger existing = new AsyncDownloadTrigger();
        when(asyncDownloadTriggerMapper.selectByRegisterId(7L)).thenReturn(existing);
        DownloadTriggerRequest request = new DownloadTriggerRequest();
        request.setRegisterId(7L);

        service.trigger(request);

        verify(asyncDownloadTriggerMapper, never()).insert(any());
        verify(asyncDownloadTriggerMapper, never()).resetForRetry(7L);
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails**

```bash
mvn -pl easyfile-storage/easyfile-storage-local -am test -Dtest=LocalDownloadTriggerServiceImplRetriggerTest
```

Expected: FAIL, `retrigger` / `resetForRetry` missing.

- [ ] **Step 3: Implement retrigger**

`DownloadTriggerService`:

```java
    void retrigger(Long registerId);
```

Mapper:

```java
    int resetForRetry(Long registerId);
```

`AsyncDownloadTriggerMapperImpl`:

```java
    private static final String RESET_FOR_RETRY_SQL =
        "update {0} set trigger_status = 'init', trigger_count = 0, last_execute_time = now() where register_id = ?";

    @Override
    public int resetForRetry(Long registerId) {
        String sql = MessageFormat.format(RESET_FOR_RETRY_SQL,
            EasyFileTableGeneratorSupplier.genAsyncDownloadTriggerTable());
        return jdbcTemplate.update(sql, registerId);
    }
```

`LocalDownloadTriggerServiceImpl.retrigger`:

```java
    @Override
    public void retrigger(Long registerId) {
        AsyncDownloadTrigger existing = asyncDownloadTriggerMapper.selectByRegisterId(registerId);
        if (existing != null) {
            asyncDownloadTriggerMapper.resetForRetry(registerId);
            return;
        }
        DownloadTriggerRequest request = new DownloadTriggerRequest();
        request.setRegisterId(registerId);
        trigger(request);
    }
```

- [ ] **Step 4: Re-run the test**

```bash
mvn -pl easyfile-storage/easyfile-storage-local -am test -Dtest=LocalDownloadTriggerServiceImplRetriggerTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/download/DownloadTriggerService.java \
  easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/mapper/AsyncDownloadTriggerMapper.java \
  easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/mapper/impl/AsyncDownloadTriggerMapperImpl.java \
  easyfile-storage/easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/impl/LocalDownloadTriggerServiceImpl.java \
  easyfile-storage/easyfile-storage-local/src/test/java/com/openquartz/easyfile/storage/local/impl/LocalDownloadTriggerServiceImplRetriggerTest.java
git commit -m "feat(trigger): reset existing trigger rows for business retry"
```

---

### Task 5: dispatchRetry + reexecute hooks

**Files:**
- Modify: `easyfile-core/pom.xml` (test starter)
- Modify: `easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/BaseAsyncFileHandler.java`
- Modify: `easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/AsyncFileHandlerAdapter.java`
- Modify: `easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/impl/DatabaseAsyncFileHandlerAdapter.java`
- Modify: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/DefaultAsyncFileHandler.java`
- Modify: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/ScheduleTriggerAsyncFileHandler.java`
- Modify: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/MqTriggerAsyncFileHandler.java`
- Test: `easyfile-core/src/test/java/com/openquartz/easyfile/core/executor/AsyncFileHandlerAdapterDispatchRetryTest.java`

**Interfaces:**
- Consumes: `DownloadStorageService.claimRetry(Long, String)`, `getRequestInfoByRegisterId(Long)`; `FileExportExecutorSupport.get/contains`; `Constants.SYSTEM_USER`; `DownloadTriggerService.retrigger(Long)` from Task 4
- Produces:
  - `void BaseAsyncFileHandler.reexecute(Long registerId)`
  - `void AsyncFileHandlerAdapter.dispatchRetry(Long registerId)` — try/catch, claim then `reexecute`
  - Default `execute` runnable ends with `dispatchRetry`
  - `DatabaseAsyncFileHandlerAdapter.doTrigger` calls `dispatchRetry` after `exeSuccess` / `exeFail` and after clearing `FileExportTriggerContext`
  - `ScheduleTriggerAsyncFileHandler.reexecute` = `retrigger` then `scheduleExecutorService.execute(() -> doTrigger(entry))` (not same-stack recursion)
  - `MqTriggerAsyncFileHandler.reexecute` = `retrigger` then `doSend` (not `afterCommit`)

- [ ] **Step 1: Write the failing adapter test**

Register a fake executor in `FileExportExecutorSupport` using a unique code `retryDispatchDemo`.

```java
package com.openquartz.easyfile.core.executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.bean.BaseDownloaderRequestContext;
import com.openquartz.easyfile.common.bean.DownloadRequestInfo;
import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.core.annotations.FileExportExecutor;
import com.openquartz.easyfile.core.executor.support.FileExportExecutorSupport;
import com.openquartz.easyfile.core.property.IEasyFileDownloadProperty;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import com.openquartz.easyfile.storage.file.UploadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncFileHandlerAdapterDispatchRetryTest {

    @Mock
    private IEasyFileDownloadProperty downloadProperties;
    @Mock
    private UploadService uploadService;
    @Mock
    private DownloadStorageService downloadStorageService;

    private RecordingHandler handler;
    private StubExecutor executor;

    @Before
    public void setUp() {
        handler = new RecordingHandler(downloadProperties, uploadService, downloadStorageService);
        executor = new StubExecutor();
        FileExportExecutorSupport.register("retryDispatchDemo",
            StubExecutor.class.getAnnotation(FileExportExecutor.class), executor);
    }

    @Test
    public void dispatchRetry_claimsThenReexecutes() {
        DownloadRequestInfo info = new DownloadRequestInfo();
        info.setDownloadCode("retryDispatchDemo");
        info.setRequestContext(new BaseDownloaderRequestContext());
        when(downloadStorageService.claimRetry(11L, Constants.SYSTEM_USER)).thenReturn(true);
        when(downloadStorageService.getRequestInfoByRegisterId(11L)).thenReturn(info);

        handler.dispatchRetry(11L);

        assertEquals(1, handler.executeCount);
        verify(downloadStorageService).claimRetry(11L, Constants.SYSTEM_USER);
    }

    @Test
    public void dispatchRetry_skipsReexecuteWhenClaimFails() {
        when(downloadStorageService.claimRetry(11L, Constants.SYSTEM_USER)).thenReturn(false);

        handler.dispatchRetry(11L);

        assertEquals(0, handler.executeCount);
        verify(downloadStorageService, never()).getRequestInfoByRegisterId(any());
    }

    @Test
    public void dispatchRetry_swallowsClaimException() {
        when(downloadStorageService.claimRetry(eq(11L), any())).thenThrow(new RuntimeException("boom"));
        handler.dispatchRetry(11L);
        assertEquals(0, handler.executeCount);
    }

    @Test
    public void reexecute_skipsWhenExecutorMissing() {
        DownloadRequestInfo info = new DownloadRequestInfo();
        info.setDownloadCode("missing-code");
        when(downloadStorageService.getRequestInfoByRegisterId(12L)).thenReturn(info);
        handler.reexecute(12L);
        assertEquals(0, handler.executeCount);
        assertFalse(FileExportExecutorSupport.contains("missing-code"));
    }

    private static final class RecordingHandler extends AsyncFileHandlerAdapter {
        private int executeCount;

        private RecordingHandler(IEasyFileDownloadProperty p, UploadService u, DownloadStorageService s) {
            super(p, u, s);
        }

        @Override
        public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
            executeCount++;
        }
    }

    @FileExportExecutor("retryDispatchDemo")
    private static final class StubExecutor implements BaseDownloadExecutor {
        @Override
        public boolean enableAsync(BaseDownloaderRequestContext context) {
            return true;
        }

        @Override
        public void export(com.openquartz.easyfile.common.bean.DownloaderRequestContext context) {
        }
    }
}
```

`StubExecutor` only needs `enableAsync` and `export`. `FileExportExecutorSupport.register` uses `putIfAbsent`, so the same code can be registered once per JVM.

- [ ] **Step 2: Run the test and confirm it fails**

```bash
mvn -pl easyfile-core -am test -Dtest=AsyncFileHandlerAdapterDispatchRetryTest
```

Expected: FAIL, `dispatchRetry` missing.

- [ ] **Step 3: Implement adapter + handler hooks**

`BaseAsyncFileHandler`:

```java
    default void reexecute(Long registerId) {
        // no-op default; adapter overrides
    }
```

`AsyncFileHandlerAdapter` — `downloadStorageService` is already a field. Add:

```java
    @Override
    public void reexecute(Long registerId) {
        DownloadRequestInfo requestInfo = downloadStorageService.getRequestInfoByRegisterId(registerId);
        if (requestInfo == null || requestInfo.getRequestContext() == null
            || !FileExportExecutorSupport.contains(requestInfo.getDownloadCode())) {
            logger.error("[AsyncFileHandlerAdapter#reexecute] skip, missing request or executor, registerId:{}",
                registerId);
            return;
        }
        execute(FileExportExecutorSupport.get(requestInfo.getDownloadCode()),
            requestInfo.getRequestContext(), registerId);
    }

    protected void dispatchRetry(Long registerId) {
        try {
            if (!downloadStorageService.claimRetry(registerId, Constants.SYSTEM_USER)) {
                return;
            }
            reexecute(registerId);
        } catch (Exception ex) {
            logger.error("[AsyncFileHandlerAdapter#dispatchRetry] error, registerId:{}", registerId, ex);
        }
    }
```

Add imports for `DownloadRequestInfo`, `FileExportExecutorSupport`, `Constants`.

`DefaultAsyncFileHandler.execute`:

```java
    @Override
    public void execute(BaseDownloadExecutor executor, BaseDownloaderRequestContext baseRequest, Long registerId) {
        executorService.execute(() -> {
            doExecute(executor, baseRequest, registerId);
            dispatchRetry(registerId);
        });
    }
```

`DatabaseAsyncFileHandlerAdapter.doTrigger` — keep the try/catch, then dispatch **after** `exeSuccess`/`exeFail` and after `FileExportTriggerContext.clear()`:

```java
    public void doTrigger(DownloadTriggerEntry k) {
        boolean execute = triggerService.startExecute(k.getRegisterId(), k.getTriggerCount());
        if (execute) {
            try {
                DownloadRequestInfo requestInfo = storageService.getRequestInfoByRegisterId(k.getRegisterId());
                BaseDownloadExecutor executor = FileExportExecutorSupport.get(requestInfo.getDownloadCode());
                FileExportTriggerContext.setAsyncTriggerFlagIfAbsent(true);
                doExecute(executor, requestInfo.getRequestContext(), k.getRegisterId());
                triggerService.exeSuccess(k.getRegisterId());
            } catch (Exception ex) {
                log.error("[DatabaseAsyncFileHandlerAdapter#doTrigger] execute-failed!registerId:{}",
                    k.getRegisterId(), ex);
                triggerService.exeFail(k.getRegisterId());
            } finally {
                FileExportTriggerContext.clear();
            }
            dispatchRetry(k.getRegisterId());
        }
    }
```

`ScheduleTriggerAsyncFileHandler` — `scheduleExecutorService` is already a field. Override `reexecute`:

```java
    @Override
    public void reexecute(Long registerId) {
        triggerService.retrigger(registerId);
        DownloadTriggerEntry entry = triggerService.getTriggerRegisterId(registerId,
            handlerProperties.getMaxTriggerCount());
        if (entry == null) {
            return;
        }
        scheduleExecutorService.execute(() -> doTrigger(entry));
    }
```

Do **not** call `doTrigger` on the same stack as `dispatchRetry` (that would recurse until quota).

`MqTriggerAsyncFileHandler` — change `doSend` to `protected` (or package) and override:

```java
    @Override
    public void reexecute(Long registerId) {
        triggerService.retrigger(registerId);
        doSend(registerId);
    }
```

Leave first-run `execute()` using `afterCommit` + `doSend` unchanged.

- [ ] **Step 4: Re-run the test**

```bash
mvn -pl easyfile-core -am test -Dtest=AsyncFileHandlerAdapterDispatchRetryTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add easyfile-core/pom.xml \
  easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/BaseAsyncFileHandler.java \
  easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/AsyncFileHandlerAdapter.java \
  easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/impl/DatabaseAsyncFileHandlerAdapter.java \
  easyfile-core/src/test/java/com/openquartz/easyfile/core/executor/AsyncFileHandlerAdapterDispatchRetryTest.java \
  easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/DefaultAsyncFileHandler.java \
  easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/ScheduleTriggerAsyncFileHandler.java \
  easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/handler/MqTriggerAsyncFileHandler.java
git commit -m "feat(handler): dispatch business retry after execute completes"
```

---

### Task 6: Scanner + properties

**Files:**
- Create: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/spring/boot/autoconfig/properties/EasyFileDownloadRetryProperties.java`
- Create: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/retry/DownloadRetryScanner.java`
- Create: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/spring/boot/autoconfig/DownloadRetryAutoConfiguration.java`
- Modify: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/resources/META-INF/spring.factories`
- Modify: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Test: `easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/test/java/com/openquartz/easyfile/starter/trigger/retry/DownloadRetryScannerTest.java`

**Interfaces:**
- Consumes: `DownloadStorageService.listRetryable(int,int,int)`, `claimRetry(Long,String)`, `BaseAsyncFileHandler.reexecute(Long)`, `Constants.SYSTEM_USER`
- Produces:
  - `EasyFileDownloadRetryProperties` prefix `easyfile.download.retry` with `scanEnabled=true`, `scanPeriodSeconds=10`, `orphanGraceSeconds=30`, `lookBackHours=24`, `scanBatchSize=50`
  - `void DownloadRetryScanner.scan()` — for each id: `claimRetry` (ignore boolean) then `reexecute`; swallow per-id errors
  - Scheduled bean when `scanEnabled` is true and `BaseAsyncFileHandler` exists

- [ ] **Step 1: Write the failing scanner test**

```java
package com.openquartz.easyfile.starter.trigger.retry;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadRetryProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadRetryScannerTest {

    @Mock
    private DownloadStorageService downloadStorageService;
    @Mock
    private BaseAsyncFileHandler handler;

    @Test
    public void scan_claimsAndReexecutesEachId() {
        EasyFileDownloadRetryProperties properties = new EasyFileDownloadRetryProperties();
        when(downloadStorageService.listRetryable(24, 50, 30)).thenReturn(Arrays.asList(1L, 2L));
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, handler, properties);

        scanner.scan();

        verify(downloadStorageService).claimRetry(1L, Constants.SYSTEM_USER);
        verify(downloadStorageService).claimRetry(2L, Constants.SYSTEM_USER);
        verify(handler).reexecute(1L);
        verify(handler).reexecute(2L);
    }

    @Test
    public void scan_continuesWhenOneIdThrows() {
        EasyFileDownloadRetryProperties properties = new EasyFileDownloadRetryProperties();
        when(downloadStorageService.listRetryable(24, 50, 30)).thenReturn(Arrays.asList(1L, 2L));
        doThrow(new RuntimeException("x")).when(handler).reexecute(1L);
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, handler, properties);

        scanner.scan();

        verify(handler).reexecute(2L);
    }

    @Test
    public void scan_noopWhenEmpty() {
        EasyFileDownloadRetryProperties properties = new EasyFileDownloadRetryProperties();
        when(downloadStorageService.listRetryable(24, 50, 30)).thenReturn(Collections.emptyList());
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, handler, properties);

        scanner.scan();

        verify(handler, never()).reexecute(anyLong());
        verify(downloadStorageService, never()).claimRetry(anyLong(), anyString());
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails**

```bash
mvn -pl easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent -am test -Dtest=DownloadRetryScannerTest
```

Expected: FAIL, `DownloadRetryScanner` missing.

- [ ] **Step 3: Implement properties, scanner, auto-config**

`EasyFileDownloadRetryProperties.java`:

```java
package com.openquartz.easyfile.starter.spring.boot.autoconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = EasyFileDownloadRetryProperties.PREFIX)
public class EasyFileDownloadRetryProperties {

    public static final String PREFIX = "easyfile.download.retry";

    private boolean scanEnabled = true;
    private int scanPeriodSeconds = 10;
    private int orphanGraceSeconds = 30;
    private int lookBackHours = 24;
    private int scanBatchSize = 50;
}
```

`DownloadRetryScanner.java`:

```java
package com.openquartz.easyfile.starter.trigger.retry;

import com.openquartz.easyfile.common.constants.Constants;
import com.openquartz.easyfile.common.util.CollectionUtils;
import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadRetryProperties;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadRetryScanner {

    private final DownloadStorageService downloadStorageService;
    private final BaseAsyncFileHandler handler;
    private final EasyFileDownloadRetryProperties properties;

    public DownloadRetryScanner(DownloadStorageService downloadStorageService, BaseAsyncFileHandler handler,
        EasyFileDownloadRetryProperties properties) {
        this.downloadStorageService = downloadStorageService;
        this.handler = handler;
        this.properties = properties;
    }

    public void scan() {
        List<Long> ids = downloadStorageService.listRetryable(properties.getLookBackHours(),
            properties.getScanBatchSize(), properties.getOrphanGraceSeconds());
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        for (Long registerId : ids) {
            try {
                downloadStorageService.claimRetry(registerId, Constants.SYSTEM_USER);
                handler.reexecute(registerId);
            } catch (Exception ex) {
                log.error("[DownloadRetryScanner#scan] registerId:{}", registerId, ex);
            }
        }
    }
}
```

`DownloadRetryAutoConfiguration.java`:

```java
package com.openquartz.easyfile.starter.spring.boot.autoconfig;

import com.openquartz.easyfile.common.concurrent.ThreadFactoryBuilder;
import com.openquartz.easyfile.core.executor.BaseAsyncFileHandler;
import com.openquartz.easyfile.starter.spring.boot.autoconfig.properties.EasyFileDownloadRetryProperties;
import com.openquartz.easyfile.starter.trigger.retry.DownloadRetryScanner;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EasyFileDownloadRetryProperties.class)
@ConditionalOnBean({BaseAsyncFileHandler.class, DownloadStorageService.class})
@ConditionalOnProperty(prefix = EasyFileDownloadRetryProperties.PREFIX, name = "scan-enabled", havingValue = "true",
    matchIfMissing = true)
@AutoConfigureAfter({DefaultAsyncFileHandlerAutoConfiguration.class, ScheduleAsyncFileHandlerAutoConfiguration.class,
    DisruptorAsyncFileHandlerAutoConfiguration.class, RocketMqAsyncFileHandlerAutoConfiguration.class})
public class DownloadRetryAutoConfiguration {

    @Bean
    public DownloadRetryScanner downloadRetryScanner(DownloadStorageService downloadStorageService,
        BaseAsyncFileHandler baseAsyncFileHandler, EasyFileDownloadRetryProperties properties) {
        DownloadRetryScanner scanner = new DownloadRetryScanner(downloadStorageService, baseAsyncFileHandler,
            properties);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1,
            new ThreadFactoryBuilder().setNameFormat("DownloadRetryScanner-thread-%d").build());
        executor.scheduleAtFixedRate(scanner::scan, properties.getScanPeriodSeconds(),
            properties.getScanPeriodSeconds(), TimeUnit.SECONDS);
        return scanner;
    }
}
```

Append this line to both `spring.factories` and `AutoConfiguration.imports`:

```
com.openquartz.easyfile.starter.spring.boot.autoconfig.DownloadRetryAutoConfiguration
```

- [ ] **Step 4: Re-run the test**

```bash
mvn -pl easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent -am test -Dtest=DownloadRetryScannerTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/spring/boot/autoconfig/properties/EasyFileDownloadRetryProperties.java \
  easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/trigger/retry/DownloadRetryScanner.java \
  easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/java/com/openquartz/easyfile/starter/spring/boot/autoconfig/DownloadRetryAutoConfiguration.java \
  easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/resources/META-INF/spring.factories \
  easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports \
  easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent/src/test/java/com/openquartz/easyfile/starter/trigger/retry/DownloadRetryScannerTest.java
git commit -m "feat(starter): scan claimable FAIL and orphan NONE retries"
```

---

### Task 7: Admin retry API + UI

**Files:**
- Modify: `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/controller/DownloadTaskController.java`
- Modify: `easyfile-ui/src/main/resources/easyfile-admin-ui/templates/download-task.html`
- Modify: `easyfile-ui/src/main/resources/easyfile-admin-ui/static/js/download-task.js`
- Modify: `easyfile-ui/pom.xml` (add `spring-boot-starter-test` test scope)
- Test: `easyfile-ui/src/test/java/com/openquartz/easyfile/admin/controller/DownloadTaskControllerRetryTest.java`

**Interfaces:**
- Consumes: `DownloadStorageService.retry(Long, String)`; `RetryDownloadTaskRequest.registerId`; `AdminProperty.getAdminUsername()`
- Produces: `POST /easyfile-ui/download-task/retry` `@Auth`, returns `ResponseResult.ok()` when claim succeeds, `ResponseResult.fail("01", "重试失败")` when not. Admin **only claims**. `easyfile-ui` does not depend on `easyfile-core`; same-process `reexecute` is the scanner / business-app handler. UI shows 重试 only when `uploadStatus === 'fail'`.

- [ ] **Step 1: Write the failing controller test**

`DownloadTaskController` currently uses `@RequiredArgsConstructor` with three finals. Add an optional handler via setter or `@Autowired(required = false)` field so tests can omit it.

```java
package com.openquartz.easyfile.admin.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.openquartz.easyfile.admin.model.request.RetryDownloadTaskRequest;
import com.openquartz.easyfile.admin.property.AdminProperty;
import com.openquartz.easyfile.admin.service.ServerAppIdProvider;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.storage.download.DownloadStorageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadTaskControllerRetryTest {

    @Mock
    private DownloadStorageService downloadStorageService;
    @Mock
    private ServerAppIdProvider serverAppIdProvider;
    private DownloadTaskController controller;

    @Before
    public void setUp() {
        AdminProperty adminProperty = new AdminProperty();
        adminProperty.setAdminUsername("admin");
        controller = new DownloadTaskController(downloadStorageService, serverAppIdProvider, adminProperty);
    }

    @Test
    public void retry_okWhenClaimSucceeds() {
        when(downloadStorageService.retry(4L, "admin")).thenReturn(true);
        RetryDownloadTaskRequest request = new RetryDownloadTaskRequest();
        request.setRegisterId(4L);

        ResponseResult<?> result = controller.retry(request);

        assertTrue(result.isSuccess());
        verify(downloadStorageService).retry(4L, "admin");
    }

    @Test
    public void retry_failWhenClaimRejected() {
        when(downloadStorageService.retry(4L, "admin")).thenReturn(false);
        RetryDownloadTaskRequest request = new RetryDownloadTaskRequest();
        request.setRegisterId(4L);

        ResponseResult<?> result = controller.retry(request);

        assertFalse(result.isSuccess());
        assertEquals("01", result.getCode());
    }
}
```

Add `spring-boot-starter-test` with `<scope>test</scope>` to `easyfile-ui/pom.xml`.

- [ ] **Step 2: Run the test and confirm it fails**

```bash
mvn -pl easyfile-ui -am test -Dtest=DownloadTaskControllerRetryTest
```

Expected: FAIL, `retry` mapping missing.

- [ ] **Step 3: Implement controller + UI**

Controller method (claim only; scanner / handler `dispatchRetry` re-queues):

```java
    @Auth
    @ResponseBody
    @PostMapping(value = "/retry", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<?> retry(@RequestBody @Valid RetryDownloadTaskRequest request) {
        boolean claimed = downloadStorageService.retry(request.getRegisterId(), adminProperty.getAdminUsername());
        if (!claimed) {
            return ResponseResult.fail("01", "重试失败");
        }
        return ResponseResult.ok();
    }
```

Add import for `RetryDownloadTaskRequest`.

In `download-task.js` columns, replace `{fixed: 'right', title: '操作', toolbar: '#clickDownload', width: 150}` with:

```javascript
      , {
        fixed: 'right', title: '操作', width: 200, templet: function (d) {
          var html = '<a class="layui-btn layui-btn-xs" lay-event="click">下载</a>';
          if (d.uploadStatus === 'fail') {
            html += '<a class="layui-btn layui-btn-xs" lay-event="retry">重试</a>';
          }
          html += '<a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="cancel">取消</a>';
          return html;
        }
      }
```

Delete the unused `#clickDownload` script from `download-task.html`.

In the `tool(table)` listener, add `else if (obj.event === 'retry')` modeled on cancel:

```javascript
    } else if (obj.event === 'retry') {
      layer.confirm('真的要重试下载任务: ' + data.registerId + ' 吗?', {icon: 3, title: '提示'}, function (index) {
        var requestBody = {registerId: data.registerId};
        $.ajax({
          type: 'POST',
          url: '/easyfile-ui/download-task/retry',
          data: JSON.stringify(requestBody),
          contentType: 'application/json',
          success: function (result) {
            if (result.success) {
              layer.msg('重试成功！');
              table.reload('table');
            } else {
              layer.msg('重试失败!失败原因:' + result.message);
            }
          },
          error: function (xhr, status, error) {
            layer.msg('重试失败!' + error);
          },
          dataType: 'json'
        });
        layer.close(index);
      });
    }
```

- [ ] **Step 4: Re-run the test**

```bash
mvn -pl easyfile-ui -am test -Dtest=DownloadTaskControllerRetryTest
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add easyfile-ui/src/main/java/com/openquartz/easyfile/admin/controller/DownloadTaskController.java \
  easyfile-ui/src/main/resources/easyfile-admin-ui/templates/download-task.html \
  easyfile-ui/src/main/resources/easyfile-admin-ui/static/js/download-task.js \
  easyfile-ui/src/test/java/com/openquartz/easyfile/admin/controller/DownloadTaskControllerRetryTest.java \
  easyfile-ui/pom.xml
git commit -m "feat(admin): retry FAIL download tasks from the console"
```

---

## Spec coverage

| Spec section | Task |
|---|---|
| Claim CAS `FAIL` + quota | 1, 2 |
| `listRetryable` FAIL + orphan `NONE` | 1, 2, 6 |
| Remote null-safe | 3 |
| `retrigger` after `exeSuccess` | 4, 5 |
| `dispatchRetry` swallow errors, no `current_retry` rollback | 5 |
| default / schedule / MQ re-queue | 5 |
| Scanner grace / batch / period defaults | 6 |
| Admin + server HTTP | 2, 7 |
| `CANCEL` never claimed | 1 SQL `upload_status = 'fail'` |
| First-run `trigger()` unchanged | 4 test |
| Import / `handleFileWithRetry` untouched | all tasks |

## Type checklist

- `claimRetry(Long, String) -> boolean` used by Tasks 1–3, 5–7
- `listRetryable(int, int, int) -> List<Long>` used by Tasks 1–3, 6
- `retrigger(Long)` used by Tasks 4–5
- `reexecute(Long)` / `dispatchRetry(Long)` used by Tasks 5–7
- `RetryDownloadRequest.registerId` used by Tasks 1–3
- `Constants.SYSTEM_USER` is `"system"`

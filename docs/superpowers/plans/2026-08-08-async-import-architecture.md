# Async Import Architecture Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete EasyFile async import so it matches export’s task model: dual entry (AOP + HTTP), ThreadPool + DB Trigger handlers, sync+async, local+remote storage, streaming parsers, limiting/notify/listeners, and runnable examples.

**Architecture:** Import mirrors export with separate domain tables/APIs. Shared lifecycle patterns are copied (not a unified meta-model). Users implement `validate`/`processBusiness`; framework owns register → handler → progress/complete. Spec: `docs/superpowers/specs/2026-08-08-async-import-architecture-design.md`.

**Tech Stack:** Java 8+, Spring Boot starter AOP/auto-config, Maven multi-module (`easyfile-common` / `core` / `storage-*` / `server` / `starter` / `example`), JUnit 4/5 + Mockito as used by neighboring modules, Apache POI / streaming Excel + CSV parsers.

## Global Constraints

- Follow export package/naming style; do not merge import/export tables.
- Do not implement MQ import handler (SPI reserve only).
- Do not redesign Admin UI pages; keep APIs compatible with `PARTIAL_SUCCESS`.
- Do not force-refactor export onto a shared lifecycle base in this plan.
- Field `ImportRegisterRequest.ossUrl` is the source file URL (keep name for compatibility).
- Status codes use `ImportStatusEnum` codes: `none`, `executing`, `success`, `partial_success`, `fail`, `revoke`, `invalid` (mirror export code style).
- Config: `easyfile.import.trigger-type=thread_pool|db_schedule` (separate from `easyfile.download.async-trigger-type`).
- TDD: failing test → implement → pass → commit per task.
- Supersedes incomplete slice plan `docs/superpowers/plans/2026-05-23-big-data-import-implementation.md` for remaining work.

## File Structure (create / modify map)

| Path | Responsibility |
|---|---|
| `easyfile-common/.../dictionary/ImportStatusEnum.java` | Record lifecycle enum |
| `easyfile-common/.../bean/ImportRequestInfo.java` | Handler recovery payload |
| `easyfile-common/.../request/CancelImportRequest.java` | Cancel DTO |
| `easyfile-common/.../request/ImportTriggerRequest.java` | Trigger insert DTO |
| `easyfile-common/.../request/ImportLimitingRequest.java` | Limiting DTO |
| `easyfile-common/.../response/CancelImportResult.java` | Cancel result |
| `easyfile-common/.../response/ImportTriggerEntry.java` | Trigger claim entry |
| `easyfile-common/.../request/ImportRegisterRequest.java` | Extend notify/locale/app fields if missing |
| `easyfile-storage-api/.../imports/ImportStorageService.java` | Full storage contract |
| `easyfile-storage-api/.../imports/ImportTriggerService.java` | Trigger contract |
| `sql/storage.sql` + `sql/import_tables.sql` | Align DDL: task/record/trigger |
| `easyfile-storage-local/.../entity/AsyncImport{Task,Record,Trigger}.java` | JPA/entities |
| `easyfile-storage-local/.../dictionary/ImportTriggerStatusEnum.java` | Or reuse `DownloadTriggerStatusEnum` |
| `easyfile-storage-local/.../mapper/*Import*Mapper*.java` | SQL mappers |
| `easyfile-storage-local/.../impl/LocalImportStorageServiceImpl.java` | Local storage |
| `easyfile-storage-local/.../impl/LocalImportTriggerServiceImpl.java` | Local trigger |
| `easyfile-storage-remote/.../impl/RemoteImportStorageServiceImpl.java` | Remote storage client |
| `easyfile-core/.../executor/BaseImportExecutor.java` | Add `importResult` / callback defaults |
| `easyfile-core/.../context/ImportRequestContext.java` | User call context (symmetric download) |
| `easyfile-core/.../executor/BaseAsyncImportHandler.java` | Async SPI |
| `easyfile-core/.../executor/AsyncImportHandlerAdapter.java` | Lifecycle template |
| `easyfile-core/.../executor/impl/DatabaseAsyncImportHandlerAdapter.java` | DB trigger adapter |
| `easyfile-core/.../parser/ImportRow.java`, `ImportParserFactory.java`, streaming parsers | Production parsers |
| `easyfile-core/.../executor/AbstractPagedImportExecutor.java` | Engine split + factory usage |
| `easyfile-core/.../intercept/*Import*` | Interceptor + listeners |
| `starter/.../aop/FileImportInterceptor.java` | AOP entry |
| `starter/.../trigger/handler/DefaultAsyncImportHandler.java` | ThreadPool handler |
| `starter/.../trigger/handler/ScheduleTriggerAsyncImportHandler.java` | Schedule handler |
| `starter/.../properties/EasyFileImportProperties.java` | trigger-type + schedule keys |
| `starter/.../autoconfig/*Import*AutoConfiguration.java` | Wire beans |
| `easyfile-server/.../ImportServiceImpl.java` | Real facade (delete stubs) |
| `easyfile-server/.../DefaultImportStorageServiceImpl.java` | Remove or thin-delegate; ownership → local |
| `easyfile-example-*/.../controller/*Import*Controller.java` | Demo endpoints |

---

### Task 1: ImportStatusEnum + recovery/cancel DTOs

**Files:**
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/dictionary/ImportStatusEnum.java`
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/bean/ImportRequestInfo.java`
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/request/CancelImportRequest.java`
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/request/ImportTriggerRequest.java`
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/request/ImportLimitingRequest.java`
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/response/CancelImportResult.java`
- Create: `easyfile-common/src/main/java/com/openquartz/easyfile/common/response/ImportTriggerEntry.java`
- Modify: `easyfile-common/src/main/java/com/openquartz/easyfile/common/request/ImportRegisterRequest.java` — add `notifyEmail`, `locale`, `enableNotify` if absent
- Test: `easyfile-common/src/test/java/com/openquartz/easyfile/common/dictionary/ImportStatusEnumTest.java`

**Interfaces:**
- Consumes: `BaseEnum<String>`
- Produces: `ImportStatusEnum.fromCode(String)`, DTO getters/setters used by storage tasks

- [ ] **Step 1: Write failing test**

```java
package com.openquartz.easyfile.common.dictionary;

import org.junit.Assert;
import org.junit.Test;

public class ImportStatusEnumTest {

    @Test
    public void fromCode_resolvesPartialSuccess() {
        Assert.assertEquals(ImportStatusEnum.PARTIAL_SUCCESS,
                ImportStatusEnum.fromCode("partial_success"));
    }

    @Test
    public void codes_matchExportStyle() {
        Assert.assertEquals("none", ImportStatusEnum.NONE.getCode());
        Assert.assertEquals("executing", ImportStatusEnum.EXECUTING.getCode());
        Assert.assertEquals("success", ImportStatusEnum.SUCCESS.getCode());
        Assert.assertEquals("fail", ImportStatusEnum.FAIL.getCode());
        Assert.assertEquals("revoke", ImportStatusEnum.CANCEL.getCode());
        Assert.assertEquals("invalid", ImportStatusEnum.INVALID.getCode());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl easyfile-common -am -Dtest=ImportStatusEnumTest test`  
Expected: FAIL (class not found)

- [ ] **Step 3: Implement enum + DTOs**

`ImportStatusEnum` values: `NONE("none")`, `EXECUTING("executing")`, `SUCCESS("success")`, `PARTIAL_SUCCESS("partial_success")`, `FAIL("fail")`, `CANCEL("revoke")`, `INVALID("invalid")` — copy structure from `UploadStatusEnum`.

`ImportRequestInfo` fields: `String importCode`, `ImportRegisterRequest registerRequest` (or flattened source URL/params), `Long resumePosition`, `ImportStatusEnum importStatus`.

`CancelImportRequest`: `Long registerId`, `String cancelBy`.  
`CancelImportResult`: `boolean cancelResult`, `String cancelMsg`.  
`ImportTriggerRequest`: `Long registerId`, `Integer maxTriggerCount` (mirror `DownloadTriggerRequest`).  
`ImportTriggerEntry`: `Long registerId`, `Integer triggerCount`, trigger status fields (mirror `DownloadTriggerEntry`).  
`ImportLimitingRequest`: `String appId`, `String importCode`, `String operator` (mirror `ExportLimitingRequest` shape).

Extend `ImportRegisterRequest` with: `String notifyEmail`, `String locale`, `Boolean enableNotify`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -pl easyfile-common -am -Dtest=ImportStatusEnumTest test`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add easyfile-common/src/main/java/com/openquartz/easyfile/common/dictionary/ImportStatusEnum.java \
  easyfile-common/src/main/java/com/openquartz/easyfile/common/bean/ImportRequestInfo.java \
  easyfile-common/src/main/java/com/openquartz/easyfile/common/request/*.java \
  easyfile-common/src/main/java/com/openquartz/easyfile/common/response/CancelImportResult.java \
  easyfile-common/src/main/java/com/openquartz/easyfile/common/response/ImportTriggerEntry.java \
  easyfile-common/src/test/java/com/openquartz/easyfile/common/dictionary/ImportStatusEnumTest.java
git commit -m "$(cat <<'EOF'
feat(common): add import status enum and orchestration DTOs

EOF
)"
```

---

### Task 2: Expand ImportStorageService + add ImportTriggerService APIs

**Files:**
- Modify: `easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/imports/ImportStorageService.java`
- Create: `easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/imports/ImportTriggerService.java`
- Test: `easyfile-storage/easyfile-storage-api/src/test/java/com/openquartz/easyfile/storage/imports/ImportStorageServiceContractTest.java` (compile-only / default-method smoke if no test dir — create test dir)

**Interfaces:**
- Consumes: Task 1 DTOs/enums
- Produces: full method signatures below for local/remote/handlers

- [ ] **Step 1: Write failing compile test (anonymous stub)**

```java
package com.openquartz.easyfile.storage.imports;

import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import org.junit.Test;

public class ImportStorageServiceContractTest {

    @Test
    public void registerAndEnableRunning_arePartOfContract() {
        ImportStorageService svc = new ImportStorageService() {
            @Override public boolean enableRunning(Long registerId) { return true; }
            @Override public Long register(ImportRegisterRequest request) { return 1L; }
            @Override public com.openquartz.easyfile.common.bean.ImportRequestInfo getRequestInfoByRegisterId(Long id) { return null; }
            @Override public void refreshExecuteProgress(Long id, Integer p, Long resume, ImportStatusEnum s) {}
            @Override public void resetExecuteProcess(Long registerId) {}
            @Override public void updateTotalCount(Long registerId, Long totalCount) {}
            @Override public void updateFileHash(Long registerId, String fileHash) {}
            @Override public void updateResultFileUrl(Long registerId, String resultFileUrl) {}
            @Override public void complete(Long id, ImportStatusEnum status, Long total, Long success, Long fail, Long resume, String err) {}
            @Override public com.openquartz.easyfile.common.response.CancelImportResult cancelImport(
                    com.openquartz.easyfile.common.request.CancelImportRequest request) { return null; }
            // keep existing list/retry/revoke/resultFile/getAppTree defaults or override
            @Override public void markExecuting(Long registerId) { enableRunning(registerId); }
            @Override public void refreshProgress(Long registerId, Integer process, Long resumePosition) {
                refreshExecuteProgress(registerId, process, resumePosition, ImportStatusEnum.EXECUTING);
            }
        };
        org.junit.Assert.assertTrue(svc.enableRunning(1L));
        org.junit.Assert.assertEquals(Long.valueOf(1L), svc.register(ImportRegisterRequest.builder().importCode("x").build()));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl easyfile-storage/easyfile-storage-api -am -Dtest=ImportStorageServiceContractTest test`  
Expected: FAIL (missing methods)

- [ ] **Step 3: Replace ImportStorageService + add ImportTriggerService**

Replace `ImportStorageService` with (keep admin helpers; deprecate string `complete(boolean)` by changing to enum overload — update call sites in later tasks):

```java
package com.openquartz.easyfile.storage.imports;

import com.openquartz.easyfile.common.bean.ImportRequestInfo;
import com.openquartz.easyfile.common.dictionary.ImportStatusEnum;
import com.openquartz.easyfile.common.request.AutoTaskRegisterRequest;
import com.openquartz.easyfile.common.request.CancelImportRequest;
import com.openquartz.easyfile.common.request.ImportRegisterRequest;
import com.openquartz.easyfile.common.request.ImportResultQueryRequest;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.CancelImportResult;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.response.ImportResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import java.util.Collections;
import java.util.List;

public interface ImportStorageService {

    boolean enableRunning(Long registerId);

    Long register(ImportRegisterRequest request);

    default void autoRegisterTask(AutoTaskRegisterRequest request) {}

    ImportRequestInfo getRequestInfoByRegisterId(Long registerId);

    void refreshExecuteProgress(Long registerId, Integer executeProcess, Long resumePosition,
                                ImportStatusEnum nextStatus);

    void resetExecuteProcess(Long registerId);

    void updateTotalCount(Long registerId, Long totalCount);

    void updateFileHash(Long registerId, String fileHash);

    void updateResultFileUrl(Long registerId, String resultFileUrl);

    void complete(Long registerId, ImportStatusEnum status, Long totalCount, Long successCount,
                  Long failCount, Long resumePosition, String errorMsg);

    CancelImportResult cancelImport(CancelImportRequest request);

    default Pagination<ImportResult> listImportResult(ImportResultQueryRequest request) {
        return Pagination.empty();
    }

    default boolean retry(Long registerId, String operator) { return false; }

    default boolean revoke(Long registerId, String operator) { return false; }

    default DownloadUrlResult resultFile(Long registerId) { return null; }

    default List<AppTree> getAppTree() { return Collections.emptyList(); }

    /** @deprecated use enableRunning */
    @Deprecated
    default void markExecuting(Long registerId) { enableRunning(registerId); }

    /** @deprecated use refreshExecuteProgress */
    @Deprecated
    default void refreshProgress(Long registerId, Integer process, Long resumePosition) {
        refreshExecuteProgress(registerId, process, resumePosition, ImportStatusEnum.EXECUTING);
    }
}
```

`ImportTriggerService` — copy method names from `DownloadTriggerService`, swapping types to `ImportTriggerRequest` / `ImportTriggerEntry`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -pl easyfile-storage/easyfile-storage-api -am -Dtest=ImportStorageServiceContractTest test`  
Expected: PASS (server/local compile breaks are fixed in Task 4–5; if reactor fails on dependents, compile only api: `mvn -pl easyfile-storage/easyfile-storage-api -Dtest=ImportStorageServiceContractTest test`)

- [ ] **Step 5: Commit**

```bash
git add easyfile-storage/easyfile-storage-api
git commit -m "$(cat <<'EOF'
feat(storage-api): expand import storage and trigger contracts

EOF
)"
```

---

### Task 3: SQL — import task / record / trigger aligned with export

**Files:**
- Modify: `sql/import_tables.sql`
- Modify: `sql/storage.sql` (append import DDL or `#include` comment pointing to import script)
- Test: manual review + optional comment assertion in a small doc check; add `sql/README` note only if repo already documents SQL that way — otherwise verify by grepping table names in a shell step

**Interfaces:**
- Produces: tables `ef_async_import_task`, `ef_async_import_record`, `ef_async_import_trigger`

- [ ] **Step 1: Write failing check script expectation**

```bash
# expect these names after change
rg -n "ef_async_import_task|ef_async_import_trigger|partial_success" sql/import_tables.sql
# currently missing task/trigger → empty/fail
```

- [ ] **Step 2: Run check (expect missing)**

Run: `rg "ef_async_import_task" sql/import_tables.sql`  
Expected: no matches

- [ ] **Step 3: Rewrite import DDL**

Replace record status comment to use `NONE/EXECUTING/SUCCESS/PARTIAL_SUCCESS/FAIL/CANCEL/INVALID`.  
Add `import_task_id`, notify columns (`notify_enable_status`, `notify_email`), `locale`, keep `resume_position` / counts.  
Add tables from spec section 9 (copy from design doc DDL for `ef_async_import_task` and `ef_async_import_trigger`).  
Keep optional `ef_import_error_detail` if still useful for admin; not required by handler path if result file covers errors.  
Append same three core tables into `sql/storage.sql` for single-script deploys.

- [ ] **Step 4: Re-run check**

Run: `rg -n "ef_async_import_task|ef_async_import_trigger|partial_success" sql/import_tables.sql sql/storage.sql`  
Expected: matches in both files

- [ ] **Step 5: Commit**

```bash
git add sql/import_tables.sql sql/storage.sql
git commit -m "$(cat <<'EOF'
feat(sql): align async import task/record/trigger schema

EOF
)"
```

---

### Task 4: Local entities, mappers, LocalImportStorageServiceImpl

**Files:**
- Create: `easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/entity/AsyncImportTask.java`
- Create: `.../entity/AsyncImportRecord.java`
- Create: `.../entity/AsyncImportTrigger.java`
- Create: mappers under `.../mapper/` + `.../mapper/impl/` mirroring download mappers
- Create: `.../impl/LocalImportStorageServiceImpl.java`
- Create: convertor if needed `.../convertor/AsyncImportRecordConverter.java`
- Modify: server `DefaultImportStorageServiceImpl` — either delete and stop wiring, or make it a deprecated wrapper; prefer delete after local is wired
- Test: `easyfile-storage-local/src/test/java/com/openquartz/easyfile/storage/local/impl/LocalImportStorageServiceImplTest.java`

**Interfaces:**
- Consumes: `ImportStorageService` methods from Task 2
- Produces: working local `register` / `enableRunning` CAS / `getRequestInfoByRegisterId` / `complete`

- [ ] **Step 1: Write failing unit tests (mock mapper)**

```java
@Test
public void enableRunning_onlyFromNone() {
    // arrange record status NONE → mapper.refreshStatus returns 1
    // assert service.enableRunning(id) == true
    // when status EXECUTING → mapper returns 0 → false
}

@Test
public void register_rejectsUnknownImportCode() {
    // task mapper findByAppIdAndCode returns null → expect exception or null policy
    // match LocalDownloadStorageServiceImpl behavior for missing download code
}
```

Mirror assertion style from `RemoteDownloadStorageServiceImplEnableRunningTest` and download local register logic in `LocalDownloadStorageServiceImpl.register`.

- [ ] **Step 2: Run tests — expect FAIL**

Run: `mvn -pl easyfile-storage/easyfile-storage-local -am -Dtest=LocalImportStorageServiceImplTest test`  
Expected: FAIL

- [ ] **Step 3: Implement entities/mappers/service**

Copy structure from:
- `AsyncDownloadTask` / `AsyncDownloadRecord` / `AsyncDownloadTrigger`
- `LocalDownloadStorageServiceImpl` methods: `register`, `enableRunning`, `getRequestInfoByRegisterId`, `refreshExecuteProgress`, `complete`, `cancelUpload` → `cancelImport`, `autoRegisterTask`, `listExportResult` → `listImportResult`

CAS SQL must require previous status `none` when moving to `executing` (same as download `refreshUploadStatus`).

`getRequestInfoByRegisterId` rebuilds `ImportRequestInfo` from record columns (`import_code`, `oss_url`, `execute_param`, `resume_position`, …).

- [ ] **Step 4: Run tests — expect PASS**

Run: `mvn -pl easyfile-storage/easyfile-storage-local -am -Dtest=LocalImportStorageServiceImplTest test`  
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add easyfile-storage/easyfile-storage-local
git commit -m "$(cat <<'EOF'
feat(storage-local): implement local import storage service

EOF
)"
```

---

### Task 5: LocalImportTriggerServiceImpl

**Files:**
- Create: `easyfile-storage-local/src/main/java/com/openquartz/easyfile/storage/local/impl/LocalImportTriggerServiceImpl.java`
- Create: trigger mapper methods (or reuse patterns from `AsyncDownloadTriggerMapper`)
- Test: `.../LocalImportTriggerServiceImplTest.java` + optional SQL test like `AsyncDownloadTriggerMapperSqlTest`

**Interfaces:**
- Consumes: `ImportTriggerService`
- Produces: `trigger` / `startExecute` / `exeSuccess` / `exeFail` / compensate APIs

- [ ] **Step 1: Write failing test for startExecute CAS**

```java
@Test
public void startExecute_transitionsToExecuting() {
    // mock mapper.execute(registerId, triggerCount) returns 1 → true
}
```

- [ ] **Step 2: Run — expect FAIL**

Run: `mvn -pl easyfile-storage/easyfile-storage-local -am -Dtest=LocalImportTriggerServiceImplTest test`

- [ ] **Step 3: Implement by mirroring `LocalDownloadTriggerServiceImpl`**

Same method bodies with import table/mapper names.

- [ ] **Step 4: Run — expect PASS**

- [ ] **Step 5: Commit**

```bash
git add easyfile-storage/easyfile-storage-local
git commit -m "$(cat <<'EOF'
feat(storage-local): implement local import trigger service

EOF
)"
```

---

### Task 6: Streaming parsers + ImportParserFactory

**Files:**
- Create: `easyfile-core/src/main/java/com/openquartz/easyfile/core/parser/ImportRow.java`
- Modify: `easyfile-core/src/main/java/com/openquartz/easyfile/core/parser/ImportParser.java` — `Closeable`, `readPage(long startRow, int pageSize)`, `getTotalCount()`
- Create: `.../parser/ImportParserFactory.java`
- Create: `.../parser/StreamingExcelImportParser.java` (xlsx)
- Create: `.../parser/HssfExcelImportParser.java` (xls)
- Create: `.../parser/CsvImportParser.java`
- Modify or deprecate production use of `ExcelImportParser` (keep tests migrating to new API)
- Test: `.../parser/StreamingExcelImportParserTest.java`, `CsvImportParserTest.java`

**Interfaces:**
- Consumes: `@ExcelImportField` row types
- Produces: `ImportParserFactory.create(fileType, file, Class<T>)`

- [ ] **Step 1: Failing CSV test with temp file**

```java
@Test
public void csv_readPage_returnsRowNumbers() throws Exception {
    File f = File.createTempFile("imp", ".csv");
    Files.write(f.toPath(), Arrays.asList("name,age", "a,1", "b,2"));
    try (ImportParser<DemoRow> parser = ImportParserFactory.create("csv", f, DemoRow.class)) {
        Assert.assertEquals(2L, parser.getTotalCount());
        List<ImportRow<DemoRow>> page = parser.readPage(1, 10);
        Assert.assertEquals(1L, page.get(0).getRowNumber());
        Assert.assertEquals("a", page.get(0).getData().getName());
    }
}
```

- [ ] **Step 2: Run — expect FAIL**

Run: `mvn -pl easyfile-core -am -Dtest=CsvImportParserTest test`

- [ ] **Step 3: Implement factory + CSV + streaming xlsx + hssf**

Rules from spec:
- No per-page full workbook reopen for xlsx
- `ImportRow` holds `rowNumber`, `data`, `rawValues`, `parseError`
- Factory switch on normalized `fileType`

- [ ] **Step 4: Run CSV + Excel tests — PASS**

Run: `mvn -pl easyfile-core -am -Dtest=CsvImportParserTest,StreamingExcelImportParserTest,ExcelImportParserTest test`

- [ ] **Step 5: Commit**

```bash
git add easyfile-core/src/main/java/com/openquartz/easyfile/core/parser \
  easyfile-core/src/test/java/com/openquartz/easyfile/core/parser
git commit -m "$(cat <<'EOF'
feat(core): add streaming import parsers and factory

EOF
)"
```

---

### Task 7: Harden AbstractPagedImportExecutor (engine split)

**Files:**
- Modify: `easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/AbstractPagedImportExecutor.java`
- Modify: progress reporter to accept `ImportStatusEnum` if needed
- Test: `.../AbstractPagedImportExecutorValidationTest.java` (extend), create `AbstractPagedImportExecutorResumeTest.java`

**Interfaces:**
- Consumes: `ImportParserFactory`, `ImportStorageService.complete(..., ImportStatusEnum, ...)`
- Produces: resume rules + `PARTIAL_SUCCESS` completion

- [ ] **Step 1: Failing resume test**

```java
@Test
public void processBusinessFailure_doesNotAdvanceResume() {
    // stub parser two pages; processBusiness throws on page 1
    // verify storage.refreshExecuteProgress never called with resume >= page1 end
    // verify complete(..., FAIL, ...)
}
```

```java
@Test
public void mixedRowErrors_completePartialSuccess() {
    // validate fails half rows; processBusiness succeeds on valid
    // verify complete(..., PARTIAL_SUCCESS, ...) and result file URL set
}
```

- [ ] **Step 2: Run — expect FAIL**

- [ ] **Step 3: Implement loop per spec §10.3**

- Use factory parsers  
- Check cancel flag each page (read from storage or context atomic boolean set by cancel)  
- Advance resume only after successful business page  
- Map outcomes to `SUCCESS` / `PARTIAL_SUCCESS` / `FAIL`  
- Remove hardcoded `new ExcelImportParser`  
- Keep lock + idempotent manager behavior

- [ ] **Step 4: Run — PASS**

Run: `mvn -pl easyfile-core -am -Dtest=AbstractPagedImportExecutorResumeTest,AbstractPagedImportExecutorValidationTest test`

- [ ] **Step 5: Commit**

```bash
git add easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/AbstractPagedImportExecutor.java \
  easyfile-core/src/test/java/com/openquartz/easyfile/core/executor
git commit -m "$(cat <<'EOF'
feat(core): harden paged import engine for resume and partial success

EOF
)"
```

---

### Task 8: BaseImportExecutor.importResult + Async import handler family

**Files:**
- Create: `easyfile-core/src/main/java/com/openquartz/easyfile/core/context/ImportRequestContext.java`
- Modify: `easyfile-core/src/main/java/com/openquartz/easyfile/core/executor/BaseImportExecutor.java`
- Create: `.../executor/BaseAsyncImportHandler.java`
- Create: `.../executor/AsyncImportHandlerAdapter.java`
- Create: `.../executor/impl/DatabaseAsyncImportHandlerAdapter.java`
- Test: `.../executor/AsyncImportHandlerAdapterTest.java`

**Interfaces:**
- Consumes: `ImportStorageService`, `ImportTriggerService`, `BaseImportExecutor.process`
- Produces:

```java
// BaseImportExecutor defaults (AOP interceptor short-circuits before these run in production)
default Pair<Boolean, Long> importResult(ImportRequestContext context) {
    ImportContext importContext = ImportContext.builder()
            .sourceFileUrl(context.getSourceFileUrl())
            .fileName(context.getFileName())
            .fileType(context.getFileType())
            .businessUniqueKey(context.getBusinessUniqueKey())
            .executeParam(context.getExecuteParam())
            .registerId(context.getRegisterId())
            .build();
    process(importContext);
    return Pair.of(Boolean.FALSE, context.getRegisterId());
}

default void asyncCompleteCallback(ImportProcessResult result, ImportRequestContext context) {
}

// BaseAsyncImportHandler
void execute(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId);
ImportProcessResult handleResult(BaseImportExecutor<?> executor, ImportRequestContext context, Long registerId);
```

- [ ] **Step 1: Failing adapter test**

```java
@Test
public void handleResult_enableRunningFalse_skipsProcess() {
    // enableRunning → false; executor.process never called
}
```

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement adapters**

`AsyncImportHandlerAdapter.handleResult`:
1. `enableRunning`  
2. idempotent short-circuit if storage/idempotent manager says so  
3. build `ImportContext` from `getRequestInfoByRegisterId`  
4. `executor.process`  
5. `complete` already done inside process — adapter ensures trigger `exeSuccess`/`exeFail` when used from DB adapter  
6. invoke `asyncCompleteCallback`

`DatabaseAsyncImportHandlerAdapter.execute`: insert trigger only (mirror download).  
Claim loop methods `doTrigger`/`doCompensate` mirror `DatabaseAsyncFileHandlerAdapter`.

- [ ] **Step 4: Run — PASS**

- [ ] **Step 5: Commit**

```bash
git add easyfile-core/src/main/java/com/openquartz/easyfile/core/executor \
  easyfile-core/src/main/java/com/openquartz/easyfile/core/context/ImportRequestContext.java \
  easyfile-core/src/test/java/com/openquartz/easyfile/core/executor
git commit -m "$(cat <<'EOF'
feat(core): add importResult contract and async import handlers

EOF
)"
```

---

### Task 9: Starter — properties, ThreadPool + Schedule handlers, auto-config

**Files:**
- Modify: `.../properties/EasyFileImportProperties.java` — add `triggerType`, schedule period, maxTriggerCount, executingTimeoutSeconds, `syncPersist`, `retryFromResume`
- Create: `.../properties/ImportAsyncTriggerType.java` — `THREAD_POOL`, `DB_SCHEDULE`
- Create: `.../trigger/handler/DefaultAsyncImportHandler.java`
- Create: `.../trigger/handler/ScheduleTriggerAsyncImportHandler.java`
- Create: `.../autoconfig/DefaultAsyncImportHandlerAutoConfiguration.java`
- Create: `.../autoconfig/ScheduleAsyncImportHandlerAutoConfiguration.java`
- Modify: `EasyFileImportAutoConfiguration.java` — fix broken `new ImportServiceImpl()`; wire `ImportStorageService` properly for local/remote starters
- Modify: local/remote starter auto-config to expose `LocalImportStorageServiceImpl` / remote client beans
- Test: property binding test or context load smoke if module has Spring tests; otherwise unit-construct handlers

**Interfaces:**
- Consumes: Task 8 handlers
- Produces: conditional beans on `easyfile.import.trigger-type`

- [ ] **Step 1: Failing test — properties default**

```java
@Test
public void defaultTriggerType_isThreadPool() {
    Assert.assertEquals(ImportAsyncTriggerType.THREAD_POOL, new EasyFileImportProperties().getTriggerType());
}
```

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement properties + handlers + `@ConditionalOnProperty`**

YAML keys:
```yaml
easyfile.import.trigger-type: thread_pool # or db_schedule
easyfile.import.sync-persist: true
easyfile.import.retry-from-resume: true
easyfile.import.schedule-period-seconds: 10
easyfile.import.max-trigger-count: 3
easyfile.import.executing-timeout-seconds: 3600
```

`DefaultAsyncImportHandler` extends `AsyncImportHandlerAdapter`, overrides `execute` to submit to import executor thread pool (copy `DefaultAsyncFileHandler`).  
`ScheduleTriggerAsyncImportHandler` extends `DatabaseAsyncImportHandlerAdapter` (copy schedule download handler timing using import properties).

- [ ] **Step 4: Run — PASS + compile starter**

Run: `mvn -pl easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent -am -DskipTests compile`

- [ ] **Step 5: Commit**

```bash
git add easyfile-spring-boot-starter
git commit -m "$(cat <<'EOF'
feat(starter): wire import thread-pool and db-schedule handlers

EOF
)"
```

---

### Task 10: FileImportInterceptor + limiting + listeners

**Files:**
- Create: `starter/.../aop/FileImportInterceptor.java`
- Create: `core/.../intercept/ImportExecutorInterceptor.java` (if export has parallel package)
- Create: `core/.../intercept/ImportStartListener.java`, `ImportEndListener.java`
- Modify: AOP advisor registration (find where `FileExportInterceptor` is registered — same place for import)
- Modify: `BaseImportExecutor.importResult` documentation — real path is AOP
- Test: `starter/.../aop/FileImportInterceptorTest.java` with mocked storage/limiting/handler

**Interfaces:**
- Consumes: `LimitingService`, `ImportStorageService`, `BaseAsyncImportHandler`
- Produces: async `Pair(true, registerId)` / sync process path

- [ ] **Step 1: Failing interceptor test**

```java
@Test
public void asyncPath_registersAndExecutesHandler() throws Throwable {
    // enableAsync true → limiting called, register returns 9L, handler.execute called, result Pair(true, 9L)
}
```

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement interceptor mirroring `FileExportInterceptor`**

Sync path: if `syncPersist` register then `process` then end listener.  
Publish start/end listeners.  
Call `ImportLimitingRequest` on async path.

- [ ] **Step 4: Run — PASS**

- [ ] **Step 5: Commit**

```bash
git add easyfile-spring-boot-starter easyfile-core/src/main/java/com/openquartz/easyfile/core/intercept
git commit -m "$(cat <<'EOF'
feat(starter): add FileImportInterceptor with limit and listeners

EOF
)"
```

---

### Task 11: Server facade + RemoteImportStorageServiceImpl + notify

**Files:**
- Rewrite: `easyfile-server/.../ImportServiceImpl.java` — real register/status/progress/execute using storage+trigger
- Remove/replace: `DefaultImportStorageServiceImpl` ownership (server uses local impl bean or shared JDBC only if server deployment embeds local module — match how download is wired on server)
- Create: `easyfile-storage-remote/.../RemoteImportStorageServiceImpl.java`
- Wire remote HTTP routes on `EasyFileClient` / server controller parity with download remote calls
- Hook notify on complete (server `NotifyService` or completion path used by download)
- Test: `ImportServiceImplTest`, `RemoteImportStorageServiceImplTest` (enableRunning null-safety like download)

**Interfaces:**
- Consumes: storage/trigger contracts
- Produces: non-stub `/easyfile/import/*`

- [ ] **Step 1: Failing test — register returns persisted id**

```java
@Test
public void registerImport_delegatesToStorageAndTriggers() {
    // mock storage.register → 5L; verify triggerService.trigger called when async
}
```

- [ ] **Step 2: Run — FAIL**

- [ ] **Step 3: Implement real facade; delete mock status/progress**

`queryImportStatus` / `queryImportProgress` read from storage `getRequestInfoByRegisterId` / list DTO.  
`retry`/`revoke` delegate to storage.  
Remote client methods map to same REST paths as `ImportController`.

Notify: on `complete` success-like/fail, if `enableNotify`, call existing notify sender with import-specific message.

- [ ] **Step 4: Run module tests — PASS**

Run: `mvn -pl easyfile-server,easyfile-storage/easyfile-storage-remote -am -Dtest=ImportServiceImplTest,RemoteImportStorageServiceImplTest test`

- [ ] **Step 5: Commit**

```bash
git add easyfile-server easyfile-storage/easyfile-storage-remote
git commit -m "$(cat <<'EOF'
feat: complete import server facade and remote storage client

EOF
)"
```

---

### Task 12: Examples + Admin API compatibility + end-to-end verification

**Files:**
- Create: `easyfile-example/easyfile-example-local/.../controller/StudentImportController.java` (and remote counterpart)
- Modify: UI import response mapping if status display assumes COMPLETED/FAILED strings → accept `success`/`partial_success`/`fail`/`revoke`
- Modify: `AutoRegisteredImportTaskListener` ensure it hits real `autoRegisterTask` inserting `ef_async_import_task`
- Test: example compile; optional `@SpringBootTest` smoke if present

**Interfaces:**
- Consumes: `importResult` AOP path
- Produces: demo upload → import → poll → result file

- [ ] **Step 1: Failing compile of controller calling importResult**

Add controller that does not compile until `ImportRequestContext` wired.

- [ ] **Step 2: Implement controller**

```java
@PostMapping("/student/import")
public Pair<Boolean, Long> importStudents(@RequestParam String fileUrl,
                                          @RequestParam(required = false) String businessKey) {
    ImportRequestContext ctx = new ImportRequestContext();
    ctx.setSourceFileUrl(fileUrl); // map to ossUrl in register request inside interceptor
    ctx.setFileType("xlsx");
    ctx.setBusinessUniqueKey(businessKey);
    return studentImportExecutor.importResult(ctx);
}
```

- [ ] **Step 3: Fix Admin/UI status labels for new enum codes**

Search UI for `COMPLETED`/`FAILED`/`CANCELLED` import status and map to new codes.

- [ ] **Step 4: Verification commands**

```bash
mvn -pl easyfile-common,easyfile-core,easyfile-storage/easyfile-storage-api,easyfile-storage/easyfile-storage-local,easyfile-storage/easyfile-storage-remote,easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent,easyfile-server -am test
mvn -pl easyfile-example/easyfile-example-local -am -DskipTests compile
```

Expected: all tests PASS; example compiles.

- [ ] **Step 5: Commit**

```bash
git add easyfile-example easyfile-ui
git commit -m "$(cat <<'EOF'
feat(example): add import demo API and admin status compatibility

EOF
)"
```

- [ ] **Step 6: Final acceptance smoke (manual checklist)**

1. App starts with `@FileImportExecutor` → row in `ef_async_import_task`  
2. `importResult` async → `registerId`, record `executing`→`success`  
3. `trigger-type=db_schedule` recovers after restart  
4. Sync `enableAsync=false` completes with persisted record  
5. Bad rows → result file + `partial_success`  
6. Duplicate `businessUniqueKey`+hash → prior result  
7. Limiting rejects excess  
8. Remote mode register on server, execute on example-remote node  

---

## Spec Coverage Self-Review

| Spec requirement | Task |
|---|---|
| Dual entry AOP + HTTP | 10, 11, 12 |
| ThreadPool + DB Trigger | 8, 9 |
| Sync + async | 8, 10 |
| ImportStatusEnum + PARTIAL_SUCCESS | 1, 7 |
| Full ImportStorageService | 2, 4 |
| ImportTriggerService | 2, 5 |
| SQL three tables | 3 |
| Local storage move from server | 4, 11 |
| Remote storage client | 11 |
| Streaming xlsx/csv parsers | 6 |
| Engine resume/idempotent/result file | 7 |
| Limiting / notify / listeners | 10, 11 |
| Examples + Admin compatibility | 12 |
| No MQ / no UI redesign / no export merge | Global Constraints |

## Placeholder / consistency notes (resolved in plan)

- `ossUrl` remains register field; `ImportRequestContext.sourceFileUrl` maps to it in interceptor.
- Cancel code `revoke` matches export `UploadStatusEnum.CANCEL` code style.
- Deprecated `markExecuting`/`refreshProgress` bridge old call sites until Task 7 cleans them.

# EasyFile Big Data Import Architecture Design

## Background

EasyFile already has a mature async export architecture:

- `BaseDownloadExecutor` defines user-facing export behavior.
- `@FileExportExecutor` and `FileExportExecutorSupport` discover and register executors.
- `DownloadStorageService` owns task registration, record state, progress, result query, cancel, and request recovery.
- `DownloadTriggerService` owns DB-backed async trigger state, retry, expiration, compensation, and archive.
- `AsyncFileHandlerAdapter` and `DatabaseAsyncFileHandlerAdapter` execute export tasks and report final state.

The import implementation currently in the working tree has useful core pieces, including `BaseImportExecutor`,
`AbstractPagedImportExecutor`, `ImportContext`, `ImportParser`, `ImportStorageService`, idempotent import management,
and import progress reporting. The main gap is architectural integration: the import path should use the same storage,
trigger, auto-registration, and starter style as export instead of remaining a partially separate execution stack.

## Goals

1. Support large file import without loading all rows into memory.
2. Keep import architecture symmetric with the existing export architecture.
3. Support async execution, DB trigger compensation, retry, progress, cancel, and result query.
4. Support resumable import using a clear `resumePosition` contract.
5. Support idempotent import by `businessUniqueKey` and file hash when configured.
6. Generate a result file for failed rows or partial success.
7. Keep extension points small and consistent with existing EasyFile style.

## Non-Goals

1. Do not introduce a new batch framework in the first version.
2. Do not require MQ for import. MQ can be added later by following the existing async handler extension model.
3. Do not replace the existing export implementation.
4. Do not redesign the admin UI in this phase.

## Recommended Approach

Use the same DB trigger model as async export.

Import should follow this high-level flow:

```text
register import record
        |
create import trigger
        |
scheduled handler claims trigger
        |
recover ImportRequestInfo from storage
        |
resolve BaseImportExecutor by importCode
        |
execute paged import
        |
report progress and resume position
        |
write result file when needed
        |
complete record and trigger
```

This approach matches the existing codebase better than a direct thread-pool implementation. It also preserves recovery
after application restart because trigger state is stored in the database.

## Module Boundaries

### easyfile-common

Common request, response, enum, and simple data classes live here.

Add:

- `com.openquartz.easyfile.common.request.ImportRegisterRequest`
- `com.openquartz.easyfile.common.request.ImportResultQueryRequest`
- `com.openquartz.easyfile.common.request.ImportTriggerRequest`
- `com.openquartz.easyfile.common.request.CancelImportRequest`
- `com.openquartz.easyfile.common.request.AutoImportTaskRegisterRequest`
- `com.openquartz.easyfile.common.response.ImportResult`
- `com.openquartz.easyfile.common.response.ImportTriggerEntry`
- `com.openquartz.easyfile.common.response.ImportRequestInfo`
- `com.openquartz.easyfile.common.response.ErrorColumnInfo`
- `com.openquartz.easyfile.common.response.CancelImportResult`
- `com.openquartz.easyfile.common.dictionary.ImportStatusEnum`

Current files already added under `easyfile-common` should be kept there when they are generic request/response types.
Avoid putting common request classes under `easyfile-core`.

### easyfile-core

Core import execution logic lives here.

Keep and refine:

- `BaseImportExecutor<T>`
- `AbstractPagedImportExecutor<T>`
- `ImportContext`
- `ImportDataContext<T>`
- `ImportProcessContext`
- `ImportParser<T>`
- `ExcelImportParser<T>`
- `ImportExecuteProcessProbe`
- `ImportExecuteProcessReporter`
- `ImportExecuteProcessReporterImpl`
- `ImportResultFileGenerator`
- `IdempotentImportManager`
- `RedisIdempotentImportManager`

Add:

- `FileImportExecutor`
- `FileImportExecutorSupport`
- `BaseAsyncImportHandler`
- `AsyncImportHandlerAdapter`
- `DatabaseAsyncImportHandlerAdapter`
- optional `ImportExecutorInterceptor` and import listener hooks after the first storage-backed version works

The core module should not directly contain MyBatis mappers or server-local JDBC implementations.

### easyfile-storage-api

Storage-facing import interfaces live here.

Expand `ImportStorageService` from progress-only methods to the full import storage contract:

```java
public interface ImportStorageService {

    boolean enableRunning(Long registerId);

    Long register(ImportRegisterRequest request);

    void autoRegisterTask(AutoImportTaskRegisterRequest request);

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

    Pagination<ImportResult> listImportResult(ImportResultQueryRequest request);
}
```

Add `ImportTriggerService` mirroring `DownloadTriggerService`:

```java
public interface ImportTriggerService {

    void trigger(ImportTriggerRequest request);

    List<ImportTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
                                                  Integer triggerOffset);

    List<ImportTriggerEntry> getTriggerRegisterId(Integer lookBackHours, Integer maxTriggerCount,
                                                  Integer minReaperSeconds, Integer triggerOffset);

    List<ImportTriggerEntry> getTriggerCompensate(Integer lookBackHours, Integer maxTriggerCount,
                                                  Integer triggerOffset);

    ImportTriggerEntry getTriggerRegisterId(Long registerId, Integer maxTriggerCount);

    boolean startExecute(Long registerId, Integer triggerCount);

    void exeSuccess(Long registerId);

    void exeFail(Long registerId);

    void enterWaiting(Long registerId);

    void handleExpirationTrigger(Integer maxExpireSeconds);

    void handleWaitingExpirationTrigger(Integer maxWaitingSeconds);

    void archiveHistoryTrigger(Integer archiveHours, Integer maxTriggerCount);
}
```

### easyfile-storage-local

Local DB persistence belongs here, not in `easyfile-server`.

Add:

- `AsyncImportTask`
- `AsyncImportRecord`
- `AsyncImportTrigger`
- `AsyncImportTaskMapper`
- `AsyncImportRecordMapper`
- `AsyncImportTriggerMapper`
- `LocalImportStorageServiceImpl`
- `LocalImportTriggerServiceImpl`
- import query/update condition classes
- import converter classes

Move or replace `easyfile-server/src/main/java/.../DefaultImportStorageServiceImpl` with the storage-local
implementation. The server module can expose remote APIs later, but it should not own the default local storage
implementation.

### easyfile-spring-boot-starter

Starter integration should match export integration.

Modify `EasyFileBeanEnhancePostProcessor`:

- Keep existing export executor registration.
- Add import executor registration when a bean is annotated with `@FileImportExecutor`.
- Validate that annotated beans implement `BaseImportExecutor`.
- Enforce unique `importCode`.

Add:

- `AutoRegisteredImportTaskListener`
- `EasyFileImportProperties` or import fields inside the existing EasyFile property group
- local DB import handler auto-configuration

The first version can reuse existing async handler timing properties if the import and export schedules are acceptable.
If import requires separate tuning, add import-specific schedule properties.

## Public Extension Model

Users should implement import tasks like this:

```java
@Component
@FileImportExecutor(value = "StudentImport", desc = "Student Import")
public class StudentImportExecutor extends AbstractPagedImportExecutor<StudentImportRow> {

    @Override
    protected Class<StudentImportRow> getTargetClass() {
        return StudentImportRow.class;
    }

    @Override
    public ValidationResult validate(ImportDataContext<StudentImportRow> dataContext) {
        return ValidationResult.success();
    }

    @Override
    public ProcessResult processBusiness(List<StudentImportRow> data, ImportProcessContext processContext) {
        studentService.batchUpsert(data);
        return ProcessResult.success(data.size());
    }
}
```

The executor API should expose only business concepts:

- `validate` checks page data.
- `processBusiness` persists valid rows.
- `getPageSize` tunes memory and transaction size.
- `supportResumable` declares whether resume is safe.
- `enableAsync` allows sync execution later, but async DB trigger is the default for large imports.

## Data Model

### ef_async_import_task

```sql
CREATE TABLE ef_async_import_task
(
    id             BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    task_code      VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入任务编码',
    task_desc      VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '任务描述',
    app_id         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '归属系统 APP ID',
    unified_app_id VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '统一 APP ID',
    enable_status  TINYINT(3)          NOT NULL DEFAULT 0 COMMENT '启用状态',
    version        INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by      VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by      VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    is_deleted     BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (id),
    UNIQUE KEY uniq_app_id_task_code (task_code, app_id) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '异步导入任务';
```

### ef_async_import_record

```sql
CREATE TABLE ef_async_import_record
(
    id                   BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'id',
    import_task_id       BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '导入任务ID',
    app_id               VARCHAR(50)         NOT NULL DEFAULT '' COMMENT 'app ID',
    import_code          VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入code',
    import_status        VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '导入状态',
    source_file_url      VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '源文件路径',
    source_file_name     VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '源文件名',
    file_type            VARCHAR(20)         NOT NULL DEFAULT '' COMMENT '文件类型',
    file_hash            VARCHAR(64)         NOT NULL DEFAULT '' COMMENT '文件哈希',
    business_unique_key  VARCHAR(128)        NOT NULL DEFAULT '' COMMENT '业务幂等键',
    result_file_url      VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '结果文件路径',
    total_count          BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '总行数',
    success_count        BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '成功行数',
    fail_count           BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '失败行数',
    execute_process      INT(3)              NOT NULL DEFAULT 0 COMMENT '执行进度',
    resume_position      BIGINT(20)          NOT NULL DEFAULT 0 COMMENT '断点续传位置',
    notify_enable_status TINYINT(3)          NOT NULL DEFAULT 0 COMMENT '通知启用状态',
    notify_email         VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '通知邮箱',
    max_server_retry     INT(3)              NOT NULL DEFAULT 0 COMMENT '最大服务重试',
    current_retry        INT(3)              NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    execute_param        TEXT                NULL COMMENT '导入执行参数',
    error_msg            VARCHAR(512)        NOT NULL DEFAULT '' COMMENT '异常信息',
    last_execute_time    DATETIME            NULL COMMENT '最新执行时间',
    version              INT(10)             NOT NULL DEFAULT 0 COMMENT '版本号',
    create_time          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time          TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by            VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '创建人',
    update_by            VARCHAR(50)         NOT NULL DEFAULT '' COMMENT '更新人',
    locale               VARCHAR(16)         NOT NULL DEFAULT '' COMMENT '语言',
    PRIMARY KEY (id),
    KEY idx_import_operate_by (create_by) USING BTREE,
    KEY idx_operator_record (create_by, app_id, create_time),
    KEY idx_import_status (import_status, id),
    KEY idx_business_unique_key (business_unique_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '异步导入记录';
```

### ef_async_import_trigger

```sql
CREATE TABLE ef_async_import_trigger
(
    id                BIGINT(20)  NOT NULL AUTO_INCREMENT COMMENT 'Id',
    register_id       BIGINT(20)  NOT NULL DEFAULT -1 COMMENT '注册ID',
    trigger_status    VARCHAR(50) NOT NULL DEFAULT '' COMMENT '触发状态',
    start_time        DATETIME    NOT NULL COMMENT '开始时间',
    last_execute_time DATETIME    NOT NULL COMMENT '最新执行时间',
    trigger_count     INT(3)      NOT NULL DEFAULT 0 COMMENT '触发次数',
    creating_owner    VARCHAR(50) NOT NULL DEFAULT '' COMMENT '触发者',
    processing_owner  VARCHAR(50) NOT NULL DEFAULT '' COMMENT '执行者',
    UNIQUE KEY ux_register_id (register_id),
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '导入触发执行';
```

## Status Model

Add `ImportStatusEnum` instead of reusing `UploadStatusEnum`.

Recommended values:

- `NONE`: registered but not started.
- `EXECUTING`: parser, validation, or business processing is running.
- `SUCCESS`: all rows processed successfully.
- `PARTIAL_SUCCESS`: valid rows processed and failed rows written to result file.
- `FAIL`: task failed before producing a valid result.
- `CANCEL`: user cancelled before execution.
- `INVALID`: source file or task expired.

`PARTIAL_SUCCESS` is important because import often accepts valid rows while reporting row-level failures.

## Execution Flow

### Registration

1. Client uploads a source file and gets a URL.
2. Client calls import register with `ImportRegisterRequest`.
3. `ImportStorageService.register` validates that `importCode + appId` exists in `ef_async_import_task`.
4. Storage creates an `ef_async_import_record` with `ImportStatusEnum.NONE`.
5. The import handler calls `ImportTriggerService.trigger(registerId)`.

### Trigger Claim

1. `DatabaseAsyncImportHandlerAdapter` scans `ef_async_import_trigger`.
2. `ImportTriggerService.startExecute` atomically changes trigger status to `EXECUTING`.
3. Handler recovers `ImportRequestInfo` from `ImportStorageService.getRequestInfoByRegisterId`.
4. Handler resolves executor by `FileImportExecutorSupport.get(importCode)`.
5. Handler sets the record to running using `ImportStorageService.enableRunning`.

### Import Processing

1. `AbstractPagedImportExecutor` downloads the source file using `DownloadService`.
2. It calculates file hash and writes it to storage.
3. It creates a parser based on `fileType`.
4. It determines `totalCount`.
5. It resumes from `resumePosition` when supported.
6. For each page:
   - parse rows
   - validate rows
   - pass valid rows to `processBusiness`
   - collect row-level errors
   - update progress and resume position after successful business commit
7. It generates a result file when failed rows exist.
8. It completes record as `SUCCESS`, `PARTIAL_SUCCESS`, or `FAIL`.
9. It marks the idempotent result when the import completed successfully or partially successfully.

### Compensation

The import trigger service should copy the export trigger compensation behavior:

- Mark long-running `EXECUTING` triggers as `FAIL`.
- Re-enter retryable failed triggers until `maxTriggerCount`.
- Archive successful and permanently failed triggers.

## Parser Design

The current `ExcelImportParser` should be changed before production use.

Issues to address:

1. `SXSSFWorkbook` is a streaming writer API and should not be used as the primary reader.
2. The same `InputStream` cannot be safely reused after `getTotalCount`.
3. Reopening a workbook for every page causes repeated full-file parsing.
4. Validation errors need row numbers and column metadata.

Recommended parser contract:

```java
public interface ImportParser<T> extends Closeable {

    long getTotalCount();

    List<ImportRow<T>> readPage(int pageNum, int pageSize);

    String getFileType();
}
```

`ImportRow<T>` should keep:

- row number
- parsed data
- original row values when needed for result file generation

First implementation can use Apache POI event/SAX reading for `.xlsx` and `HSSFWorkbook` for smaller `.xls`.
CSV can be added as a second parser because it is naturally streaming and useful for very large files.

## Resume Contract

`resumePosition` means the last source row number that has been durably handled.

Rules:

1. Update `resumePosition` only after `processBusiness` succeeds for valid rows in the page.
2. If a page has validation errors but valid rows are committed, resume position can advance to the end of that page.
3. If `processBusiness` fails for the page, do not advance resume position for that page.
4. Executors that cannot guarantee idempotent page writes should return `supportResumable() == false`.

This keeps retry behavior understandable and prevents accidental duplicate writes.

## Idempotency

Use `businessUniqueKey` as the primary idempotency key.

Recommended behavior:

- If `businessUniqueKey` is blank, idempotency is disabled for that import.
- If present, acquire a distributed import lock before execution.
- If a completed result already exists for the key, return the existing result and mark the new record as idempotent.
- Store idempotent results only after final status is `SUCCESS` or `PARTIAL_SUCCESS`.
- Failed imports should not permanently block retry by the same key.

`fileHash` can be used as an optional guard:

- If both `businessUniqueKey` and `fileHash` exist, the idempotent result should match the same file hash.
- A same business key with a different file hash should be rejected or treated as a new version based on a future option.

The first version should reject mismatched file hashes because it is safer.

## Error Handling

There are two error levels:

1. Task-level errors: source file missing, parser creation failure, unsupported file type, storage failure, unexpected exception.
2. Row-level errors: validation failure, conversion failure, business rejection for a specific row.

Task-level errors complete the record as `FAIL`.

Row-level errors are accumulated into a result file. If at least one row succeeds and at least one row fails, complete as
`PARTIAL_SUCCESS`. If all rows fail due to row-level validation, complete as `PARTIAL_SUCCESS` with `successCount = 0`
only when the task itself ran normally and produced a result file. If the parser cannot read the file at all, complete as
`FAIL`.

## Result File

`ImportResultFileGenerator` should generate a downloadable result file with:

- source row number
- business key or row identifier when available
- column name
- invalid value
- error message

The generator should use existing `UploadService` so local and remote deployments share the same file storage extension
model as export.

## Migration From Current Working Tree

The current working tree already added several useful files. Keep the core ideas, but align locations and contracts:

1. Keep `BaseImportExecutor`, `AbstractPagedImportExecutor`, context classes, parser interfaces, progress reporter,
   result generator, and idempotent manager in `easyfile-core`.
2. Move request/response classes from `easyfile-core/request` to `easyfile-common/request` if any remain there.
3. Remove MyBatis/JPA/storage implementation classes from `easyfile-core`.
4. Move `DefaultImportStorageServiceImpl` from `easyfile-server` to `easyfile-storage-local` as
   `LocalImportStorageServiceImpl`.
5. Add local entities and mappers beside download local entities and mappers.
6. Add `FileImportExecutorSupport` and enhance `EasyFileBeanEnhancePostProcessor`.
7. Add auto-registration listener for import tasks.
8. Add SQL DDL to `sql/storage.sql`.
9. Replace parser implementation with a single-pass or streaming reader before calling the implementation production-ready.

## Testing Strategy

Unit tests:

- `FileImportExecutorSupport` duplicate registration and lookup.
- `LocalImportStorageServiceImpl.register` creates `NONE` records and rejects missing tasks.
- `LocalImportStorageServiceImpl.enableRunning` atomically changes `NONE` to `EXECUTING`.
- `LocalImportTriggerServiceImpl` status transitions match download trigger behavior.
- `AbstractPagedImportExecutor` advances resume position only after successful page processing.
- `AbstractPagedImportExecutor` reports `PARTIAL_SUCCESS` for mixed success and failure.
- Parser tests for string, number, boolean, blank cell, row number, and conversion failure.

Integration tests:

- Register an import, trigger it, execute through `DatabaseAsyncImportHandlerAdapter`, and complete successfully.
- Restart-safe behavior by inserting trigger rows and letting the handler claim them.
- Failed page retry with `resumePosition`.
- Idempotent duplicate import returns the previous result.

Compile verification:

- `mvn -pl easyfile-common,easyfile-core,easyfile-storage/easyfile-storage-api,easyfile-storage/easyfile-storage-local,easyfile-spring-boot-starter/easyfile-spring-boot-starter-parent -am test`

## Open Decisions

1. Whether CSV support is included in the first implementation or follows after Excel.
2. Whether import should share export async scheduling properties or have separate import-specific properties.
3. Whether same `businessUniqueKey` with a different `fileHash` should reject or create a new import version.

Recommended defaults:

1. Excel first, CSV second.
2. Add import-specific properties only for schedule period, max trigger count, and timeout.
3. Reject different file hash for the same business key in the first version.

## Acceptance Criteria

1. Users can define an import executor with `@FileImportExecutor`.
2. Import executors are auto-registered into import task storage.
3. A registered import creates a DB trigger and survives application restart.
4. The DB handler can claim and execute pending import triggers.
5. Large files are processed page by page without retaining all rows in memory.
6. Progress and resume position are persisted during execution.
7. Mixed row-level failures produce a result file and `PARTIAL_SUCCESS`.
8. Task-level failures complete as `FAIL` and preserve a clear error message.
9. Duplicate imports with the same idempotency key return the stored result.
10. Local storage implementation follows the existing download storage style.

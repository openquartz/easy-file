# EasyFile Async Import Architecture Design (Revised)

> Status: Draft pending user review  

> Date: 2026-08-08  
> Supersedes: [`2026-05-23-big-data-import-design.md`](./2026-05-23-big-data-import-design.md) for orchestration and delivery scope  
> Related: [`2026-05-23-import-admin-design.md`](./2026-05-23-import-admin-design.md) (Admin UI not redesigned in this phase)

## 1. Background

EasyFile export is a mature async task system:

- `@FileExportExecutor` + `BaseDownloadExecutor` + `exportResult`
- `FileExportInterceptor` for limiting, register, async dispatch
- `DownloadStorageService` / `DownloadTriggerService`
- `AsyncFileHandlerAdapter` family (ThreadPool / DB schedule / MQ)
- Progress, cancel, notify, cache, listeners

Import today has a strong **in-process engine** (`AbstractPagedImportExecutor`, parser, idempotency, result file, progress probe) and partial server/admin APIs, but the **task orchestration spine is incomplete**:

- `ImportServiceImpl.register` / `execute` / status query are stubbed or mocked
- No `FileImportInterceptor`, no `BaseAsyncImportHandler` family
- No `ImportTriggerService` / local trigger persistence
- `ImportStorageService` lacks `register` / `enableRunning` / request recovery
- Default JDBC storage lives in `easyfile-server` instead of `easyfile-storage-local`
- No remote import storage client
- No streaming production parser; example has no HTTP import entry

This revision keeps the May 2026 direction (align import with export’s task model) and expands it to **export-level parity** for developer experience and operational completeness.

## 2. Goals

1. **Ease of use:** annotation + thin business methods + `importResult`, symmetric with export.
2. **High abstraction:** users only implement `validate` / `processBusiness`; framework owns I/O, lifecycle, trigger, progress, notify.
3. **Generality:** sync + async; ThreadPool + DB Trigger; Excel (xlsx/xls) + CSV; local + remote storage.
4. **Export task-model fit:** register → handler → progress/complete → query/cancel/retry/notify.
5. **Complete flows:** dual entry (AOP + HTTP), limiting, listeners/interceptors, result file, idempotency, resume, examples; Admin APIs remain usable without UI redesign.
6. **Large-file safety:** page-based processing with streaming parsers; no full-file row materialization.

## 3. Non-Goals

1. Do not merge import/export into one physical task/record/trigger schema.
2. Do not force-refactor export onto the new shared lifecycle template in this phase.
3. Do not implement MQ import handler (SPI reserved only).
4. Do not redesign Admin UI pages.
5. Do not introduce an external batch framework (Spring Batch, etc.).

## 4. Decisions (from design workshop)

| Topic | Decision |
|---|---|
| Relation to May 2026 spec | Revise and supersede orchestration/scope; keep useful DDL/engine ideas |
| User entry | **Dual entry:** AOP (`importResult`) + Server/Remote HTTP |
| Async engines (v1) | **ThreadPool + DB Trigger**, isomorphic to export; MQ later |
| Sync path | **First-class:** `enableAsync=false` runs in-process; default also persists record |
| Delivery scope | **Export-level parity** (limit, notify, listeners, remote, streaming parser, examples) |
| Architecture style | Import as first-class mirror of export + light shared lifecycle template |
| Same `businessUniqueKey` + different `fileHash` | **Reject** |
| CSV | **In v1** with Excel |
| Import schedule properties | **Separate** from export schedule properties |

## 5. Recommended Approach

**Import is a first-class mirror of export; domain models stay separate; orchestration patterns are shared.**

```text
                    ┌─────────────────────────────────────┐
                    │     Dual entry                      │
                    │  AOP: importResult / importSync     │
                    │  HTTP: /easyfile/import/*           │
                    └───────────────┬─────────────────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
        LimitingService    ImportStorageService    ImportTriggerService
                                    │
                          BaseAsyncImportHandler
                     ┌──────────────┴──────────────┐
                     ▼                             ▼
          DefaultAsyncImportHandler     DatabaseAsyncImportHandlerAdapter
               (ThreadPool)                  (DB schedule claim)
                     └──────────────┬──────────────┘
                                    ▼
                      AbstractPagedImportExecutor
              download → parse → validate → processBusiness
              → progress/resume → result file → complete
```

Rejected alternatives:

- **Strict 1:1 copy with no shared template:** faster short-term, doubles drift risk.
- **Unified `AsyncFileTask` meta-model:** highest abstraction, forces export migration and table changes — out of scope.

## 6. Module Boundaries

| Module | Responsibility |
|---|---|
| `easyfile-common` | `ImportStatusEnum`, register/cancel/query DTOs, `ImportRequestInfo`, limiting request types |
| `easyfile-core` | `@FileImportExecutor`, `FileImportExecutorSupport`, `BaseImportExecutor`, paged engine, parser SPI/impls, async import handlers, interceptors/listeners, result file, idempotency; optional light `AbstractAsyncTaskLifecycle` |
| `easyfile-storage-api` | Full `ImportStorageService`, `ImportTriggerService` |
| `easyfile-storage-local` | Entities/mappers + `LocalImportStorageServiceImpl` + `LocalImportTriggerServiceImpl` |
| `easyfile-storage-remote` | `RemoteImportStorageServiceImpl` (+ remote trigger client if required by export parity) |
| `easyfile-server` | HTTP facade, notify wiring; **must not** own default local JDBC storage |
| `easyfile-spring-boot-starter` | Bean registration, AOP, ThreadPool/DB handler auto-config, auto-register listener, import properties |
| `easyfile-example` | Upload → `importResult` → poll → result file (local + remote) |
| `easyfile-ui` | Keep existing admin APIs compatible (`PARTIAL_SUCCESS`); no page redesign |

### Boundary rules

1. Users write only business concepts in executors.
2. Domain tables/APIs remain Import-specific; lifecycle template may be shared later with export.
3. Local vs Remote is a storage-mode switch; executors still run in the business application process (same as export remote mode).
4. Sync is first-class and by default persists a record for audit/admin parity.

## 7. Public Developer API

### 7.1 Minimal user code

```java
@Component
@FileImportExecutor(value = "StudentImport", desc = "学生导入",
        enableNotify = true, maxServerRetry = 3)
public class StudentImportExecutor extends AbstractPagedImportExecutor<StudentImportRow> {

    @Override
    protected Class<StudentImportRow> getTargetClass() {
        return StudentImportRow.class;
    }

    @Override
    public ValidationResult validate(ImportDataContext<StudentImportRow> ctx) {
        return ValidationResult.success();
    }

    @Override
    public ProcessResult processBusiness(List<StudentImportRow> data,
                                         ImportProcessContext ctx) {
        studentService.batchUpsert(data);
        return ProcessResult.success(data.size());
    }
}
```

### 7.2 Invocation

```java
Pair<Boolean, Long> outcome = studentImportExecutor.importResult(importRequestContext);
// async: (true, registerId)
// sync:  (false, registerIdOrNull) + ImportProcessResult available via return channel / holder
```

Upload is **outside** the executor: client uploads via `UploadService` (or business upload) to obtain `sourceFileUrl`, then calls `importResult`.

### 7.3 Contract layers

| Layer | API | Owner |
|---|---|---|
| Business extension | `validate`, `processBusiness`, `getPageSize`, `supportResumable`, `enableAsync` | User |
| Unified entry | `importResult`, `importSync` (defaults on `BaseImportExecutor`) | Framework + AOP |
| Engine | `process(ImportContext)` | `AbstractPagedImportExecutor`; called by handler/sync interceptor |
| Orchestration | `BaseAsyncImportHandler.execute` | Framework |

`process` remains, but **business code should not call it directly** (bypasses limit/register/listeners).

### 7.4 `ImportRequestContext`

Symmetric to `DownloaderRequestContext`:

- `sourceFileUrl`, `sourceFileName`, `fileType`
- `businessUniqueKey` (optional)
- `executeParam` (map/JSON)
- `notifyEmail`, `locale`, `operator`
- `importCode` optional at call site (resolved from annotation/registry)

### 7.5 AOP: `FileImportInterceptor`

```text
if import disabled → proceed

publish ImportStart

if executor.enableAsync(ctx):
    limitingService.limiting(...)
    registerId = importStorageService.register(...)
    asyncImportHandler.execute(executor, ctx, registerId)
    return Pair(true, registerId)
else:
    registerId = register(...) if sync-persist enabled (default true)
    result = executor.process(importContext)
    publish ImportEnd
    return Pair(false, registerId)
```

### 7.6 HTTP / Remote entry

Server endpoints under `/easyfile/import`:

- `POST /register`
- `GET /status/{id}`, `GET /progress/{id}`
- `POST /list`, `POST /retry/{id}`, `POST /revoke/{id}`
- `GET /result-file/{id}`
- `GET /app-tree`

Remote client implements the same `ImportStorageService` contract against server. Execution still happens on nodes that host the matching `@FileImportExecutor` bean.

### 7.7 Annotation & auto-register

`@FileImportExecutor`: `value`, `desc`, `enableNotify`, `maxServerRetry`.

Starter registers beans into `FileImportExecutorSupport` and `AutoRegisteredImportTaskListener` calls real `ImportStorageService.autoRegisterTask` → `ef_async_import_task`.

### 7.8 Export API mirror

| Export | Import |
|---|---|
| `@FileExportExecutor` | `@FileImportExecutor` |
| `exportResult` | `importResult` |
| `FileExportInterceptor` | `FileImportInterceptor` |
| `asyncCompleteCallback` | `asyncCompleteCallback(ImportProcessResult, ctx)` |
| generate + upload file | consume source + optional result-file upload |

## 8. Status Model

### 8.1 `ImportStatusEnum`

Do **not** reuse `UploadStatusEnum` (export’s `UPLOADING` does not apply).

| Status | Meaning |
|---|---|
| `NONE` | Registered, not started |
| `EXECUTING` | Download/parse/validate/business/result-upload in progress |
| `SUCCESS` | All rows succeeded |
| `PARTIAL_SUCCESS` | Task finished with row-level failures (includes successCount=0 if result file produced) |
| `FAIL` | Task-level failure |
| `CANCEL` | Cancelled |
| `INVALID` | Expired / invalidated |

Transition sketch:

```text
NONE → EXECUTING → SUCCESS | PARTIAL_SUCCESS | FAIL | CANCEL
NONE → CANCEL
* → INVALID (maintenance)
```

`enableRunning` performs CAS `NONE → EXECUTING`.

### 8.2 Cooperative cancel

- `NONE`: cancel immediately → `CANCEL`
- `EXECUTING`: set cancel flag; paged loop checks each page boundary and exits → `CANCEL`

### 8.3 Trigger status

Mirror export trigger semantics (`INIT → WAITING → EXECUTING → SUCCESS | FAIL`) via `ImportTriggerService`, copying compensation/expire/archive behavior from `DownloadTriggerService`.

ThreadPool handler does not depend on trigger scanning; DB schedule handler does. (Same split as export.)

## 9. Storage & Data Model

### 9.1 `ImportStorageService` (full contract)

```java
boolean enableRunning(Long registerId);
Long register(ImportRegisterRequest request);
void autoRegisterTask(AutoTaskRegisterRequest request);
ImportRequestInfo getRequestInfoByRegisterId(Long registerId);

void refreshExecuteProgress(Long registerId, Integer process,
                            Long resumePosition, ImportStatusEnum nextStatus);
void resetExecuteProcess(Long registerId);
void updateTotalCount(Long registerId, Long totalCount);
void updateFileHash(Long registerId, String fileHash);
void updateResultFileUrl(Long registerId, String resultFileUrl);

void complete(Long registerId, ImportStatusEnum status,
              Long totalCount, Long successCount, Long failCount,
              Long resumePosition, String errorMsg);

CancelImportResult cancelImport(CancelImportRequest request);
Pagination<ImportResult> listImportResult(ImportResultQueryRequest request);
boolean retry(Long registerId, String operator);
boolean revoke(Long registerId, String operator);
DownloadUrlResult resultFile(Long registerId);
List<AppTree> getAppTree();
```

Placement:

- Local impl in `easyfile-storage-local`
- Remote impl in `easyfile-storage-remote`
- Remove/replace `easyfile-server` `DefaultImportStorageServiceImpl` as the default owner

### 9.2 `ImportTriggerService`

Method-for-method mirror of `DownloadTriggerService`:
`trigger`, claim APIs, `startExecute`, `exeSuccess`, `exeFail`, `enterWaiting`, compensate, expire, archive.

### 9.3 Tables

Keep the May 2026 three-table shape:

1. `ef_async_import_task` — task definition  
2. `ef_async_import_record` — per-run record (`import_status`, source file fields, hash, business key, counts, progress, resume, result file, notify, retry, `execute_param`)  
3. `ef_async_import_trigger` — trigger queue with `UNIQUE(register_id)`

DDL should live with the project’s storage SQL scripts (`sql/storage.sql` and/or aligned `sql/import_tables.sql`).

### 9.4 Idempotency

- Blank `businessUniqueKey` → idempotency off
- Non-blank → distributed lock before run
- Existing terminal `SUCCESS` / `PARTIAL_SUCCESS` with **same fileHash** → return prior result
- Same key, **different fileHash** → **reject**
- `FAIL` does not permanently block the key

### 9.5 Resume

`resumePosition` = last source row number durably handled.

1. Advance only after successful `processBusiness` for the page’s valid rows (and page commit semantics).
2. Validation-only failures may still advance after valid rows are committed.
3. If `processBusiness` fails for the page → do **not** advance; task → `FAIL`.
4. `supportResumable() == false` → always start from 0; retries must be business-idempotent.

### 9.6 Retry

Allowed from `FAIL` / `CANCEL` (and optional manual re-run of `PARTIAL_SUCCESS`).  
Default `retryFromResume=true`. Re-trigger via ThreadPool execute or DB trigger insert.

## 10. Handler Orchestration & Engine Split

### 10.1 Handler family

| Export | Import |
|---|---|
| `BaseAsyncFileHandler` | `BaseAsyncImportHandler` |
| `AsyncFileHandlerAdapter` | `AsyncImportHandlerAdapter` |
| `DefaultAsyncFileHandler` | `DefaultAsyncImportHandler` |
| `DatabaseAsyncFileHandlerAdapter` | `DatabaseAsyncImportHandlerAdapter` |
| MQ handlers | SPI reserved only |

`AsyncImportHandlerAdapter.handleResult`:

1. `enableRunning` CAS  
2. Idempotent short-circuit  
3. Build `ImportContext`  
4. `executor.process(context)`  
5. `complete` + trigger success/fail  
6. Notify if enabled  
7. `asyncCompleteCallback` + `ImportEnd`

Optional light shared template `AbstractAsyncTaskLifecycle` may host tryEnable → run → success/fail; **export migration not required in this phase**.

### 10.2 Responsibility split

| Component | Does | Does not |
|---|---|---|
| Handler / Interceptor | Lifecycle, CAS, idempotent gate, complete, notify, callback | Row parse / business |
| `AbstractPagedImportExecutor` | Download, parser, page loop, progress/resume, result file, error aggregation | Register / trigger / limiting |
| User executor | `validate` / `processBusiness` | Framework concerns |
| `ImportParser` | Streaming pages, total count, row numbers | Business validation |

### 10.3 Page loop

```text
optional lock(businessUniqueKey)
download → temp file
parser = ImportParserFactory.create(...)
updateTotalCount
start from resumePosition if supportResumable
for each page:
    if cancel flag → CANCEL and break
    readPage → validate → processBusiness(valid)
    on processBusiness failure → FAIL (no resume advance)
    advance resume + refreshProgress
generate result file if row errors
complete SUCCESS | PARTIAL_SUCCESS | FAIL
idempotent mark on success-like terminal
cleanup temp + parser.close()
```

**Page business failure policy:** exception or failed `ProcessResult` ⇒ task `FAIL`.

## 11. Parser Design

```java
public interface ImportParser<T> extends Closeable {
    long getTotalCount();
    List<ImportRow<T>> readPage(long startRow, int pageSize);
    String getFileType();
}
```

`ImportRow<T>`: `rowNumber`, `data`, `rawValues`, `parseError`.

Implementations (v1):

1. **Streaming xlsx** (SAX / event / EasyExcel-style) — sequential read with internal buffer; **no per-page full workbook reopen**
2. **xls** via HSSF for smaller files; document size limits
3. **CSV** streaming parser

`ImportParserFactory` selects by `fileType`. Production path must not hardcode `new ExcelImportParser(...)`.

Replace current non-streaming / incorrect SXSSF-as-reader usage before calling import production-ready.

## 12. Result File & Errors

### 12.1 Two error levels

| Level | Examples | Terminal | Result file |
|---|---|---|---|
| Task | missing file, bad type, parser open failure, storage failure, page business failure | `FAIL` | best-effort |
| Row | validation / conversion / row reject | counted in failCount; task continues | required |

Completion:

- all success → `SUCCESS`
- finished with row failures → `PARTIAL_SUCCESS`
- task-level failure → `FAIL`
- result-file generation failure after row errors → degrade to `FAIL` with message

### 12.2 Result file content

Source row number, column, invalid value, error message (and business key when available).  
Upload via existing `UploadService`; persist URL through `updateResultFileUrl`.

## 13. Cross-Cutting: Limit, Notify, Listeners

### Limiting

- `ImportLimitingRequest` (or shared limit DTO with `bizType=IMPORT`)
- Applied on AOP/register path via existing `LimitingService`
- Suggested dimensions: `appId + importCode + operator` (configurable)
- Reject before `EXECUTING`

### Notify

- Driven by annotation `enableNotify` + request `notifyEmail`
- Fire on `SUCCESS` / `PARTIAL_SUCCESS` / `FAIL`
- Prefer single-fire on storage/server completion path in remote mode (avoid double notify)

### Interceptors / listeners

- `ImportExecutorInterceptor`
- `ImportStartListener` / `ImportEndListener`
- `asyncCompleteCallback` on executor

Registration style matches export listeners.

## 14. Configuration

```yaml
easyfile:
  import:
    enabled: true
    trigger-type: thread_pool   # thread_pool | db_schedule
    sync-persist: true
    retry-from-resume: true
    schedule-period-seconds: <n>
    max-trigger-count: <n>
    executing-timeout-seconds: <n>
    default-page-size: 1000
```

Import scheduling properties are **separate** from export’s.

## 15. Migration From Current Tree

1. Keep core ideas: `BaseImportExecutor`, paged executor, contexts, progress, result generator, idempotent manager.
2. Expand `ImportStorageService`; move JDBC to `storage-local`; add `storage-remote`.
3. Replace `ImportServiceImpl` stubs/mocks with real facade over storage/trigger/handler.
4. Fix `EasyFileImportAutoConfiguration` (no broken `new ImportServiceImpl()` wiring).
5. Add AOP, handler family, trigger service, status enum.
6. Replace production parser with streaming implementations + factory.
7. Align SQL scripts; ensure Admin list/detail understand `PARTIAL_SUCCESS`.
8. Add example controllers for local and remote.
9. Keep May 2026 docs as historical; this document is the implementation source of truth.

## 16. Testing Strategy

### Unit

- `FileImportExecutorSupport` duplicate/lookup
- Local storage `register` / `enableRunning` CAS
- Trigger state transitions
- Resume advance rules
- `PARTIAL_SUCCESS` completion
- Parser conversions + row numbers
- Limiting rejection
- Idempotent same/different hash

### Integration

- ThreadPool end-to-end
- DB Trigger claim + restart recovery
- Sync `importResult` with persist
- Remote register + business-node execute
- Cooperative cancel
- Result file download
- Notify hook invocation (can assert listener/mock)

### Acceptance checklist

1. `@FileImportExecutor` auto-registers into import task storage  
2. AOP `importResult` async returns `registerId`  
3. Sync path executes and (by default) persists  
4. ThreadPool and DB Trigger both runnable via config  
5. Large files page without full materialization (streaming parser)  
6. Progress + resume persisted  
7. Row failures → result file + `PARTIAL_SUCCESS`  
8. Task failures → `FAIL` with message  
9. Idempotent duplicate returns prior result; hash mismatch rejected  
10. Limiting, notify, start/end listeners wired  
11. Remote storage client works against server APIs  
12. Example demo path works; Admin APIs remain usable  

## 17. Implementation Phasing (for planning)

Suggested plan slices (not optional relative to goals — ordering only):

1. **Contracts & model** — enum, DTOs, storage/trigger APIs, SQL, local entities/mappers  
2. **Local storage + trigger impl** — register/CAS/progress/complete/cancel/retry  
3. **Engine hardening** — responsibility split, streaming parsers, resume/idempotent/result file  
4. **Handler family + starter config** — ThreadPool + DB schedule  
5. **AOP entry + sync path** — interceptor, limiting, listeners  
6. **Server facade + remote client + notify** — remove stubs; move storage ownership  
7. **Examples + Admin API compatibility + tests**

## 18. Open Items Closed By This Spec

| Former open item | Resolution |
|---|---|
| CSV in v1? | Yes |
| Shared vs separate schedule properties | Separate import properties |
| Different fileHash for same business key | Reject |

## 19. Success Criteria

Import feels like export from a framework user’s perspective: one annotation, thin business methods, one call (`importResult`), observable async task lifecycle, operational controls (limit/cancel/retry/notify), and production-safe large-file parsing — available both embedded (AOP/local) and centralized (HTTP/remote).

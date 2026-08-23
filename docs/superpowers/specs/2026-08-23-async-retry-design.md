# Async Download Business Retry Design

Date: 2026-08-23  
Branch: `feature/async_retry-260823` (from `master`)  
Status: Approved in design review

## 1. Problem

Async download already has the pieces of a business-retry story, but they are not wired into the execute path.

What exists today:

- `@FileExportExecutor.maxServerRetry` is persisted as `ef_async_download_record.max_server_retry`.
- Server mapper methods `selectExportRecordOfRetryable` and `updateRetryById` exist and have no callers.
- Admin DTO `RetryDownloadTaskRequest` exists; there is no controller, no storage API, no UI action.
- `enableRunning` only accepts `NONE` → `EXECUTING`. A `FAIL` record cannot re-enter `doExecute`.
- Trigger compensation (`ef_async_download_trigger.trigger_count` / `maxTriggerCount`) retries unfinished jobs (timeout, crash, waiting expired). After `doExecute` returns normally — including business `FAIL` — the trigger is marked `SUCCESS` and is never picked up again.

Result: `maxServerRetry` is dead configuration. Failed exports stay `FAIL` unless a user registers a new task.

## 2. Goals

- Complete a business-retry chain that re-enters the existing `execute` / `doExecute` path.
- Support automatic retry after `FAIL` and manual retry from Admin/API.
- Keep trigger compensation and business retry strictly separate.
- Work for `default` (thread pool), `schedule`, and `MQ`/`disruptor` trigger types.
- Work for local storage and remote server storage (claim on server, execute on the app that owns the executor).

## 3. Non-goals

- Import-task retry (out of this branch; master download only).
- Changing `AsyncFileHandlerAdapter.handleFileWithRetry` (upload `GenerateFileException` IO retry).
- Retrying `CANCEL`, `SUCCESS`, `INVALID`, `EXECUTING`, or `UPLOADING`.
- Forcing a retry after `current_retry >= max_server_retry`.
- Changing first-register behavior of `DownloadTriggerService.trigger()` (still insert-if-absent).
- Suppressing per-attempt notify callbacks.
- Changing export-cache short-circuit behavior.

## 4. Decisions

| Topic | Choice |
|---|---|
| Who initiates retry | Automatic after `FAIL` **and** manual Admin/API |
| Retryable record status | `FAIL` only |
| Trigger compensation vs business retry | Strict layers: trigger does not consume `current_retry`; business retry only claims `FAIL` |
| When to re-enter execute | After this attempt completes, claim then re-queue immediately; `default` also has a lightweight scanner |
| Manual vs automatic quota | Same `max_server_retry` / `current_retry` CAS |
| `maxServerRetry <= 0` | No business retry (matches annotation) |
| `maxServerRetry = 1` | One retry after first `FAIL` (two executions total) |
| Claim vs increment | Single atomic claim: increment + `FAIL` → `NONE` + reset progress |
| Rollback of `current_retry` | Never. A successful claim spends one retry slot |
| Dispatch failures | Logged only; must not break the completed callback / trigger success path |

## 5. Architecture

Business retry is a thin coordinator outside generate/upload/`enableRunning`.

```
First run:
  register → handler.execute → doExecute
           → enableRunning(NONE→EXECUTING) → generate/upload → uploadCallback

Retry:
  claimRetry(FAIL→NONE, current_retry+1)
           → handler.reexecute(registerId)
           → same doExecute (interceptor is not involved; no second register)
```

### 5.1 Three responsibilities

| Responsibility | Owner | Contract |
|---|---|---|
| Claim | `DownloadStorageService.claimRetry(registerId, operator)` | One CAS. Succeeds only when `upload_status = FAIL` and `max_server_retry > current_retry`. Sets `current_retry = current_retry + 1`, `upload_status = NONE`, `execute_process = 0`, `error_msg` cleared, `last_execute_time = now()`, `update_by = operator`. |
| Retrigger | `DownloadTriggerService.retrigger(registerId)` | If a trigger row exists: set status `INIT`, `trigger_count = 0`, refresh `last_execute_time`. If none: same as `trigger()` insert. Existing `trigger()` stays insert-if-absent. |
| Reexecute | `BaseAsyncFileHandler.reexecute(registerId)` | Load `DownloadRequestInfo` + `FileExportExecutorSupport.get(downloadCode)`, then re-queue. Must not go through `FileExportInterceptor` (no second `register`). |

`listRetryable` is a storage query used by the scanner (and remote client): records that are claimable `FAIL`, or orphan `NONE` with `current_retry > 0` older than the grace window.

### 5.2 Why dispatch happens after trigger `exeSuccess`

`DatabaseAsyncFileHandlerAdapter.doTrigger` today:

1. `doExecute` (business `FAIL` still returns normally)
2. `triggerService.exeSuccess`
3. `exeSuccess` accepts `INIT` as a source status

If retry reset the trigger to `INIT` inside `doExecute`, step 2 would mark it `SUCCESS` again and schedule would never pick it up.

Therefore:

- **default:** after `doExecute` returns, `claimRetry` + `reexecute` (submit to the pool; do not recurse on the current stack).
- **schedule / MQ:** `doExecute` → `exeSuccess` (this trigger attempt is done) → `claimRetry` + `retrigger` + immediate re-queue.

If `doExecute` throws and never callbacks, do not claim. Trigger goes `exeFail` and existing compensation owns it.

### 5.3 Re-queue per trigger type

`reexecute` is type-specific because first-run `execute()` must stay unchanged:

| Type | `reexecute` |
|---|---|
| default | Load executor/params, `execute()` → thread pool |
| schedule | `retrigger` then `doTrigger` (immediate; do not wait for the next schedule tick) |
| MQ / disruptor | `retrigger` then `doSend` directly. Retry is **not** inside the interceptor `TransactionTemplate`, so do not use `TransactionSynchronization.afterCommit` |

### 5.4 Hook points (existing code stays the main path)

- `AsyncFileHandlerAdapter.handleResult` / `generateFile` / `enableRunning`: unchanged.
- `DefaultAsyncFileHandler`: after the runnable finishes `doExecute`, call `dispatchRetry`.
- `DatabaseAsyncFileHandlerAdapter.doTrigger`: after `exeSuccess` / `exeFail`, call `dispatchRetry` (no-op when the record is not claimable `FAIL`).
- Server `updateRetryById` is replaced/extended by the full claim SQL (status back to `NONE`, progress reset). Keep the quota predicate `max_server_retry > current_retry` (equivalent to `max_server_retry >= current_retry + 1` after increment).
- Admin: `POST /easyfile-ui/download-task/retry` and server `POST /easyfile/download/retry`. Storage `retry` is claim only. Local process may `reexecute` immediately when a handler bean is present. Remote admin only claims; the business app scanner reexecutes.

## 6. State machine

`current_retry` is the number of **successful business claims**, not trigger attempts.

```
NONE(retry=0) --enableRunning--> EXECUTING --callback--> SUCCESS     (terminal)
     ^                              |
     |                              +--callback--> FAIL
     |                                              |
     |                         claim succeeds        |  FAIL && max_server_retry > current_retry
     |                      (retry+1, progress 0)    |
     +---------------- NONE(retry>0) <---------------+
                            |
                            +--enableRunning--> EXECUTING --> same doExecute
                            +--existing cancel: NONE --> CANCEL
                               (claimed but not started yet; allowed)

No claim from: SUCCESS, CANCEL, INVALID, EXECUTING, UPLOADING,
               FAIL with current_retry >= max_server_retry,
               NONE (already waiting)
```

Trigger states remain independent. A new business attempt resets `trigger_count` to 0 so infrastructure budget is fresh for that attempt. Trigger compensation still does not increment `current_retry`.

## 7. Flows

### 7.1 Automatic retry

1. This attempt has already written `uploadCallback(FAIL)`.
2. default: go to step 3. schedule/MQ: `exeSuccess` first, then step 3.
3. `claimRetry`. Failure (race, no quota, not `FAIL`) ends the flow.
4. Success → `reexecute(registerId)`.
5. `enableRunning(NONE→EXECUTING)` and the existing main path run again.

### 7.2 Manual retry

Same `claimRetry` (operator stored in `update_by`).

- Local (handler in-process): claim then `reexecute`.
- Remote (admin/server only): claim only. Client scanner picks `NONE && current_retry > 0`.

Exhausted quota returns failure. No override.

### 7.3 Scanner (default required; safe for all types)

Small period. Never selects first-run work.

| Match | Action |
|---|---|
| `FAIL && max_server_retry > current_retry` | `claimRetry` then `reexecute` |
| `NONE && current_retry > 0` and `last_execute_time` older than grace | `reexecute` only (already claimed) |

Do **not** select `NONE && current_retry = 0` (first execution still belongs to the original `execute` / trigger).

Grace exists so the scanner does not race an in-process dispatch that just claimed. If it still races, `enableRunning` CAS fails and `reexecute` is a no-op.

Default grace: 30 seconds. Default scan period: 10 seconds. Keys are listed in §12; all have defaults, none are required.

### 7.4 Concurrency

- Two of {auto, manual, scanner} claiming the same id: one CAS winner.
- Claim succeeded, `enableRunning` failed: another worker is already running; this `reexecute` no-ops.
- Trigger compensation only touches the trigger row and unfinished `EXECUTING` jobs. Business claim only accepts `FAIL`. Counters are not shared.

## 8. Error handling

`dispatchRetry` is fully try/caught. Failures log and must not affect the already-written callback or trigger `exeSuccess` / `exeFail`.

| Situation | Behavior |
|---|---|
| Status is not `FAIL` | `claimRetry` is false; no enqueue |
| Quota exhausted | Same; manual API returns failure |
| Concurrent double claim | One CAS success |
| `reexecute` cannot resolve executor or `execute_param` | Record stays `NONE(retry>0)`; scanner retries; no automatic flip back to `FAIL` |
| `retrigger` fails after a successful claim | Do not roll back `current_retry`. Scanner or the next cycle can still see `NONE(retry>0)` |
| `doExecute` threw, no callback | Do not claim. Trigger `exeFail` + existing compensation |
| Scanner hits a just-claimed row | Grace skips it; `enableRunning` is the backstop |
| Remote `data == null` | Treat `claimRetry` as false and `listRetryable` as empty; no NPE (same as `enableRunning`) |
| MQ retry outside a transaction | `retrigger + doSend` only |
| Crash after claim, before enqueue | Scanner picks `NONE && current_retry > 0` after grace |

Do not roll back `current_retry`: a successful claim means the slot was granted. Rolling back under retrigger flaps would either starve or unbounded-retry.

## 9. APIs and storage

### 9.1 Storage / service

```java
boolean claimRetry(Long registerId, String operator);

List<Long> listRetryable(int lookBackHours, int limit, int orphanGraceSeconds);
```

`DownloadStorageService.retry(registerId, operator)` is an alias of `claimRetry` for Admin naming. It does not enqueue.

Server `AsyncDownloadService` exposes the same two methods. `DownloadController` adds:

- `POST /easyfile/download/retry` body: `RetryDownloadRequest` in `easyfile-common` (`registerId` only). Operator is `"system"` on the server API and `adminProperty.getAdminUsername()` on Admin UI. Returns `ResponseResult<Boolean>`.
- `POST /easyfile/download/listRetryable` for the remote scanner (look-back, limit, grace).

Remote `EasyFileClient` + `RemoteDownloadStorageServiceImpl` implement both calls. Null `data` → false / empty list.

Local mapper adds `claimRetry`. Server replaces unused `updateRetryById` with the same claim SQL (status back to `NONE`, progress reset, `max_server_retry > current_retry`). `selectExportRecordOfRetryable` is aligned with `listRetryable` (include orphan `NONE`, exclude `NONE(retry=0)`).

Admin `RetryDownloadTaskRequest` stays as the UI payload and maps to `registerId` only.

### 9.2 Admin

`DownloadTaskController`:

- `POST /easyfile-ui/download-task/retry` with existing `RetryDownloadTaskRequest`.
- `@Auth` like `revoke`.
- Returns ok / fail from `claimRetry`.
- If a `BaseAsyncFileHandler` is in the same process, `reexecute` after a successful claim.

UI: retry action on `download-task` for `FAIL` rows only.

### 9.3 Handler

```java
// BaseAsyncFileHandler
default void reexecute(Long registerId) { /* load + execute */ }

protected final void dispatchRetry(Long registerId) { /* claim + reexecute, swallow errors */ }
```

`reexecute` must use the same executor instance style as the first run: default matches first-run `execute()` (no extra trigger flag). schedule/MQ `doTrigger` already calls `FileExportTriggerContext.setAsyncTriggerFlagIfAbsent(true)`; retry `doTrigger` keeps that so `export()` cannot register a new task.

## 10. Compatibility

- First register path: unchanged (`register` + `handler.execute`).
- `trigger()`: unchanged insert-if-absent.
- `enableRunning`: still `NONE` only. Retry works because claim returns the row to `NONE`.
- `cancel`: still `NONE` only. A claimed-but-not-started retry can be cancelled; that is intended and compatible.
- Trigger `maxTriggerCount` / reaper / archive: unchanged.
- Local and remote download storage: same claim semantics.
- DB-trigger modes remain local-only, as documented today. Remote + `default` is covered by claim-on-server + client `reexecute` / scanner.

## 11. Testing

1. **claimRetry** (local mapper + server service): `FAIL` with quota → `NONE`, `retry+1`, progress 0. `SUCCESS` / `CANCEL` / exhausted quota / second concurrent claim → false.
2. **retrigger**: existing `SUCCESS`/`FAIL` → `INIT` and `trigger_count = 0`. Missing row → insert. Existing `trigger()` still no-ops when a row exists.
3. **dispatchRetry**: `FAIL` with quota → claim then `reexecute`. `doExecute` throw → no claim. Exceptions inside dispatch do not escape.
4. **Re-queue by type**: default uses the pool; schedule retriggers after `exeSuccess` then `doTrigger`; MQ uses `doSend` and not `afterCommit`.
5. **Scanner**: does not select `NONE(retry=0)`; selects claimable `FAIL`; skips in-grace `NONE(retry>0)`.
6. **Admin/API**: success and failure responses; remote null `data` does not NPE.
7. **Regression**: `enableRunning` still only from `NONE`; `cancel` still only from `NONE`; trigger compensation count is unchanged by a business `FAIL`.

## 12. Configuration

No required new config. Optional overrides (defaults in parentheses):

- `easyfile.download.retry.scan-enabled` (`true` for every trigger type; cheap safety net, CAS makes it idempotent)
- `easyfile.download.retry.scan-period-seconds` (`10`)
- `easyfile.download.retry.orphan-grace-seconds` (`30`)
- `easyfile.download.retry.look-back-hours` (`24`)
- `easyfile.download.retry.scan-batch-size` (`50`)

Existing `maxServerRetry` on the executor annotation remains the only per-task quota.

## 13. Implementation sketch (file-level)

Create / extend, without rewriting generate/upload:

- `DownloadStorageService` / local / remote / server service + mapper + `DownloadController`
- `DownloadTriggerService.retrigger` + local mapper
- `BaseAsyncFileHandler.reexecute` + `dispatchRetry` hooks on default and database handlers
- MQ path: `doSend` from `reexecute`
- Starter scheduled scanner bean
- Admin controller + `download-task` UI
- Unit tests listed in §11

## 14. Success criteria

- A task with `maxServerRetry = 1` that callbacks `FAIL` is claimed once, returns to `NONE`, and runs `doExecute` again without a new `register`.
- A task with `maxServerRetry = 0` never claims.
- `CANCEL` is never claimed.
- Trigger compensation still retries unfinished executions without incrementing `current_retry`.
- Admin retry of a `FAIL` record uses the same claim; remote default still eventually `reexecute`s via the scanner.
- Existing first-run default / schedule / MQ behavior is unchanged.

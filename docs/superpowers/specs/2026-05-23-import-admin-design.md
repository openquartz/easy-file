# EasyFile Import Admin Design

## Background

EasyFile already provides a lightweight admin console for async download task management in `easyfile-ui`.
The current admin experience is centered around a single page:

- side navigation enters the task page from `index.html`
- `download-task.html` renders the management table
- `download-task.js` drives the table behavior
- `DownloadTaskController` exposes list, download, revoke, and app-switch endpoints

At the same time, the import architecture is being aligned with the export architecture. Once import gains storage,
trigger, retry, progress, and result-file support, the admin console also needs a matching management surface.

This design intentionally does not add import creation or upload capability to admin. The scope is management and
result viewing only.

## Goals

1. Reuse the existing admin architecture instead of creating a second admin workflow.
2. Let users switch between download tasks and import tasks inside the same management page.
3. Preserve the current lightweight layui + iframe admin style.
4. Make import-specific concepts visible: progress, total/success/fail counts, result file, retryability.
5. Keep backend controller and service boundaries clear even if the UI is shared.

## Non-Goals

1. No file upload entry in admin.
2. No import task creation wizard in admin.
3. No redesign of the whole admin shell.
4. No replacement of the existing download management behavior.

## Recommended Approach

Use one shared task-management page with an internal task-type switch.

The existing `download-task` page becomes a unified task workbench:

- `下载任务`
- `导入任务`

The page frame, toolbar region, and table container are shared.
The active task type changes:

- request URLs
- table columns
- row actions
- empty-state and status texts

Backend APIs remain separated by domain:

- download APIs stay under `/easyfile-ui/download-task/*`
- import APIs are added under `/easyfile-ui/import-task/*`

This gives the user one coherent page while keeping server responsibilities clean.

## Alternatives Considered

### Option 1: Shared Page With Tabs

Put `下载任务 | 导入任务` at the top of the current task page and switch table config dynamically.

Pros:

- best reuse of the current admin shell
- lowest navigation cost for users
- clear path for later adding more task types

Cons:

- `download-task.js` must be refactored into a more generic task page script

### Option 2: Shared Page With Task-Type Dropdown

Keep one table and use a dropdown filter to switch `下载` and `导入`.

Pros:

- smallest surface change

Cons:

- weak discoverability
- import management feels hidden rather than first-class

### Option 3: Separate Import Page

Add a new side-nav entry and a second standalone management page.

Pros:

- clean separation

Cons:

- duplicates page structure
- worse user navigation
- contradicts the desired "merge into existing page" direction

## Information Architecture

### Admin Shell

The side navigation should keep a single task-management entry.

Recommended text:

- current: `下载任务`
- target: `任务管理`

Reason:

Once import management is embedded in the page, the current label becomes misleading. A user entering a page labeled
`下载任务` does not expect import administration to exist inside it.

If changing nav text is considered too disruptive in the first version, the old label may remain temporarily, but the
design should treat `任务管理` as the intended final wording.

### Page Structure

The current page served from `/easyfile-ui/download-task/get` becomes a shared management page with three zones:

1. Task type switch
2. Toolbar and filters
3. Main table

Suggested structure:

```text
+---------------------------------------------------------+
| 下载任务 | 导入任务                                      |
+---------------------------------------------------------+
| 刷新 | 统一 AppId | 状态筛选(导入模式)                  |
+---------------------------------------------------------+
| 表格区域                                                 |
| 根据当前视图切换列、接口和行操作                         |
+---------------------------------------------------------+
```

## UI Design

### Task Type Switch

Place a two-tab switch at the top of the page:

- `下载任务`
- `导入任务`

Behavior:

- default to `下载任务` to preserve current behavior
- switching tabs should immediately reload the table
- no full page navigation
- no iframe replacement

This should feel like changing the dataset in one workbench, not like moving to a different subsystem.

### Toolbar

Keep existing shared actions:

- `刷新`
- `统一 AppId`

Add import-only light filters in import mode:

- `全部`
- `处理中`
- `失败/部分成功`

These should be compact and shallow. The admin page is for operational scanning, not advanced reporting.

### Download Mode

Download mode keeps the current columns and current operations:

- download
- cancel

No behavioral changes are required beyond adapting the shared-page script.

### Import Mode Columns

Recommended import columns:

- `ID`
- `导入状态`
- `导入描述`
- `操作人`
- `导入时间`
- `最新执行时间`
- `处理统计`
- `执行进度`
- `结果文件`
- `异常信息`
- `操作`

#### 处理统计

Display as compact summary text:

`成功 120 / 失败 8 / 总计 128`

This is more readable than splitting counts into too many narrow columns.

#### 执行进度

Reuse the current layui progress bar pattern used in the download page.

#### 结果文件

This should not display a raw URL.
It should display state-aware text or action:

- `查看结果`
- `无异常`
- `生成中`
- `无结果`

Reason:

For import management, the result file is one of the main operator outcomes. It deserves first-class visibility in the
table rather than being hidden behind a generic action menu.

### Import Mode Actions

Import row actions should stay minimal and status-driven.
Do not show more than two main actions per row.

Recommended action rules:

- `EXECUTING`
  - show `撤销` only if backend supports cancellation
  - optional future `查看详情`, but do not require it in the first version

- `FAIL`
  - show `重试`
  - show `查看错误` when result file exists

- `PARTIAL_SUCCESS`
  - show `查看结果`
  - show `重试`

- `SUCCESS`
  - show `查看结果` only when a result file exists
  - otherwise no primary action is needed

- `CANCEL`
  - no primary action, or optionally `重试` if business rules allow it later

### Empty States

Recommended import empty-state texts:

- no import data at all: `暂无导入任务`
- current filter has no matches: `当前筛选条件下无导入记录`

These should be different from generic download text so operators always know which view they are on.

### Error Feedback

Continue using layui `layer.confirm` and `layer.msg`.

Import mode needs three key feedback flows:

1. `查看结果`
   - if a result file exists, open or download it directly

2. `重试`
   - confirm with explicit wording that the task will re-enter processing

3. `撤销`
   - confirm and give clear result toast

## Frontend Architecture

The current `download-task.js` should be refactored into a shared task-page driver instead of remaining download-only.

Recommended state model:

- `currentTaskType = "download" | "import"`
- `currentFilter`
- `currentUnifiedAppId`

Recommended script responsibilities:

- build columns by task type
- build list URL by task type
- build row action behavior by task type
- reload table on tab switch
- keep shared `parseData` behavior

Suggested helper split:

- `buildDownloadColumns()`
- `buildImportColumns()`
- `getListUrlByType(type)`
- `getActionUrlByType(type, action)`
- `reloadTaskTable(type)`

This keeps the current page technically simple while avoiding a pile of `if (type === ...)` logic inside one large
table definition block.

## Backend Design

### Controller Layout

Keep page rendering on the existing route:

- `GET/REQUEST /easyfile-ui/download-task/get`

Download data APIs remain in:

- `DownloadTaskController`

Add a dedicated controller for import task APIs:

- `ImportTaskController`

Recommended endpoints:

- `POST /easyfile-ui/import-task/list`
- `POST /easyfile-ui/import-task/result`
- `POST /easyfile-ui/import-task/retry`
- `POST /easyfile-ui/import-task/revoke`
- `POST /easyfile-ui/import-task/listAppId`

Reason:

Even though the UI is shared, the server-side domain logic should remain explicit. Download and import are sibling
capabilities, not one controller's miscellaneous action bucket.

### Request Models

The import list endpoint should mirror download list usage and keep the same pagination contract.

Recommended request model:

- `ShowImportTaskRequest`

Fields:

- `pageNum`
- `pageSize`
- `startImportTime`
- `endImportTime`
- `status`

This intentionally stays small. Admin is for operational monitoring, not a full audit query tool.

### Response Models

Add a UI-oriented response model aligned with `DownloadTaskResult`.

Recommended model:

- `ImportTaskResult`

Fields:

- `registerId`
- `importStatus`
- `importStatusDesc`
- `importCode`
- `importCodeDesc`
- `createBy`
- `createTime`
- `lastExecuteTime`
- `executeProcess`
- `totalCount`
- `successCount`
- `failCount`
- `resultFileUrl`
- `errorMsg`
- `allowRetry`
- `allowRevoke`

`allowRetry` and `allowRevoke` should be computed on the backend instead of inferred in JavaScript.
This keeps action availability consistent with actual business rules.

### Storage Service Usage

The UI controller layer should stay thin and map directly to the storage-facing import APIs:

- list import results
- revoke import
- retry import
- get result file
- get app tree

The admin layer should not perform state reconstruction or business-rule duplication.

## Status Semantics

Admin display should explicitly support these import statuses:

- `NONE`: 待执行
- `EXECUTING`: 执行中
- `SUCCESS`: 成功
- `PARTIAL_SUCCESS`: 部分成功
- `FAIL`: 失败
- `CANCEL`: 已撤销

`PARTIAL_SUCCESS` must be visually distinguished from `SUCCESS`.

Why:

- `SUCCESS` means nothing requires operator follow-up
- `PARTIAL_SUCCESS` means the task finished but still produced a result file that may need review or retry

This difference should affect:

- text
- color
- primary row action

## Interaction Flows

### View Import Tasks

1. User enters `任务管理`
2. User clicks `导入任务`
3. Page reloads table with import columns and import data source
4. User scans status, counts, progress, and result-file availability

### View Result File

1. User clicks `查看结果`
2. Frontend calls `/easyfile-ui/import-task/result`
3. Backend returns file URL and file metadata or a direct result structure
4. Browser downloads or opens the file

### Retry Import

1. User clicks `重试`
2. Frontend shows confirm dialog
3. Backend validates retry eligibility
4. Storage/trigger layer re-queues the task
5. UI shows success and refreshes table

### Revoke Import

1. User clicks `撤销`
2. Frontend shows confirm dialog
3. Backend validates revocation eligibility
4. UI shows success and refreshes table

## Visual Style Guidance

This design should preserve the existing EasyFile admin language:

- layui table as primary surface
- compact operations
- no card-heavy redesign
- no additional page chrome beyond the current shell

Import should feel native to the current admin, not like a newly embedded product area.

The main visual change is the addition of the task-type switch and the stronger prominence of result-file state in the
import view.

## Implementation Boundaries

### Included In This Design

- shared page information architecture
- import tab interaction design
- import management API surface
- admin response model shape
- status semantics

### Not Included In This Design

- import upload entry
- import creation flow
- detailed row-level result preview modal
- multi-step wizard UI
- global admin shell redesign

## Risks And Mitigations

### Risk 1: Shared Script Becomes Hard To Maintain

Mitigation:

- split table config by task type
- keep backend API families separate
- compute action availability on backend

### Risk 2: Users Misunderstand Navigation Because Sidebar Says Download

Mitigation:

- rename the nav entry to `任务管理`
- if renaming is delayed, add strong in-page tabs with clear labels

### Risk 3: Import And Download Status Semantics Diverge

Mitigation:

- keep a dedicated import response model
- do not force import into download status names
- treat `PARTIAL_SUCCESS` as a first-class import state

## Acceptance Criteria

1. Admin retains a single task-management page entry.
2. Users can switch between download and import views without leaving the page.
3. Download behavior remains unchanged from the user perspective.
4. Import view shows status, counts, progress, result file, and error information.
5. Import result files are directly accessible from the list view.
6. Retry and revoke actions are shown only when allowed.
7. Frontend and backend remain separated by task domain even though the page is shared.
8. The design fits the current layui admin structure without requiring a shell redesign.

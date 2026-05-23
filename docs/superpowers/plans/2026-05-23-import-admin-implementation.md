# Import Admin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add import-task management and result viewing to the existing EasyFile admin task page without introducing a second admin workflow.

**Architecture:** Reuse the current `/easyfile-ui/download-task/get` page as a shared task workbench. Keep backend domains separated with `DownloadTaskController` for download APIs and a new `ImportTaskController` for import APIs, while refactoring the page template and JavaScript into a task-type-driven UI that switches between download and import views.

**Tech Stack:** Spring MVC, Thymeleaf, layui table/dropdown/tabs, existing EasyFile admin module, Maven multi-module build.

---

### Task 1: Add import admin request/response models and controller skeleton

**Files:**
- Create: `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/request/ShowImportTaskRequest.java`
- Create: `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/request/RetryImportTaskRequest.java`
- Create: `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/request/RevokeImportTaskRequest.java`
- Create: `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/request/ViewImportResultRequest.java`
- Create: `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/response/ImportTaskResult.java`
- Create: `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/controller/ImportTaskController.java`
- Modify: `easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/imports/ImportStorageService.java`

- [x] **Step 1: Write the failing compile target by defining missing admin import API surface**

Add request/response classes with the exact fields the UI needs.

`ShowImportTaskRequest.java`
```java
package com.openquartz.easyfile.admin.model.request;

import java.util.Date;
import lombok.Data;

@Data
public class ShowImportTaskRequest {

    private Integer pageNum;

    private Integer pageSize;

    private Date startImportTime;

    private Date endImportTime;

    private String status;
}
```

`RetryImportTaskRequest.java`
```java
package com.openquartz.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RetryImportTaskRequest {

    @NotNull
    private Long registerId;
}
```

`RevokeImportTaskRequest.java`
```java
package com.openquartz.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RevokeImportTaskRequest {

    @NotNull
    private Long registerId;
}
```

`ViewImportResultRequest.java`
```java
package com.openquartz.easyfile.admin.model.request;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ViewImportResultRequest {

    @NotNull
    private Long registerId;
}
```

`ImportTaskResult.java`
```java
package com.openquartz.easyfile.admin.model.response;

import java.util.Date;
import lombok.Data;

@Data
public class ImportTaskResult {

    private Long registerId;

    private String importStatus;

    private String importStatusDesc;

    private String importCode;

    private String importCodeDesc;

    private String createBy;

    private Date createTime;

    private Date lastExecuteTime;

    private Integer executeProcess;

    private Long totalCount;

    private Long successCount;

    private Long failCount;

    private String resultFileUrl;

    private String errorMsg;

    private Boolean allowRetry;

    private Boolean allowRevoke;
}
```

- [x] **Step 2: Add default import admin methods to `ImportStorageService`**

Update `ImportStorageService` so the admin controller has stable methods to call before the concrete local implementation lands.

```java
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import java.util.Collections;
import java.util.List;
import com.openquartz.easyfile.admin.model.request.ShowImportTaskRequest;
import com.openquartz.easyfile.admin.model.response.ImportTaskResult;

default Pagination<ImportTaskResult> listImportResult(ShowImportTaskRequest request) {
    return Pagination.empty();
}

default boolean retry(Long registerId, String operator) {
    return false;
}

default boolean revoke(Long registerId, String operator) {
    return false;
}

default DownloadUrlResult resultFile(Long registerId) {
    return null;
}

default List<AppTree> getAppTree() {
    return Collections.emptyList();
}
```

- [x] **Step 3: Add the minimal import admin controller**

Create `ImportTaskController.java` with the same response contract pattern used by `DownloadTaskController`.

```java
package com.openquartz.easyfile.admin.controller;

import com.openquartz.easyfile.admin.controller.annotation.Auth;
import com.openquartz.easyfile.admin.model.request.RevokeImportTaskRequest;
import com.openquartz.easyfile.admin.model.request.RetryImportTaskRequest;
import com.openquartz.easyfile.admin.model.request.ShowImportTaskRequest;
import com.openquartz.easyfile.admin.model.request.ViewImportResultRequest;
import com.openquartz.easyfile.admin.model.response.ImportTaskResult;
import com.openquartz.easyfile.admin.property.AdminProperty;
import com.openquartz.easyfile.common.bean.ResponseResult;
import com.openquartz.easyfile.common.response.AppTree;
import com.openquartz.easyfile.common.response.DownloadUrlResult;
import com.openquartz.easyfile.common.util.page.Pagination;
import com.openquartz.easyfile.storage.imports.ImportStorageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/easyfile-ui/import-task")
@CrossOrigin
@RequiredArgsConstructor
public class ImportTaskController {

    private final ImportStorageService importStorageService;
    private final AdminProperty adminProperty;

    @Auth
    @ResponseBody
    @PostMapping(value = "/list", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<Pagination<ImportTaskResult>> list(@RequestBody ShowImportTaskRequest request) {
        return ResponseResult.ok(importStorageService.listImportResult(request));
    }

    @Auth
    @ResponseBody
    @PostMapping(value = "/result", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<DownloadUrlResult> result(@RequestBody ViewImportResultRequest request) {
        return ResponseResult.ok(importStorageService.resultFile(request.getRegisterId()));
    }

    @Auth
    @ResponseBody
    @PostMapping(value = "/retry", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<?> retry(@RequestBody RetryImportTaskRequest request) {
        return importStorageService.retry(request.getRegisterId(), adminProperty.getAdminUsername())
            ? ResponseResult.ok()
            : ResponseResult.fail("01", "重试失败");
    }

    @Auth
    @ResponseBody
    @PostMapping(value = "/revoke", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<?> revoke(@RequestBody RevokeImportTaskRequest request) {
        return importStorageService.revoke(request.getRegisterId(), adminProperty.getAdminUsername())
            ? ResponseResult.ok()
            : ResponseResult.fail("01", "撤销失败");
    }

    @Auth
    @ResponseBody
    @PostMapping(value = "/listAppId", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseResult<List<AppTree>> listAppId() {
        return ResponseResult.ok(importStorageService.getAppTree());
    }
}
```

- [x] **Step 4: Run compile to verify the new controller surface**

Run:
```bash
mvn -pl easyfile-ui,easyfile-storage/easyfile-storage-api -am -DskipTests compile
```

Expected: compile succeeds, or failures are only from missing methods that will be added in later steps of this plan.

- [ ] **Step 5: Commit the controller/model slice**

```bash
git add easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/request/ShowImportTaskRequest.java \
  easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/request/RetryImportTaskRequest.java \
  easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/request/RevokeImportTaskRequest.java \
  easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/request/ViewImportResultRequest.java \
  easyfile-ui/src/main/java/com/openquartz/easyfile/admin/model/response/ImportTaskResult.java \
  easyfile-ui/src/main/java/com/openquartz/easyfile/admin/controller/ImportTaskController.java \
  easyfile-storage/easyfile-storage-api/src/main/java/com/openquartz/easyfile/storage/imports/ImportStorageService.java
git commit -m "feat: add import admin controller surface"
```

### Task 2: Convert the page shell into a shared task-management view

**Files:**
- Modify: `easyfile-ui/src/main/resources/easyfile-admin-ui/templates/download-task.html`
- Modify: `easyfile-ui/src/main/resources/easyfile-admin-ui/templates/index.html`
- Modify: `easyfile-ui/src/main/resources/easyfile-admin-ui/static/js/index.js`

- [x] **Step 1: Add a task-type switch to the shared task template**

Replace the current single-toolbar page structure in `download-task.html` with a layout that includes:
- a task-type tab bar
- the existing table
- two row-action templates, one for download and one for import

Target template content:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="utf-8">
    <title>任务管理</title>
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <link rel="stylesheet" href="/easyfile-ui/static/layui/css/layui.css" media="all">
</head>
<body>
<div class="layui-tab layui-tab-brief" lay-filter="taskTypeTab">
    <ul class="layui-tab-title">
        <li class="layui-this" lay-id="download">下载任务</li>
        <li lay-id="import">导入任务</li>
    </ul>
</div>

<table id="table" lay-filter="table"></table>

<script src="/easyfile-ui/static/layui/layui.js"></script>
<script type="text/html" id="tableToolbar">
    <div class="layui-btn-container">
        <button class="layui-btn layui-btn-sm layui-bg-green" lay-event="reloadTable">刷新</button>
        <button class="layui-btn layui-btn-sm layui-bg-blue" id="switchApp" th:if="${showAppId}">
            统一APPID
            <i class="layui-icon layui-icon-down layui-font-12"></i>
        </button>
    </div>
</script>
<script type="text/html" id="downloadActionToolbar">
    <a class="layui-btn layui-btn-xs" lay-event="download">下载</a>
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="downloadCancel">取消</a>
</script>
<script type="text/html" id="importActionToolbar">
    {{#  if(d.allowRetry){ }}
    <a class="layui-btn layui-btn-xs" lay-event="importRetry">重试</a>
    {{#  } }}
    {{#  if(d.allowRevoke){ }}
    <a class="layui-btn layui-btn-danger layui-btn-xs" lay-event="importRevoke">撤销</a>
    {{#  } }}
    {{#  if(d.resultFileUrl){ }}
    <a class="layui-btn layui-btn-normal layui-btn-xs" lay-event="importResult">查看结果</a>
    {{#  } }}
</script>
<script type="text/javascript" th:src="@{/easyfile-ui/static/js/download-task.js}"></script>
</body>
</html>
```

- [x] **Step 2: Rename the nav entry to task management**

Update `index.html`:

```html
<li class="layui-nav-item"><a th:accesskey="table">任务管理</a></li>
```

- [x] **Step 3: Keep the nav routing stable**

Keep `index.js` pointing to `/easyfile-ui/download-task/get`:

```javascript
layui.use(['element', 'jquery'], function () {
  let element = layui.element;
  let $ = layui.jquery;
  element.on('nav(test)', function (data) {
    if (data.context.accessKey === "table") {
      $("#home").attr("src", "/easyfile-ui/download-task/get");
    }
  })
});
```

- [x] **Step 4: Run compile/resources verification**

Run:
```bash
mvn -pl easyfile-ui -am -DskipTests compile
```

Expected: build succeeds and the updated templates are copied into `target/classes`.

- [ ] **Step 5: Commit the shared page shell**

```bash
git add easyfile-ui/src/main/resources/easyfile-admin-ui/templates/download-task.html \
  easyfile-ui/src/main/resources/easyfile-admin-ui/templates/index.html \
  easyfile-ui/src/main/resources/easyfile-admin-ui/static/js/index.js
git commit -m "feat: convert admin task page into shared shell"
```

### Task 3: Refactor `download-task.js` into a task-type-driven page script

**Files:**
- Modify: `easyfile-ui/src/main/resources/easyfile-admin-ui/static/js/download-task.js`

- [x] **Step 1: Replace the download-only table definition with task-type state**

Use this structure:

```javascript
layui.use(['element', 'table', 'dropdown'], function () {

  var element = layui.element;
  var table = layui.table;
  var dropdown = layui.dropdown;
  var currentTaskType = 'download';
  var currentUnifiedAppId = '';

  function getListUrlByType(type) {
    return type === 'import' ? '/easyfile-ui/import-task/list' : '/easyfile-ui/download-task/list';
  }

  function getAppTreeUrlByType(type) {
    return type === 'import' ? '/easyfile-ui/import-task/listAppId' : '/easyfile-ui/download-task/listAppId';
  }

  function getActionToolbarByType(type) {
    return type === 'import' ? '#importActionToolbar' : '#downloadActionToolbar';
  }
```

- [x] **Step 2: Define separate column builders**

Add these helpers:

```javascript
  function progressCell(res) {
    return '<div class="layui-progress layui-progress-big" lay-showpercent="true">'
        + '<div class="layui-progress-bar" lay-percent="' + (res.executeProcess || 0) + '%"></div>'
        + '</div>';
  }

  function importSummaryCell(res) {
    return '成功 ' + (res.successCount || 0)
        + ' / 失败 ' + (res.failCount || 0)
        + ' / 总计 ' + (res.totalCount || 0);
  }

  function buildDownloadColumns() {
    return [[
      {field: 'registerId', width: 80, title: 'ID', sort: true},
      {field: 'uploadStatusDesc', width: 100, title: '处理状态'},
      {field: 'downloadCodeDesc', title: '下载描述', width: 150},
      {field: 'downloadOperateName', width: 120, title: '下载人', sort: true},
      {field: 'exportTime', width: 160, title: '下载时间', sort: true},
      {field: 'lastExecuteTime', width: 160, title: '最新执行时间'},
      {field: 'invalidTime', width: 160, title: '失效时间', sort: true},
      {field: 'downloadNum', width: 120, title: '下载次数', sort: true},
      {field: 'executeProcess', title: '执行进度', width: 200, templet: progressCell},
      {field: 'errorMsg', width: 200, title: '异常信息', sort: true},
      {fixed: 'right', title: '操作', toolbar: getActionToolbarByType('download'), width: 150}
    ]];
  }

  function buildImportColumns() {
    return [[
      {field: 'registerId', width: 80, title: 'ID', sort: true},
      {field: 'importStatusDesc', width: 110, title: '导入状态'},
      {field: 'importCodeDesc', width: 160, title: '导入描述'},
      {field: 'createBy', width: 120, title: '操作人'},
      {field: 'createTime', width: 160, title: '导入时间', sort: true},
      {field: 'lastExecuteTime', width: 160, title: '最新执行时间'},
      {title: '处理统计', width: 220, templet: importSummaryCell},
      {field: 'executeProcess', title: '执行进度', width: 200, templet: progressCell},
      {field: 'resultFileUrl', width: 120, title: '结果文件', templet: function (res) {
        if (res.resultFileUrl) return '可查看';
        if (res.importStatus === 'SUCCESS') return '无异常';
        if (res.importStatus === 'EXECUTING') return '生成中';
        return '无结果';
      }},
      {field: 'errorMsg', width: 200, title: '异常信息'},
      {fixed: 'right', title: '操作', toolbar: getActionToolbarByType('import'), width: 210}
    ]];
  }
```

- [x] **Step 3: Implement a shared table renderer and app-switch loader**

Use one `table.render()` and one `table.reload()` flow:

```javascript
  function renderTaskTable() {
    table.render({
      elem: '#table',
      id: 'table',
      height: 'full-300',
      toolbar: '#tableToolbar',
      defaultToolbar: ['filter', 'exports', 'print', {
        title: '帮助',
        layEvent: 'LAYTABLE_TIPS',
        icon: 'layui-icon-tips'
      }],
      url: getListUrlByType(currentTaskType) + (currentUnifiedAppId ? ('?unifiedAppId=' + currentUnifiedAppId) : ''),
      method: 'post',
      contentType: 'application/json',
      request: {
        pageName: 'pageNum',
        limitName: 'pageSize'
      },
      page: {
        layout: ['limit', 'count', 'prev', 'page', 'next', 'skip'],
        groups: 1
      },
      parseData: function (res) {
        return {
          code: res.code,
          msg: res.message,
          count: res.data ? res.data.totalRecords : 0,
          data: res.data ? res.data.modelList : []
        };
      },
      cols: currentTaskType === 'import' ? buildImportColumns() : buildDownloadColumns(),
      done: function () {
        element.render();
        renderAppDropdown();
      }
    });
  }

  function renderAppDropdown() {
    dropdown.render({
      elem: '#switchApp',
      align: 'center',
      data: loadAppTree(),
      click: function (obj) {
        currentUnifiedAppId = obj.id;
        reloadTaskTable();
      }
    });
  }

  function reloadTaskTable() {
    table.reload('table', {
      url: getListUrlByType(currentTaskType) + (currentUnifiedAppId ? ('?unifiedAppId=' + currentUnifiedAppId) : ''),
      cols: currentTaskType === 'import' ? buildImportColumns() : buildDownloadColumns(),
      page: {
        curr: 1
      }
    });
  }
```

- [x] **Step 4: Add tab switching and action handlers**

Include:

```javascript
  element.on('tab(taskTypeTab)', function () {
    currentTaskType = this.getAttribute('lay-id');
    reloadTaskTable();
  });
```

Add `table.on('tool(table)', ...)` branches for:
- `download`
- `downloadCancel`
- `importRetry`
- `importRevoke`
- `importResult`

Use existing AJAX style and `layer.confirm` / `layer.msg`.

- [x] **Step 5: Keep file download helpers and app-tree loading**

Retain `downloadURL`, `saveAs`, and a task-type-sensitive `loadAppTree()`:

```javascript
  function loadAppTree() {
    var array = [];
    var $ = layui.jquery;
    $.ajax({
      type: 'POST',
      url: getAppTreeUrlByType(currentTaskType),
      contentType: 'application/json',
      async: false,
      success: function (result) {
        if (result.success) {
          for (let i = 0; i < result.data.length; i++) {
            array.push({id: result.data[i].appId, title: result.data[i].appId});
          }
        }
      },
      dataType: 'json'
    });
    return array;
  }

  renderTaskTable();
});
```

- [x] **Step 6: Run UI module compile**

Run:
```bash
mvn -pl easyfile-ui -am -DskipTests compile
```

Expected: compile succeeds and JS/template resources are packaged.

- [ ] **Step 7: Commit the shared task-page script**

```bash
git add easyfile-ui/src/main/resources/easyfile-admin-ui/static/js/download-task.js
git commit -m "feat: add import mode to admin task page"
```

### Task 4: Provide the first local import admin storage implementation

**Files:**
- Modify: `easyfile-server/src/main/java/com/openquartz/easyfile/server/service/impl/DefaultImportStorageServiceImpl.java`

- [x] **Step 1: Extend `DefaultImportStorageServiceImpl` with admin methods**

Add the methods introduced in Task 1 using the existing JDBC style.

Suggested behavior:

```java
@Override
public Pagination<ImportTaskResult> listImportResult(ShowImportTaskRequest request) {
    return Pagination.empty();
}

@Override
public boolean retry(Long registerId, String operator) {
    return false;
}

@Override
public boolean revoke(Long registerId, String operator) {
    return false;
}

@Override
public DownloadUrlResult resultFile(Long registerId) {
    return null;
}

@Override
public List<AppTree> getAppTree() {
    return Collections.emptyList();
}
```

For the first slice, implement at least `listImportResult` with direct SQL over `ef_async_import_record` if the table
already exists in the local environment. If not, return `Pagination.empty()` while still wiring the controller and UI
end-to-end.

- [x] **Step 2: Map `ef_async_import_record` rows into `ImportTaskResult`**

Add a small private assembler in the same class:

```java
private ImportTaskResult assembleImportTaskResult(ResultSet rs) throws SQLException {
    ImportTaskResult result = new ImportTaskResult();
    result.setRegisterId(rs.getLong("id"));
    result.setImportStatus(rs.getString("import_status"));
    result.setImportStatusDesc(rs.getString("import_status"));
    result.setImportCode(rs.getString("import_code"));
    result.setImportCodeDesc(rs.getString("import_code"));
    result.setCreateBy(rs.getString("create_by"));
    result.setCreateTime(rs.getTimestamp("create_time"));
    result.setLastExecuteTime(rs.getTimestamp("last_execute_time"));
    result.setExecuteProcess(rs.getInt("execute_process"));
    result.setTotalCount(rs.getLong("total_count"));
    result.setSuccessCount(rs.getLong("success_count"));
    result.setFailCount(rs.getLong("fail_count"));
    result.setResultFileUrl(rs.getString("result_file_url"));
    result.setErrorMsg(rs.getString("error_msg"));
    String status = rs.getString("import_status");
    result.setAllowRetry("FAIL".equals(status) || "PARTIAL_SUCCESS".equals(status));
    result.setAllowRevoke("NONE".equals(status) || "EXECUTING".equals(status));
    return result;
}
```

- [x] **Step 3: Verify the thin fallback behavior**

Run:
```bash
mvn -pl easyfile-server,easyfile-ui -am -DskipTests compile
```

Expected: compile succeeds whether methods are real or fallback-backed.

- [ ] **Step 4: Commit the first local import admin integration**

```bash
git add easyfile-server/src/main/java/com/openquartz/easyfile/server/service/impl/DefaultImportStorageServiceImpl.java
git commit -m "feat: add import admin storage integration"
```

### Task 5: End-to-end verification

**Files:**
- Verify runtime behavior against:
  - `easyfile-ui/src/main/resources/easyfile-admin-ui/templates/download-task.html`
  - `easyfile-ui/src/main/resources/easyfile-admin-ui/static/js/download-task.js`
  - `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/controller/DownloadTaskController.java`
  - `easyfile-ui/src/main/java/com/openquartz/easyfile/admin/controller/ImportTaskController.java`

- [ ] **Step 1: Run the full relevant compile**

Run:
```bash
mvn -pl easyfile-ui,easyfile-server,easyfile-storage/easyfile-storage-api -am -DskipTests compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 2: Start the local admin app if available**

Run the existing example or local app entrypoint used by this workspace for admin verification.
Expected: `/easyfile-ui/` loads and the `任务管理` page renders.

- [ ] **Step 3: Verify the shared page manually**

Check:
- download tab still loads data
- import tab switches columns
- refresh works in both tabs
- unified AppId dropdown still renders
- import action buttons render only when flags allow

- [ ] **Step 4: Commit any final polish**

```bash
git add easyfile-ui easyfile-server easyfile-storage/easyfile-storage-api
git commit -m "feat: support import task management in admin"
```

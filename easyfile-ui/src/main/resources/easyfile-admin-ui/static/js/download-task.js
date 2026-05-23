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
      {fixed: 'right', title: '操作', toolbar: '#downloadActionToolbar', width: 150}
    ]];
  }

  function buildImportColumns() {
    return [[
      {field: 'registerId', width: 80, title: 'ID', sort: true},
      {field: 'importStatusDesc', width: 110, title: '导入状态'},
      {field: 'importCodeDesc', title: '导入描述', width: 150},
      {field: 'createBy', width: 120, title: '操作人'},
      {field: 'createTime', width: 160, title: '导入时间', sort: true},
      {field: 'lastExecuteTime', width: 160, title: '最新执行时间'},
      {title: '处理统计', width: 220, templet: importSummaryCell},
      {field: 'executeProcess', title: '执行进度', width: 200, templet: progressCell},
      {
        field: 'resultFileUrl',
        title: '结果文件',
        width: 120,
        templet: function (res) {
          if (res.resultFileUrl) {
            return '可查看';
          }
          if (res.importStatus === 'SUCCESS') {
            return '无异常';
          }
          if (res.importStatus === 'EXECUTING') {
            return '生成中';
          }
          return '无结果';
        }
      },
      {field: 'errorMsg', width: 200, title: '异常信息'},
      {fixed: 'right', title: '操作', toolbar: '#importActionToolbar', width: 230}
    ]];
  }

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

  function reloadTaskTable() {
    table.reload('table', {
      url: getListUrlByType(currentTaskType) + (currentUnifiedAppId ? ('?unifiedAppId=' + currentUnifiedAppId) : ''),
      cols: currentTaskType === 'import' ? buildImportColumns() : buildDownloadColumns(),
      page: {
        curr: 1
      }
    });
  }

  table.on('toolbar(table)', function (obj) {
    switch (obj.event) {
      case 'reloadTable':
        reloadTaskTable();
        break;
      case 'LAYTABLE_TIPS':
        window.location.href = 'https://github.com/svnlab/easyfile';
        break;
    }
  });

  table.on('tool(table)', function (obj) {
    var data = obj.data;
    var $ = layui.jquery;
    if (obj.event === 'download') {
      layer.confirm('真的要下载吗?', {icon: 3, title: '提示'}, function (index) {
        var requestBody = {registerId: data.registerId};
        $.ajax({
          type: 'POST',
          url: '/easyfile-ui/download-task/download',
          data: JSON.stringify(requestBody),
          contentType: 'application/json',
          success: function (result) {
            if (result.success) {
              downloadURL(result.data.url, result.data.fileName);
              layer.msg('下载成功!');
            } else {
              layer.msg('下载失败!失败原因:' + result.message);
            }
          },
          error: function (xhr, status, error) {
            layer.msg('下载失败!' + error);
          },
          dataType: 'json'
        });
        layer.close(index);
      });
      return;
    }

    if (obj.event === 'downloadCancel') {
      layer.confirm('真的要取消下载任务: ' + data.registerId + ' 吗?', {icon: 3, title: '提示'}, function (index) {
        var requestBody = {registerId: data.registerId};
        $.ajax({
          type: 'POST',
          url: '/easyfile-ui/download-task/revoke',
          data: JSON.stringify(requestBody),
          contentType: 'application/json',
          success: function (result) {
            if (result.success) {
              layer.msg('取消成功！');
              reloadTaskTable();
            } else {
              layer.msg('取消失败!失败原因:' + result.message);
            }
          },
          error: function (xhr, status, error) {
            layer.msg('取消失败!' + error);
          },
          dataType: 'json'
        });
        layer.close(index);
      });
      return;
    }

    if (obj.event === 'importResult') {
      var requestBody = {registerId: data.registerId};
      $.ajax({
        type: 'POST',
        url: '/easyfile-ui/import-task/result',
        data: JSON.stringify(requestBody),
        contentType: 'application/json',
        success: function (result) {
          if (result.success && result.data) {
            downloadURL(result.data.url, result.data.fileName);
          } else {
            layer.msg('结果文件暂不可用');
          }
        },
        error: function (xhr, status, error) {
          layer.msg('获取结果失败!' + error);
        },
        dataType: 'json'
      });
      return;
    }

    if (obj.event === 'importRetry') {
      layer.confirm('真的要重试导入任务: ' + data.registerId + ' 吗?', {icon: 3, title: '提示'}, function (index) {
        var requestBody = {registerId: data.registerId};
        $.ajax({
          type: 'POST',
          url: '/easyfile-ui/import-task/retry',
          data: JSON.stringify(requestBody),
          contentType: 'application/json',
          success: function (result) {
            if (result.success) {
              layer.msg('重试成功！');
              reloadTaskTable();
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
      return;
    }

    if (obj.event === 'importRevoke') {
      layer.confirm('真的要撤销导入任务: ' + data.registerId + ' 吗?', {icon: 3, title: '提示'}, function (index) {
        var requestBody = {registerId: data.registerId};
        $.ajax({
          type: 'POST',
          url: '/easyfile-ui/import-task/revoke',
          data: JSON.stringify(requestBody),
          contentType: 'application/json',
          success: function (result) {
            if (result.success) {
              layer.msg('撤销成功！');
              reloadTaskTable();
            } else {
              layer.msg('撤销失败!失败原因:' + result.message);
            }
          },
          error: function (xhr, status, error) {
            layer.msg('撤销失败!' + error);
          },
          dataType: 'json'
        });
        layer.close(index);
      });
    }
  });

  element.on('tab(taskTypeTab)', function () {
    currentTaskType = this.getAttribute('lay-id');
    reloadTaskTable();
  });

  renderTaskTable();
});

function downloadURL(url, filename) {
  url = url.replace(/\\/g, '/');
  const xhr = new XMLHttpRequest();
  xhr.open('GET', url, true);
  xhr.responseType = 'blob';
  xhr.onload = () => {
    if (xhr.status === 200) {
      saveAs(xhr.response, filename);
    }
  };
  xhr.send();
}

function saveAs(data, name) {
  var urlObject = window.URL || window.webkitURL || window;
  var exportBlob = new Blob([data]);
  var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a');
  saveLink.href = urlObject.createObjectURL(exportBlob);
  saveLink.download = name;
  saveLink.click();
}

layui.use(['element', 'table', 'dropdown'], function () {

  var element = layui.element;
  var table = layui.table;
  var dropdown = layui.dropdown;

  table.render({
    elem: '#table'
    , height: 'full-300'
    , toolbar: '#tableToolbar'
    , defaultToolbar: ['filter', 'exports', 'print', {
      title: '帮助'
      , layEvent: 'LAYTABLE_TIPS'
      , icon: 'layui-icon-tips'
    }]
    , url: '/easyfile-ui/download-task/list'
    , method: 'post'
    , contentType: 'application/json'
    , request: {
      pageName: 'pageNum'
      , limitName: 'pageSize'
    }
    , page: {
      layout: ['limit', 'count', 'prev', 'page', 'next', 'skip']
      , groups: 1
    }
    , parseData: function (res) {
      return {
        "code": res.code,
        "msg": res.message,
        "count": res.data.totalRecords,
        "data": res.data.modelList
      };
    }
    , cols: [[
      {field: 'registerId', width: 80, title: 'ID', sort: true}
      , {field: 'uploadStatusDesc', width: 100, title: '处理状态'}
      , {field: 'downloadCodeDesc', title: '下载描述', width: 150}
      , {field: 'downloadOperateName', width: 120, title: '下载人', sort: true}
      , {field: 'exportTime', width: 160, title: '下载时间', sort: true}
      , {field: 'lastExecuteTime', width: 160, title: '最新执行时间'}
      , {field: 'invalidTime', width: 160, title: '失效时间', sort: true}
      , {field: 'downloadNum', width: 120, title: '下载次数', sort: true}
      , {
        field: 'executeProcess',
        title: '执行进度',
        width: 200,
        templet: function (res) {
          return '<div class="layui-progress layui-progress-big" lay-showpercent="true">\n'
              + '  <div class="layui-progress-bar" lay-percent="'
              + res.executeProcess + '%"></div>\n'
              + '</div>';
        }
      }
      , {field: 'errorMsg', width: 200, title: '异常信息', sort: true}
      , {fixed: 'right', title: '操作', toolbar: '#clickDownload', width: 150}
    ]],
    done: function (res, curr, count) {
      element.render();

      var id = this.id;

      // 重载测试
      dropdown.render({
        elem: '#switchApp' //可绑定在任意元素中，此处以上述按钮为例
        , align: 'center'
        , data: loadAppTree()
        // 菜单被点击的事件
        , click:
            function (obj) {
              // 数据重载 - 参数重置
              table.reloadData('table', {
                url: '/easyfile-ui/download-task/list' + "?unifiedAppId="
                    + obj.id
                , method: 'post'
                , contentType: 'application/json'
                , request: {
                  pageName: 'pageNum'
                  , limitName: 'pageSize'
                }
                , scrollPos: 'fixed'  // 保持滚动条位置不变 - v2.7.3 新增
                , height: 2000 // 测试无效参数（即与数据无关的参数设置无效，此处以 height 设置无效为例）
                //,url: '404'
                //,page: {curr: 1, limit: 30} // 重新指向分页
              });
            }
      });
    }
  });

  // 工具栏事件
  table.on('toolbar(table)', function (obj) {

    var id = obj.config.id;
    var checkStatus = table.checkStatus(id);
    var othis = lay(this);
    switch (obj.event) {

      case 'reloadTable':
        table.reload('table');
        break;
    }
  });

  //监听行工具事件
  table.on('tool(table)', function (obj) {
    var $ = layui.jquery;
    var data = obj.data;
    if (obj.event === 'click') {
      layer.confirm('真的要下载吗?', {icon: 3, title: '提示'}, function (index) {

        var requestBody = {
          registerId: data.registerId
        };

        $.ajax({
          type: 'POST',
          url: '/easyfile-ui/download-task/download',
          data: JSON.stringify(requestBody),
          contentType: 'application/json',
          success: function (result, status, xhr) {
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
    } else if (obj.event === 'cancel') {
      layer.confirm('真的要取消下载任务: ' + data.registerId + ' 吗?',
          {icon: 3, title: '提示'}, function (index) {

            var requestBody = {
              registerId: data.registerId
            };

            $.ajax({
              type: 'POST',
              url: '/easyfile-ui/download-task/revoke',
              data: JSON.stringify(requestBody),
              contentType: 'application/json',
              success: function (result, status, xhr) {
                if (result.success) {
                  layer.msg('取消成功！');
                } else {
                  layer.msg('取消失败!失败原因:' + result.message);
                }
              },
              error: function (xhr, status, error) {
                layer.msg('下载失败!' + error);
              },
              dataType: 'json'
            });

            layer.close(index);
          });
    }
  });
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
  var export_blob = new Blob([data]);
  var save_link = document.createElementNS('http://www.w3.org/1999/xhtml', 'a');
  save_link.href = urlObject.createObjectURL(export_blob);
  save_link.download = name;
  save_link.click();
}

function loadAppTree() {

  var array = [];

  var $ = layui.jquery;

  $.ajax({
    type: 'POST',
    url: '/easyfile-ui/download-task/listAppId',
    contentType: 'application/json',
    success: function (result, status, xhr) {
      if (result.success) {
        for (let i = 0; i < result.data.length; i++) {
          array.push({id: result.data[i].appId, title: result.data[i].appId});
        }
      }
    },
    error: function (xhr, status, error) {
    },
    dataType: 'json'
  });
  return array;
}
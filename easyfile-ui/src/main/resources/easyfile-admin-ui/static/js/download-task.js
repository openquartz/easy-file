layui.use(['element', 'table'], function () {

  var element = layui.element;
  var table = layui.table;

  table.render({
    elem: '#table'
    , height: 'full'
    , url: '/easyfile-ui/download-task/list'
    , method: 'post'
    , contentType: 'application/json'
    , request: {
      pageName: 'pageNum' //页码的参数名称，默认：page
      , limitName: 'pageSize'
    }
    , page: { //支持传入 laypage 组件的所有参数（某些参数除外，如：jump/elem） - 详见文档
      layout: ['limit', 'count', 'prev', 'page', 'next', 'skip'] //自定义分页布局
      //,curr: 5 //设定初始在第 5 页
      , groups: 1 //只显示 1 个连续页码
      , first: false //不显示首页
      , last: false //不显示尾页
    }
    , parseData: function (res) { //res 即为原始返回的数据
      return {
        "code": res.code, //解析接口状态
        "msg": res.message, //解析提示文本
        "count": res.data.totalRecords, //解析数据长度
        "data": res.data.modelList //解析数据列表
      };
    }
    , cols: [[
      {field: 'registerId', width: 80, title: 'ID', sort: true}
      , {field: 'uploadStatusDesc', width: 100, title: '处理状态'}
      , {field: 'downloadCodeDesc', title: '下载描述', width: 150}
      , {field: 'downloadOperateName', width: 120, title: '下载人', sort: true}
      , {field: 'exportTime', width: 120, title: '下载时间', sort: true}
      , {field: 'lastExecuteTime', width: 120, title: '最新执行时间'}
      , {field: 'invalidTime', width: 120, title: '失效时间', sort: true}
      , {field: 'downloadNum', width: 120, title: '下载次数', sort: true}
      , {
        field: 'executeProcess',
        title: '执行进度',
        width: 150,
        templet: function (res) {
          return '<div class="layui-progress layui-progress-big" lay-showpercent="true">\n'
              + '  <div class="layui-progress-bar" lay-percent="'+res.executeProcess+'%"></div>\n'
              + '</div>';
        }
      }
      , {field: 'errorMsg', width: 200, title: '异常信息', sort: true}
      , {fixed: 'right', title: '操作', toolbar: '#clickDownload', width: 150}
    ]],
    done: function (res, curr, count) {
      element.render();
    }
  });

  //监听行工具事件
  table.on('tool(table)', function (obj) {
    var $ = layui.jquery;
    var data = obj.data;
    if (obj.event === 'click') {
      layer.confirm('真的要下载吗?', {icon: 3, title: '提示'}, function (index) {
        //do something

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
              layer.msg('下载成功！');
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
            //do something

            layer.close(index);
          });
    }
  });

});


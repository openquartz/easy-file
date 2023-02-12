layui.use('table', function () {
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
      {checkbox: true, fixed: true}
      , {field: 'registerId', width: 80, title: 'ID', sort: true}
      , {field: 'uploadStatusDesc', width: 100, title: '执行状态'}
      , {field: 'downloadCodeDesc', title: '下载描述', width: 150}
      , {field: 'downloadOperateName', width: 120, title: '下载人', sort: true}
      , {field: 'exportTime', width: 120, title: '下载时间', sort: true}
      , {field: 'lastExecuteTime', width: 120, title: '最新执行时间'}
      , {field: 'invalidTime', width: 120, title: '失效时间', sort: true}
      , {field: 'downloadNum', width: 120, title: '下载次数', sort: true}
      , {field: 'executeProcess', width: 120, title: '执行进度', sort: true}
      , {field: 'errorMsg', width: 200, title: '异常信息', sort: true}
    ]]
  });
});
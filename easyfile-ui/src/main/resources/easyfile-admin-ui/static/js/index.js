//JavaScript代码区域
layui.use(['element', 'jquery'], function () {
  let element = layui.element;
  let $ = layui.jquery;
  //监听导航栏的事件
  //跳转到表格的界面
  element.on('nav(test)', function (data) {
    if (data.context.accessKey === "table") {
      $("#home").attr("src", "/easyfile-ui/download-task/get");
    }
  })
});

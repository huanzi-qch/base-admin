let sysNoticeTextEdit;
layui.use(['colorpicker','form'], function () {
    let form = layui.form;//select、单选、复选等依赖form
    let colorpicker = layui.colorpicker;

    //开启全功能
    colorpicker.render({
        elem: '#test-form-sysColor'
        ,color: $('#sysColor').val()
        ,format: 'rgb'
        ,predefine: true
        ,alpha: true
        ,done: function(color){
            $('#sysColor').val(color);
        }
        ,change: function(color){
            //给当前页面头部和左侧设置主题色
            $('.header-demo,.layui-side .layui-nav').css('background-color', color);
        }
    });

    //建立编辑器
    sysNoticeTextEdit = UE.getEditor('sysNoticeTextEdit');
    //回显
    sysNoticeTextEdit.ready(function() {
        sysNoticeTextEdit.setContent($("#sysNoticeText").val());
    });

    //radio checkbox
    $("#sysForm").find("[name='sysApiEncrypt'][value='" + $("#sysApiEncrypt").val() + "']").attr("checked", true);
    $("#sysForm").find("[name='sysOpenApiLimiterEncrypt'][value='" + $("#sysOpenApiLimiterEncrypt").val() + "']").attr("checked", true);
    $("#sysForm").find("[name='sysCheckPwdEncrypt'][value='" + $("#sysCheckPwdEncrypt").val() + "']").attr("checked", true);
    form.render();
});

/**
 * 提交保存
 */
function sysFormSave() {
    let serializeObject = $("#sysForm").serializeObject();
    //获取编辑器内容
    serializeObject.sysNoticeText = sysNoticeTextEdit.getContent();
    $.post(ctx + "/sys/sysSetting/save", serializeObject, function (data) {
        if(!data.flag){
            layer.msg(data.msg, {icon: 2, time: 2000}, function () {});
            return;
        }
        layer.msg("修改成功！", {icon: 1, time: 2000}, function () {});
        $("#sysForm").form(data.data);
        $("#sysApiEncrypt").val(data.data.sysApiEncrypt)
    });
}
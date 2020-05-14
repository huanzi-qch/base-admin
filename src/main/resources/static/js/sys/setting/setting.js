let sysNoticeTextEdit;
layui.use(['form'], function () {
    let form = layui.form;//select、单选、复选等依赖form

    //建立编辑器
    sysNoticeTextEdit = UE.getEditor('sysNoticeTextEdit');
    //回显
    sysNoticeTextEdit.ready(function() {
        sysNoticeTextEdit.setContent($("#sysNoticeText").val());
    });
});

/**
 * 提交保存
 */
function sysFormSave() {
    let serializeObject = $("#sysForm").serializeObject();
    //获取编辑器内容
    serializeObject.sysNoticeText = sysNoticeTextEdit.getContent();
    $.post(ctx + "/sys/sysSetting/save", serializeObject, function (data) {
        layer.msg("修改成功！", {icon: 1, time: 2000}, function () {});
        $("#sysForm").form(data.data);
    });
}
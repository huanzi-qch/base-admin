layui.use(['form'], function () {
    let form = layui.form;//select、单选、复选等依赖form
});

/**
 * 提交保存
 */
function sysFormSave() {
    $.post(ctx + "/sys/sysSetting/save", $("#sysForm").serializeObject(), function (data) {
        layer.msg("修改成功！", {icon: 1, time: 2000}, function () {});
        $("#sysForm").form(data.data);
    });
}
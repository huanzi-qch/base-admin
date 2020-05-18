layui.use(['form'], function () {
    let form = layui.form;//select、单选、复选等依赖form
});

/**
 * 提交保存
 */
function userFormSave() {
    $.post(ctx + "/user/updateUser", $("#userForm").serializeObject(), function (data) {
        layer.msg("修改成功！", {icon: 1, time: 2000}, function () {});
        $("#userForm").form(data.data);
    });
}

/**
 * 修改密码
 */
function updatePassword() {
    let html = "<form id=\"updatePassword\" class=\"layui-form layui-form-pane\">\n" +
        "\t<div class=\"layui-form-item\">\n" +
        "\t\t<label class=\"layui-form-label\" style='width: 110px !important;'>原密码</label>\n" +
        "\t\t<div class=\"layui-input-block\">\n" +
        "\t\t\t<input type=\"text\" name=\"oldPassword\" autocomplete=\"off\"\n" +
        "\t\t\t\t   placeholder=\"原密码\" class=\"layui-input\">\n" +
        "\t\t</div>\n" +
        "\t</div>\n" +
        "\t<div class=\"layui-form-item\">\n" +
        "\t\t<label class=\"layui-form-label\"  style='width: 110px !important;'>新密码</label>\n" +
        "\t\t<div class=\"layui-input-block\">\n" +
        "\t\t\t<input type=\"text\" name=\"newPassword\" autocomplete=\"off\"\n" +
        "\t\t\t\t   placeholder=\"新密码\" class=\"layui-input\">\n" +
        "\t\t</div>\n" +
        "\t</div>\n" +
        "\t<div class=\"layui-form-item\">\n" +
        "\t\t<div class=\"layui-input-block\">\n" +
        "\t\t\t<a class=\"layui-btn\" onclick=\"" +
        "    $.post(ctx + '/user/updatePassword', $('#updatePassword').serializeObject(), function (data) {\n" +
        "        if (data.flag) {\n" +
        "            layer.msg('修改密码成功，请重新登录系统！', {icon: 1, time: 2000}, function () {\n" +
        "                window.parent.location.href = ctx + '/logout';\n" +
        "            });\n" +
        "        }else{\n" +
        "            layer.msg(data.msg, {icon: 2, time: 2000}, function () {});\n" +
        "        }\n" +
        "    });" +
        "\">修改</a>\n" +
        "\t\t</div>\n" +
        "\t</div>\n" +
        "</form>";
    //iframe层-父子操作
    layer.open({
        title: '修改密码',
        type: 1,
        area: ['400px', '250px'],
        fixed: false, //不固定
        maxmin: true,
        content: html
    });
}
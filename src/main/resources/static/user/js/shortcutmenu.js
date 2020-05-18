let tree = {};
layui.use(['element', 'form', 'table', 'layer', 'tree', 'util'], function () {
    let form = layui.form;//select、单选、复选等依赖form
    let element = layui.element; //导航的hover效果、二级菜单等功能，需要依赖element模块
    tree = layui.tree;

    //获取菜单数据
    $.post(ctx + "/user/shortcutMenuListByTier", {}, function (data) {
        //数据说明：id对应id，title对应shortcutMenuName，href对应menuPath
        let treeData = updateKeyForLayuiTree(data.data);

        //开启节点操作图标
        tree.render({
            elem: '#shortcutMenuTree'
            , id: 'shortcutMenuTree'
            , data: [{
                title: '用户个性菜单根节点'
                , href: "/"
                , id: "0"
                , spread: true
                , children: treeData
            }]
            , onlyIconControl: true
            , edit: ['add', 'del']
            //节点被点击
            , click: function (obj) {
                $("#shortcutMenuForm").form({
                    shortcutMenuId: obj.data.id,
                    shortcutMenuName: obj.data.title,
                    shortcutMenuPath: obj.data.href,
                    shortcutMenuParentName: obj.elem.parent().parent().children(".layui-tree-entry").find(".layui-tree-txt").text(),
                    shortcutMenuParentId: obj.elem.parent().parent().data("id"),
                    treeId: obj.data.id
                });
            }
            //对节点进行增删改操作回调
            , operate: function (obj) {
                let type = obj.type; //得到操作类型：add、edit、del
                let data = obj.data; //得到当前节点的数据
                let elem = obj.elem; //得到当前节点元素

                if (type === 'add') { //增加节点
                    $("#shortcutMenuForm")[0].reset();
                    //返回 key 值
                    return "";
                } else if (type === 'del') { //删除节点
                    layer.confirm('确认要删除这个菜单吗？/n注意：删除父节点将会一同删除子节点', function (index) {
                        $.delete(ctx + "/user/shortcutMenuDelete/" + data.id, {}, function (data) {
                            if (data.flag) {
                                layer.msg("删除成功");
                                elem.remove();
                            } else {
                                layer.msg(data.msg, {icon: 2, time: 2000}, function () {
                                });
                            }
                        });
                        layer.close(index);
                    });

                }
            }
        });
    });
});


/**
 * 提交保存
 */
function shortcutMenuFormSave() {
    let shortcutMenuForm = $("#shortcutMenuForm").serializeObject();
    if (shortcutMenuForm.shortcutMenuId === "0") {
        layer.msg("根节点仅用于展示，不可操作！", {icon: 2, time: 2000}, function () {
        });
        return;
    }
    if (shortcutMenuForm.shortcutMenuParentId === "0") {
        shortcutMenuForm.shortcutMenuParentId = "";
    }
    $.post(ctx + "/user/shortcutMenuSave", shortcutMenuForm, function (data) {
        layer.msg("保存成功", {icon: 1, time: 2000}, function () {
        });

        //更新树组件
        $("div[data-id='" + shortcutMenuForm.treeId + "']").children(".layui-tree-entry").find(".layui-tree-txt").text(data.data.shortcutMenuName);
        $("div[data-id='" + shortcutMenuForm.treeId + "']").attr("data-id", data.data.shortcutMenuId);
    });
}

/**
 * 将我们响应的用户个性菜单数据转换成符合layui的tree结构
 * @param arrar  旧数据
 * @returns {Array} 新数据
 */
function updateKeyForLayuiTree(arrar) {
    let newArray = [];
    for (let i = 0; i < arrar.length; i++) {
        let obj1 = {};
        let obj = arrar[i];
        obj1.id = obj.shortcutMenuId;
        obj1.title = obj.shortcutMenuName;
        obj1.href = obj.shortcutMenuPath;

        if (obj.children.length > 0) {
            obj1.children = updateKeyForLayuiTree(obj.children);
        }
        newArray.push(obj1);
    }
    return newArray
}
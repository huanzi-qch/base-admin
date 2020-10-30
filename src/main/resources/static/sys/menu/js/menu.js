let tree = {};
layui.use(['element', 'form', 'table', 'layer', 'tree', 'util'], function () {
    let form = layui.form;//select、单选、复选等依赖form
    let element = layui.element; //导航的hover效果、二级菜单等功能，需要依赖element模块
    tree = layui.tree;

    //获取菜单数据
    $.post(ctx + "/sys/sysMenu/listByTier", {}, function (data) {
        //数据说明：id对应id，title对应menuName，href对应menuPath
        let treeData = commonUtil.updateKeyForLayuiTree(data.data);

        //开启节点操作图标
        tree.render({
            elem: '#menuTree'
            , id: 'menuTree'
            , data: [{
                title: '系统菜单根节点'
                , href: "/"
                , id: "0"
                , spread: true
                , children: treeData
            }]
            , onlyIconControl: true
            , edit: ['add', 'del']
            //节点被点击
            , click: function (obj) {
                //回显操作表单，说明：menuId对应id，title对应menuName，href对应menuPath
                $("#menuForm").form({
                    menuId: obj.data.id,
                    menuName: obj.data.title,
                    menuPath: obj.data.href,
                    menuParentName: obj.elem.parent().parent().children(".layui-tree-entry").find(".layui-tree-txt").text(),
                    menuParentId: obj.elem.parent().parent().data("id"),
                    treeId: obj.data.id
                });
            }
            //复选框被点击
            , oncheck: function (obj) {
                console.log(obj.data); //得到当前点击的节点数据
                console.log(obj.checked); //得到当前节点的展开状态：open、close、normal
                console.log(obj.elem); //得到当前节点元素
            }
            //对节点进行增删改操作回调
            , operate: function (obj) {
                let type = obj.type; //得到操作类型：add、edit、del
                let data = obj.data; //得到当前节点的数据
                let elem = obj.elem; //得到当前节点元素

                if (type === 'add') { //增加节点
                    $("#menuForm")[0].reset();
                    //返回 key 值
                    return "";
                } else if (type === 'del') { //删除节点，PS：存在layer.confirm弹窗非阻塞问题，未点确定，页面上的节点就被删除，如有解决方案的同学可以跟我说下
                    layer.confirm('确认要删除这个菜单吗？\n注意：删除父节点将会一同删除子节点', function (index) {
                        $.delete(ctx + "/sys/sysMenu/delete/" + data.id,{}, function () {
                            layer.msg("删除成功");
                            elem.remove();
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
function menuFormSave() {
    var menuForm = $("#menuForm").serializeObject();
    if (menuForm.menuParentId === "") {
        return;
    }
    if(menuForm.menuId === "0"){
        layer.msg("根节点仅用于展示，不可操作！", {icon: 2,time: 2000}, function () {});
        return;
    }
    if(menuForm.menuParentId === "0"){
        menuForm.menuParentId = "";
    }
    $.post(ctx + "/sys/sysMenu/save", menuForm, function (data) {
        layer.msg("保存成功", {icon: 1,time: 2000}, function () {});

        //更新树组件
        $("div[data-id='" + menuForm.treeId + "']").children(".layui-tree-entry").find(".layui-tree-txt").text(data.data.menuName);
        $("div[data-id='" + menuForm.treeId + "']").attr("data-id", data.data.menuId);
    });
}
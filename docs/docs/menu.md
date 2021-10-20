## 菜单管理 <br/>
　　菜单管理是一棵layui的Tree，支持N层菜单结构、支持按权重排序<br/>

　　增删改<br/>
![](https://img2018.cnblogs.com/blog/1353055/201909/1353055-20190917115210905-1262864719.gif)<br/>
![](https://img2020.cnblogs.com/blog/1353055/202105/1353055-20210518172549395-1167820552.png)<br/>

```
/**
 * 菜单工具类
 */
public class MenuUtil {

    /**
     * 递归获取子节点
     * @param id 父节点id
     * @param allMenu 所有菜单列表
     * @return 每个根节点下，所有子菜单列表
     */
    public static List<SysMenuVo> getChildBySysMenuVo(String id, List<SysMenuVo> allMenu){
        //子菜单
        List<SysMenuVo> childList = new ArrayList<>(allMenu.size());
        for (SysMenuVo nav : allMenu) {
            // 遍历所有节点，将所有菜单的父id与传过来的根节点的id比较
            //相等说明：为该根节点的子节点。
            if (nav.getMenuParentId().equals(id)){
                childList.add(nav);
            }
        }
        //递归
        for (SysMenuVo nav : childList) {
            nav.setChildren(getChildBySysMenuVo(nav.getMenuId(), allMenu));
        }
        childList.sort(orderBySysMenuVo()); //排序
        return childList;
    }

    /**
     * 排序,根据sortWeight排序
     */
    private static Comparator<SysMenuVo> orderBySysMenuVo(){
        return (o1, o2) -> {
            if (!o1.getSortWeight().equals(o2.getSortWeight())){
                return o1.getSortWeight() - o2.getSortWeight();
            }
            return 0 ;
        };
    }


    /**
     * 递归获取子节点
     * @param id 父节点id
     * @param allMenu 所有菜单列表
     * @return 每个根节点下，所有子菜单列表
     */
    public static List<SysShortcutMenuVo> getChildBySysShortcutMenuVo(String id, List<SysShortcutMenuVo> allMenu){
        //子菜单
        List<SysShortcutMenuVo> childList = new ArrayList<>(allMenu.size());
        for (SysShortcutMenuVo nav : allMenu) {
            // 遍历所有节点，将所有菜单的父id与传过来的根节点的id比较
            //相等说明：为该根节点的子节点。
            if (nav.getShortcutMenuParentId().equals(id)){
                childList.add(nav);
            }
        }
        //递归
        for (SysShortcutMenuVo nav : childList) {
            nav.setChildren(getChildBySysShortcutMenuVo(nav.getShortcutMenuId(), allMenu));
        }
        childList.sort(orderBySysShortcutMenuVo()); //排序
        return childList;
    }

    /**
     * 排序,根据sortWeight排序
     */
    private static Comparator<SysShortcutMenuVo> orderBySysShortcutMenuVo(){
        return (o1, o2) -> {
            if (!o1.getSortWeight().equals(o2.getSortWeight())){
                return o1.getSortWeight() - o2.getSortWeight();
            }
            return 0 ;
        };
    }
}

递归工具类使用
public Result<List<SysMenuVo>> listByTier(SysMenuVo entityVo) {
    List<SysMenuVo> sysMenuVoList = super.list(entityVo).getData();
    return Result.of(MenuUtil.getChildBySysMenuVo("",sysMenuVoList));
}
```
```
<!-- 递归用户系统菜单模板 -->
<th:block th:fragment="sysMenu(menuList)">
    <dd class="layui-nav-item" th:each="menu,iterStat : ${menuList}">
        <a th:text="${menu.menuName}"
           th:data-url="${#request.getContextPath() + menu.menuPath}"
           th:data-id="${menu.menuId}" class="huanzi-menu" href="javascript:;">XXX菜单</a>
        <dl class="layui-nav-child" th:if="${#lists.size(menu.children)} > 0">
            <th:block th:include="this::sysMenu(${menu.children})"></th:block>
        </dl>
    </dd>
</th:block>

<!-- 递归用户个性菜单模板 -->
<th:block th:fragment="shortcutMenu(menuList)">
    <dd th:each="menu,iterStat : ${menuList}">
        <a th:text="${menu.shortcutMenuName}"
           th:data-url="${menu.shortcutMenuPath.indexOf('http') == -1 ? #request.getContextPath() + menu.shortcutMenuPath : menu.shortcutMenuPath}"
           th:data-id="${menu.shortcutMenuId}" class="huanzi-menu" href="javascript:;">XXX菜单</a>
        <dl class="layui-nav-child" style="position:unset;" th:if="${#lists.size(menu.children)} > 0">
            <th:block th:include="this::shortcutMenu(${menu.children})"></th:block>
        </dl>
    </dd>
</th:block>

thymeleaf递归使用
<ul class="layui-nav layui-nav-tree" lay-filter="test" lay-shrink="all" th:style="${' background-color:' + sys.sysColor + ' !important;'}">
    <!-- 动态读取加载系统菜单 -->
    <li class="layui-nav-item" th:each="menu,iterStat : ${menuList}">
        <a th:text="${menu.menuName}"
           th:data-url="${#request.getContextPath() + menu.menuPath}"
           th:data-id="${menu.menuId}" class="huanzi-menu" href="javascript:;">XXX菜单</a>
        <dl class="layui-nav-child" th:if="${#lists.size(menu.children)} > 0">
            <th:block th:include="common/head::sysMenu(${menu.children})"></th:block>
        </dl>
    </li>
</ul>
```

<br/><br/>

>设置好菜单后记得要在用户管理给用户配置菜单，给用户配什么菜单用户就能看见什么菜单
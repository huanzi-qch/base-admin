package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.sys.sysmenu.vo.SysMenuVo;
import cn.huanzi.qch.baseadmin.sys.sysshortcutmenu.vo.SysShortcutMenuVo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

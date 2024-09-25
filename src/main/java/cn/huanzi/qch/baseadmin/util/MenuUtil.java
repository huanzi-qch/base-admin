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
     * 根据父id，递归获取所有子节点
     * @param pId 父节点id
     * @param allMenu 所有菜单列表
     * @return 每个根节点下，所有子菜单列表
     */
    /*
        结果示例：
        [
            {
                "menu_parent_id":"-1",
                "menu_id":"1",
                "menu_name":"系统管理",
                "children":[
                        {
                            "menu_parent_id":"1",
                            "menu_id":"11",
                            "menu_name":"用户管理",
                        },
                        {
                            "menu_parent_id":"1",
                            "menu_id":"12",
                            "menu_name":"系统设置",
                            "children":[
                                {
                                    "menu_parent_id":"12",
                                    "menu_id":"121",
                                    "menu_name":"系统安全",
                                },
                                {
                                    "menu_parent_id":"12",
                                    "menu_id":"122",
                                    "menu_name":"系统维护",
                                },
                            ],
                        },
                ],
            },
            {
                "menu_parent_id":"-1",
                "menu_id":"2",
                "menu_name":"会议室管理",
            },
        ]
     */
    public static List<SysMenuVo> getSysMenuChildByPid(String pId, List<SysMenuVo> allMenu){
        //子节点
        List<SysMenuVo> childList = new ArrayList<>(allMenu.size());
        for (int i = 0; i < allMenu.size(); i++) {
            SysMenuVo nav = allMenu.get(i);

            // 遍历所有节点，将所有菜单的父id与传过来的根节点的id比较
            //相等说明：为该根节点的子节点。
            if (nav.getMenuParentId().equals(pId)){
                childList.add(nav);

                //删除，减少下次循环次数
                allMenu.remove(i);
                i--;
            }
        }
        //递归
        for (SysMenuVo nav : childList) {
            nav.setChildren(getSysMenuChildByPid(nav.getMenuId(), allMenu));
        }
        childList.sort(orderBySysMenuVo()); //排序
        return childList;
    }

    /**
     * 根据子id，递归获取所有父节点
     * @param childId 子节点id
     * @param allMenu 所有菜单列表
     * @return 每个根节点下，所有子菜单列表
     */
    /*
        示例：
        [
            {
                "menu_parent_id":"-1",
                "menu_id":"1",
                "menu_name":"系统管理",
                "children":[
                        {
                            "menu_parent_id":"1",
                            "menu_id":"12",
                            "menu_name":"系统设置",
                            "children":[
                                {
                                    "menu_parent_id":"12",
                                    "menu_id":"121",
                                    "menu_name":"系统安全",
                                },
                            ],
                        },
                ],
            },
        ]
     */
    public static SysMenuVo getSysMenuParentByChildId(String childId, List<SysMenuVo> allMenu){
        return getSysMenuParentByChildId(null,childId,allMenu);
    }
    private static SysMenuVo getSysMenuParentByChildId(SysMenuVo parent,String childId, List<SysMenuVo> allMenu){
        //父菜单
        SysMenuVo newParent = null;

        for (int i = 0; i < allMenu.size(); i++) {
            SysMenuVo nav = allMenu.get(i);

            // 相等说明：找出当前菜单
            if (nav.getMenuId().equals(childId)){
                newParent = nav;

                //设置子节点
                if(parent != null){
                    ArrayList<SysMenuVo> childList = new ArrayList<>(1);
                    childList.add(parent);
                    newParent.setChildren(childList);
                }

                //删除，减少下次循环次数
                allMenu.remove(i);
                i--;
                break;
            }
        }

        //父节点为空，则说明为顶层菜单
        String menuParentId = newParent.getMenuParentId();
        if("".equals(menuParentId)){
            return newParent;
        }

        //父节点递归
        newParent = getSysMenuParentByChildId(newParent, menuParentId,allMenu);

        return newParent;
    }

    /**
     * 树形菜单转普通list
     */
    public static List<SysMenuVo> treeToList(List<SysMenuVo> treeList){
        //子菜单
        List<SysMenuVo> List = new ArrayList<>(10);

        for (SysMenuVo sysMenuVo : treeList) {

            List<SysMenuVo> children = sysMenuVo.getChildren();
            sysMenuVo.setChildren(null);
            List.add(sysMenuVo);
            if(children != null && children.size() > 0){
                List.addAll(treeToList(children));
            }
        }

        return List;
    }
    public static List<SysMenuVo> treeToList(SysMenuVo treeSysMenuVo){
        //子菜单
        List<SysMenuVo> List = new ArrayList<>(10);


        List<SysMenuVo> children = treeSysMenuVo.getChildren();
        treeSysMenuVo.setChildren(null);
        List.add(treeSysMenuVo);
        if(children != null && children.size() > 0){
            List.addAll(treeToList(children));
        }

        return List;
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

    /* 以下是个人个性化菜单 */

    /**
     * 根据父id，递归获取所有子节点
     * @param pId 父节点id
     * @param allMenu 所有菜单列表
     * @return 每个根节点下，所有子菜单列表
     */
    public static List<SysShortcutMenuVo> getSysShortcutMenuChildByPid(String pId, List<SysShortcutMenuVo> allMenu){
        //子节点
        List<SysShortcutMenuVo> childList = new ArrayList<>(allMenu.size());
        for (int i = 0; i < allMenu.size(); i++) {
            SysShortcutMenuVo nav = allMenu.get(i);

            // 遍历所有节点，将所有菜单的父id与传过来的根节点的id比较
            //相等说明：为该根节点的子节点。
            if (nav.getShortcutMenuParentId().equals(pId)){
                childList.add(nav);

                //删除，减少下次循环次数
                allMenu.remove(i);
                i--;
            }
        }
        //递归
        for (SysShortcutMenuVo nav : childList) {
            nav.setChildren(getSysShortcutMenuChildByPid(nav.getShortcutMenuId(), allMenu));
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

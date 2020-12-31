package cn.huanzi.qch.baseadmin.sys.sysmenu.service;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.common.service.CommonServiceImpl;
import cn.huanzi.qch.baseadmin.sys.sysmenu.pojo.SysMenu;
import cn.huanzi.qch.baseadmin.sys.sysmenu.repository.SysMenuRepository;
import cn.huanzi.qch.baseadmin.sys.sysmenu.vo.SysMenuVo;
import cn.huanzi.qch.baseadmin.sys.sysusermenu.service.SysUserMenuService;
import cn.huanzi.qch.baseadmin.sys.sysusermenu.vo.SysUserMenuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class SysMenuServiceImpl extends CommonServiceImpl<SysMenuVo, SysMenu, String> implements SysMenuService{

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private SysMenuRepository sysMenuRepository;

    @Autowired
    private SysUserMenuService sysUserMenuService;

    @Override
    public Result<String> delete(String id) {
        //先删除子节点
        SysMenuVo sysMenuVo = new SysMenuVo();
        sysMenuVo.setMenuParentId(id);
        super.list(sysMenuVo).getData().forEach((menuVo)->{
            super.delete(menuVo.getMenuId());
        });

        //后删除所有用户菜单表关联信息
        SysUserMenuVo sysUserMenuVo = new SysUserMenuVo();
        sysUserMenuVo.setMenuId(id);
        sysUserMenuService.list(sysUserMenuVo).getData().forEach((vo)->{
            sysUserMenuService.delete(vo.getUserMenuId());
        });

        //再删除自己
        return super.delete(id);
    }

    @Override
    public Result<List<SysMenuVo>> listByTier(SysMenuVo entityVo) {
        List<SysMenuVo> menuVoList = new ArrayList<>();
        List<SysMenuVo> sysMenuVoList = super.list(entityVo).getData();
        sysMenuVoList.forEach((sysMenuVo) -> {
            if(StringUtils.isEmpty(sysMenuVo.getMenuParentId())){
                //上级节点
                menuVoList.add(sysMenuVo);
            }
        });
        sysMenuVoList.forEach((sysMenuVo) -> {
            if(!StringUtils.isEmpty(sysMenuVo.getMenuParentId())){
                //子节点
                menuVoList.forEach((sysMenuVoP) -> {
                    if(sysMenuVoP.getMenuId().equals(sysMenuVo.getMenuParentId())){
                        sysMenuVoP.getChildren().add(sysMenuVo);
                    }
                });
            }
        });

        //排序
        menuVoList.sort(order());
        menuVoList.forEach((sysMenuVoP) -> {
            sysMenuVoP.getChildren().sort(order());
        });

        return Result.of(menuVoList);
    }

    /**
     * 排序,根据sortWeight排序
     */
    private Comparator<SysMenuVo> order(){
        return (o1, o2) -> {
            if (!o1.getSortWeight().equals(o2.getSortWeight())){
                return o1.getSortWeight() - o2.getSortWeight();
            }
            return 0 ;
        };
    }

    /**
     * 获取子节点
     * @param id 父节点id
     * @param allMenu 所有菜单列表
     * @return 每个根节点下，所有子菜单列表
     */
    private List<SysMenuVo> getChild(String id,List<SysMenuVo> allMenu){
        //子菜单
        List<SysMenuVo> childList = new ArrayList<SysMenuVo>();
        for (SysMenuVo nav : allMenu) {
            // 遍历所有节点，将所有菜单的父id与传过来的根节点的id比较
            //相等说明：为该根节点的子节点。
            if (nav.getMenuParentId().equals(id)){
                childList.add(nav);
            }
        }
        //递归
        for (SysMenuVo nav : childList) {
            nav.setChildren(getChild(nav.getMenuId(), allMenu));
        }
        childList.sort(order()); //排序
        //如果节点下没有子节点，返回一个空List（递归退出）
        if (childList.size() == 0 ){
            return new ArrayList<>();
        }
        return childList;
    }
}

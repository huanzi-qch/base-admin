package cn.huanzi.qch.baseadmin.sys.sysmenu.service;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.common.service.CommonServiceImpl;
import cn.huanzi.qch.baseadmin.sys.sysmenu.pojo.SysMenu;
import cn.huanzi.qch.baseadmin.sys.sysmenu.repository.SysMenuRepository;
import cn.huanzi.qch.baseadmin.sys.sysmenu.vo.SysMenuVo;
import cn.huanzi.qch.baseadmin.sys.sysusermenu.service.SysUserMenuService;
import cn.huanzi.qch.baseadmin.sys.sysusermenu.vo.SysUserMenuVo;
import cn.huanzi.qch.baseadmin.util.MenuUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
            delete(menuVo.getMenuId());
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
        List<SysMenuVo> sysMenuVoList = super.list(entityVo).getData();
        return Result.of(MenuUtil.getChildBySysMenuVo("",sysMenuVoList));
    }
}

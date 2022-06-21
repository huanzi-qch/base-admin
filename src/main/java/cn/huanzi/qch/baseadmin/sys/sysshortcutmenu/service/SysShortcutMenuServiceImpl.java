package cn.huanzi.qch.baseadmin.sys.sysshortcutmenu.service;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.common.service.CommonServiceImpl;
import cn.huanzi.qch.baseadmin.sys.sysshortcutmenu.pojo.SysShortcutMenu;
import cn.huanzi.qch.baseadmin.sys.sysshortcutmenu.repository.SysShortcutMenuRepository;
import cn.huanzi.qch.baseadmin.sys.sysshortcutmenu.vo.SysShortcutMenuVo;
import cn.huanzi.qch.baseadmin.util.CopyUtil;
import cn.huanzi.qch.baseadmin.util.MenuUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class SysShortcutMenuServiceImpl extends CommonServiceImpl<SysShortcutMenuVo, SysShortcutMenu, String> implements SysShortcutMenuService{

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private SysShortcutMenuRepository sysShortcutMenuRepository;

    @Override
    public Result<String> delete(String id) {
        //先删除子节点
        SysShortcutMenuVo sysShortcutMenuVo = new SysShortcutMenuVo();
        sysShortcutMenuVo.setShortcutMenuParentId(id);
        super.list(sysShortcutMenuVo).getData().forEach((menuVo)->{
            delete(menuVo.getShortcutMenuId());
        });

        //再删除自己
        return super.delete(id);
    }

    @Override
    public Result<List<SysShortcutMenuVo>> findByUserId(String userId) {
        List<SysShortcutMenuVo> sysShortcutMenuVoList = CopyUtil.copyList(sysShortcutMenuRepository.findByUserId(userId), SysShortcutMenuVo.class);
        return Result.of(MenuUtil.getChildBySysShortcutMenuVo("",sysShortcutMenuVoList));
    }
}

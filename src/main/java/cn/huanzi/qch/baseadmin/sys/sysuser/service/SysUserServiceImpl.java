package cn.huanzi.qch.baseadmin.sys.sysuser.service;

import cn.huanzi.qch.baseadmin.common.pojo.PageInfo;
import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.common.service.CommonServiceImpl;
import cn.huanzi.qch.baseadmin.sys.sysshortcutmenu.repository.SysShortcutMenuRepository;
import cn.huanzi.qch.baseadmin.sys.sysshortcutmenu.service.SysShortcutMenuService;
import cn.huanzi.qch.baseadmin.sys.sysshortcutmenu.vo.SysShortcutMenuVo;
import cn.huanzi.qch.baseadmin.sys.sysuser.pojo.SysUser;
import cn.huanzi.qch.baseadmin.sys.sysuser.repository.SysUserRepository;
import cn.huanzi.qch.baseadmin.sys.sysuser.vo.SysUserVo;
import cn.huanzi.qch.baseadmin.sys.sysuserauthority.service.SysUserAuthorityService;
import cn.huanzi.qch.baseadmin.sys.sysusermenu.service.SysUserMenuService;
import cn.huanzi.qch.baseadmin.sys.sysusermenu.vo.SysUserMenuVo;
import cn.huanzi.qch.baseadmin.util.CopyUtil;
import cn.huanzi.qch.baseadmin.util.MD5Util;
import cn.huanzi.qch.baseadmin.util.SqlUtil;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Service
@Transactional
public class SysUserServiceImpl extends CommonServiceImpl<SysUserVo, SysUser, String> implements SysUserService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private SysShortcutMenuRepository sysShortcutMenuRepository;

    @Autowired
    private SysUserAuthorityService sysUserAuthorityService;

    @Autowired
    private SysUserMenuService sysUserMenuService;

    @Autowired
    private SysShortcutMenuService sysShortcutMenuService;

    @Override
    public Result<String> delete(String id) {
        //删除权限关联表、菜单关联表、个性菜单关联表
        sysUserAuthorityService.findByUserId(id).getData().forEach((vo -> {
            sysUserAuthorityService.delete(vo.getUserAuthorityId());
        }));
        SysUserMenuVo sysUserMenuVo = new SysUserMenuVo();
        sysUserMenuVo.setUserId(id);
        sysUserMenuService.list(sysUserMenuVo).getData().forEach((vo -> {
            sysUserMenuService.delete(vo.getUserMenuId());
        }));
        SysShortcutMenuVo sysShortcutMenuVo = new SysShortcutMenuVo();
        sysShortcutMenuVo.setUserId(id);
        sysShortcutMenuService.list(sysShortcutMenuVo).getData().forEach((vo -> {
            //直接调用Repository删除记录，Service有自己的删除逻辑，不适合这里
            sysShortcutMenuRepository.deleteById(vo.getShortcutMenuId());
        }));

        return super.delete(id);
    }

    @Override
    public Result<PageInfo<SysUserVo>> page(SysUserVo entityVo) {
        //这里可以直接调父类的page方法，当然也可以像下面这样自定义SQL
        //Result<PageInfo<SysUserVo>> result = super.page(entityVo);

        //根据实体、Vo直接拼接全部SQL
        StringBuilder sql = SqlUtil.joinSqlByEntityAndVo(SysUser.class,entityVo);

        //设置SQL、映射实体，以及设置值，返回一个Query对象
        Query query = em.createNativeQuery(sql.toString(), SysUser.class);

        //分页设置，page从0开始
        PageRequest pageRequest = entityVo.getPageable();

        //获取最终分页结果
        Result<PageInfo<SysUserVo>> result = Result.of(PageInfo.of(PageInfo.getJpaPage(query, pageRequest,em), SysUserVo.class));

        //置空密码
        result.getData().getRows().forEach((sysUserVo) -> sysUserVo.setPassword(null));
        return result;
    }


    @Override
    public Result<SysUserVo> save(SysUserVo entityVo) {
        //新增用户
        if (StringUtils.isEmpty(entityVo.getUserId())) {
            //进行登录名唯一校验
            SysUserVo sysUserVo = new SysUserVo();
            sysUserVo.setLoginName(entityVo.getLoginName());
            if(super.list(sysUserVo).getData().size() > 0){
                return Result.of(entityVo,false,"保存失败，登录名已存在！");
            }

            //需要设置初始密码
            entityVo.setPassword(MD5Util.getMd5(SysSettingUtil.getSysSetting().getUserInitPassword()));
        }
        return super.save(entityVo);
    }

    /**
     * 重置初始密码
     */
    @Override
    public Result<SysUserVo> resetPassword(String userId) {
        SysUserVo entityVo = new SysUserVo();
        entityVo.setUserId(userId);
        entityVo.setPassword(MD5Util.getMd5(SysSettingUtil.getSysSetting().getUserInitPassword()));
        Result<SysUserVo> result = super.save(entityVo);
        result.getData().setPassword(null);
        return result;
    }

    @Override
    public Result<SysUserVo> findByLoginName(String username) {
        return Result.of(CopyUtil.copy(sysUserRepository.findByLoginName(username), SysUserVo.class));
    }
}

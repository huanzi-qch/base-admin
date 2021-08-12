package cn.huanzi.qch.baseadmin.sys.syssetting.service;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.common.service.CommonServiceImpl;
import cn.huanzi.qch.baseadmin.sys.syssetting.pojo.SysSetting;
import cn.huanzi.qch.baseadmin.sys.syssetting.repository.SysSettingRepository;
import cn.huanzi.qch.baseadmin.sys.syssetting.vo.SysSettingVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
@Transactional
public class SysSettingServiceImpl extends CommonServiceImpl<SysSettingVo, SysSetting, String> implements SysSettingService{

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private SysSettingRepository sysSettingRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Result<SysSettingVo> save(SysSettingVo entityVo) {
        //调用父类
        Result<SysSettingVo> result = super.save(entityVo);

        //发布 系统设置，更新/保存事件
        applicationEventPublisher.publishEvent(result.getData());
        return result;
    }
}

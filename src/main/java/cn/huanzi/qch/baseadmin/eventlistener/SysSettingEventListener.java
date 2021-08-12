package cn.huanzi.qch.baseadmin.eventlistener;

import cn.huanzi.qch.baseadmin.limiter.RateLimiter;
import cn.huanzi.qch.baseadmin.sys.syssetting.vo.SysSettingVo;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * SysSetting系统设置
 * 事件监听，用于业务解耦
 */
@Slf4j
@Component
public class SysSettingEventListener {

    @Autowired
    private RateLimiter rateLimiter;

    /**
     * 系统设置，更新/保存 事件监听
     * 更新sysSettingMap、OpenAPI限流
     */
    @EventListener
    @Order(1)
    public void sysSettingSaveRegisterListener(SysSettingVo sysSettingVo){
        //更新系统设置时同步更新公用静态集合sysSettingMap
        SysSettingUtil.setSysSettingMap(sysSettingVo);

        //判断OpenAPI限流开启或关闭
        if(!rateLimiter.getStatus() && "Y".equals(SysSettingUtil.getSysSetting().getSysOpenApiLimiterEncrypt())){
            rateLimiter.star();
        }
        if("N".equals(SysSettingUtil.getSysSetting().getSysOpenApiLimiterEncrypt())){
            rateLimiter.stop();
        }
    }
}

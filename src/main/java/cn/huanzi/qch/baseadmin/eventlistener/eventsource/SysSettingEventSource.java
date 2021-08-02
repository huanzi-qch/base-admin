package cn.huanzi.qch.baseadmin.eventlistener.eventsource;

import cn.huanzi.qch.baseadmin.sys.syssetting.vo.SysSettingVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * SysSetting系统设置
 * 事件源
 */
@Getter
@Setter
public class SysSettingEventSource extends ApplicationEvent {
    private SysSettingVo sysSettingVo;

    public SysSettingEventSource(SysSettingVo sysSettingVo) {
        super(sysSettingVo);
        this.sysSettingVo = sysSettingVo;
    }
}

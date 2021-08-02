package cn.huanzi.qch.baseadmin.eventlistener.eventsource;

import cn.huanzi.qch.baseadmin.sys.sysauthority.vo.SysAuthorityVo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 认证数据源
 * 事件源
 */
@Getter
@Setter
public class SecurityMetadataSourceEventSource extends ApplicationEvent {
    private List<SysAuthorityVo> authorityVoList;

    public SecurityMetadataSourceEventSource(List<SysAuthorityVo> authorityVoList) {
        super(authorityVoList);
        this.authorityVoList = authorityVoList;
    }
}

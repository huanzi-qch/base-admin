package cn.huanzi.qch.baseadmin.eventlistener;

import cn.huanzi.qch.baseadmin.config.security.MyFilterInvocationSecurityMetadataSource;
import cn.huanzi.qch.baseadmin.eventlistener.eventsource.SecurityMetadataSourceEventSource;
import cn.huanzi.qch.baseadmin.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * 认证数据源
 * 事件监听，用于业务解耦
 */
@Slf4j
@Component
public class SecurityMetadataSourceEventListener {

    @Autowired
    private MyFilterInvocationSecurityMetadataSource myFilterInvocationSecurityMetadataSource;

    /**
     * 认证数据源 事件监听
     * 置空URL/权限映射缓存、更新权限集合
     */
    @EventListener
    @Order(1)
    public void securityMetadataSourceRegisterListener(SecurityMetadataSourceEventSource eventSourceEvent) {
        //置空URL/权限映射缓存
        SecurityUtil.urlAuthorityMap.clear();

        //更新权限集合
        myFilterInvocationSecurityMetadataSource.setRequestMap(eventSourceEvent.getAuthorityVoList());
    }
}
package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 注销处理
 */
@Component
public class LogoutHandlerConfig implements LogoutHandler {
    @Autowired
    private SecurityUtil securityUtil;

    @Override
    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) {
        securityUtil.sessionRegistryRemoveUserByRequest(httpServletRequest);

        securityUtil.removeRememberMeCookie(httpServletRequest,httpServletResponse);
    }
}

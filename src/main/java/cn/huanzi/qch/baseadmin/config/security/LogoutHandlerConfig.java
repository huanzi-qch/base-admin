package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        //剔除退出用户
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!StringUtils.isEmpty(principal)){
            securityUtil.sessionRegistryRemoveUser((User)principal);
        }
    }
}

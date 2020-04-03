package cn.huanzi.qch.baseadmin.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 自定义session失效策略
 */
@Component
@Slf4j
public class MyInvalidSessionStrategy implements InvalidSessionStrategy {
    @Autowired
    private SessionRegistry sessionRegistry;

    @Override
    public void onInvalidSessionDetected(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        HttpSession session = httpServletRequest.getSession();
        String sessionId = httpServletRequest.getRequestedSessionId();
        if(!session.isNew()){
            httpServletResponse.sendRedirect("/loginPage");
        }else{
            httpServletResponse.setContentType("text/html;charset=UTF-8");
            httpServletResponse.getWriter().print("session已失效，请刷新页面重新登录！");
        }
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
        if(sessionInformation != null){
            User user = (User) sessionInformation.getPrincipal();
            sessionRegistry.removeSessionInformation(sessionId);
            log.info("剔除过期用户:"+user.getUsername());
        }
        log.info(sessionRegistry.getAllPrincipals().size()+"");
        httpServletResponse.flushBuffer();
    }
}

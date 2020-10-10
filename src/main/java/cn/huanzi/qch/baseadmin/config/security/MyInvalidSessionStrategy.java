package cn.huanzi.qch.baseadmin.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void onInvalidSessionDetected(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        HttpSession session = httpServletRequest.getSession();
        String sessionId = httpServletRequest.getRequestedSessionId();
        if(!session.isNew()){
            //内部重定向
            httpServletResponse.sendRedirect("/loginPage");
        }else{
            //直接输出js脚本跳转
            httpServletResponse.setContentType("text/html;charset=UTF-8");
            httpServletResponse.getWriter().print("<script type='text/javascript'>window.location.href = \"" + contextPath + "/loginPage\"</script>");
        }
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
        if(sessionInformation != null){
            User user = (User) sessionInformation.getPrincipal();
            sessionRegistry.removeSessionInformation(sessionId);
            log.info("剔除过期用户:"+user.getUsername());
        }
        log.info("session失效处理 " + sessionRegistry.getAllPrincipals().size()+"");
        httpServletResponse.flushBuffer();
    }
}

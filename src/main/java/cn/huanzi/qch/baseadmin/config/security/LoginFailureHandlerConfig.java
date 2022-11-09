package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.exceptionhandler.LoginException;
import cn.huanzi.qch.baseadmin.sys.sysuser.service.SysUserService;
import cn.huanzi.qch.baseadmin.sys.sysuser.vo.SysUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录失败处理
 */
@Component
@Slf4j
public class LoginFailureHandlerConfig implements AuthenticationFailureHandler {

    @Autowired
    private PasswordConfig passwordConfig;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) {
        String msg = "账号或密码错误";

        //密码错误次数+1
        String username = httpServletRequest.getParameter("username");

        //查询用户，查无此用户
        SysUserVo sysUserVo = sysUserService.findByLoginName(username).getData();
        if(StringUtils.isEmpty(sysUserVo.getUserId())){
            msg = "账号不存在";
        }else{
            String addPwdFailedCount = passwordConfig.addPwdFailedCount(username);
            if(!"1".equals(addPwdFailedCount)){
                msg = addPwdFailedCount;
            }
        }

        //Filter抛出的异常无法被我们的全局异常捕获，需要转移异常才能交由全局异常处理
        handlerExceptionResolver.resolveException(httpServletRequest,httpServletResponse,null,new LoginException(msg));
    }
}

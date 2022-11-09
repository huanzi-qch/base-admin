package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.exceptionhandler.LoginException;
import cn.huanzi.qch.baseadmin.sys.sysuser.service.SysUserService;
import cn.huanzi.qch.baseadmin.sys.sysuser.vo.SysUserVo;
import cn.huanzi.qch.baseadmin.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * 登录成功处理，登陆成功后还需要验证账号的有效性
 */
@Component
@Slf4j
public class LoginSuccessHandlerConfig implements AuthenticationSuccessHandler {
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private PasswordConfig passwordConfig;

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {

        //查询当前与系统交互的用户，存储在本地线程安全上下文，校验账号有效性
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Map<String, Object> map = securityUtil.checkUserByUserData(httpServletRequest,user.getUsername());
        Result result = Result.of(null,true,map.get("msg").toString());

        //校验通过
        if(!Boolean.parseBoolean(map.get("flag").toString())){
            //注册session
            securityUtil.sessionRegistryAddUser(httpServletRequest.getRequestedSessionId(),user);

            if(Boolean.parseBoolean(httpServletRequest.getParameter(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY))){
                //创建remember-me相关数据
                securityUtil.addRememberMe(httpServletRequest,httpServletResponse,user.getUsername());
            }

            //最后登录时间
            SysUserVo sysUserVo = sysUserService.findByLoginName(user.getUsername()).getData();
            sysUserVo.setLastLoginTime(new Date());
            sysUserService.save(sysUserVo);

            //密码安全策略，登录成功后清除错误次数
            passwordConfig.removeMapDataByUser(user.getUsername());
        }else{
            //Filter抛出的异常无法被我们的全局异常捕获，需要转移异常才能交由全局异常处理
            handlerExceptionResolver.resolveException(httpServletRequest,httpServletResponse,null,new LoginException(String.valueOf(result.getMsg())));
            return;
        }


        //判断api加密开关是否开启
        if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())) {
            //api加密
            result = ApiSecurityUtil.encrypt(result);
        }

        //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
        HttpServletResponseUtil.printJson(httpServletResponse,JsonUtil.stringify(result));
    }
}

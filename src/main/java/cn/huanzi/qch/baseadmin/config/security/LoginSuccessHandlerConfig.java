package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
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

import javax.servlet.ServletException;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        //查询当前与系统交互的用户，存储在本地线程安全上下文，校验账号有效性
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Map<String, Object> map = securityUtil.checkUserByUserData(httpServletRequest,user.getUsername());
        String msg = map.get("msg").toString();

        //校验通过
        if(!Boolean.valueOf(map.get("flag").toString())){
            //注册session
            securityUtil.sessionRegistryAddUser(httpServletRequest.getRequestedSessionId(),user);

            if(Boolean.valueOf(httpServletRequest.getParameter(AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY))){
                //创建remember-me相关数据
                securityUtil.addRememberMe(httpServletRequest,httpServletResponse,user.getUsername());
            }

            //最后登录时间
            SysUserVo sysUserVo = sysUserService.findByLoginName(user.getUsername()).getData();
            sysUserVo.setLastLoginTime(new Date());
            sysUserService.save(sysUserVo);
        }

        //判断api加密开关是否开启
        if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())) {
            //api加密
            Result encrypt = ApiSecurityUtil.encrypt(msg);

            msg = JsonUtil.stringify(encrypt);
        }

        //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
        HttpServletResponseUtil.printJson(httpServletResponse,msg);
    }
}

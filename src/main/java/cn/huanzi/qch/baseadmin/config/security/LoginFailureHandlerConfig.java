package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录失败处理
 */
@Component
@Slf4j
public class LoginFailureHandlerConfig implements AuthenticationFailureHandler {

    @Autowired
    private PasswordConfig passwordConfig;

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        String msg = "{\"code\":\"400\",\"msg\":\"账号或密码错误\"}";

        //密码错误次数+1
        String username = httpServletRequest.getParameter("username");
        String addPwdFailedCount = passwordConfig.addPwdFailedCount(username);
        if(!"1".equals(addPwdFailedCount)){
            msg = "{\"code\":\"400\",\"msg\":\""+addPwdFailedCount+"\"}";
        }

        //判断api加密开关是否开启
        if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
            //加密
            try {
                //api加密
                Result encrypt = ApiSecurityUtil.encrypt(msg);

                msg = JsonUtil.stringify(encrypt);
            } catch (Throwable ee) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(ee));
            }
        }

        //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
        HttpServletResponseUtil.printJson(httpServletResponse,msg);
    }
}

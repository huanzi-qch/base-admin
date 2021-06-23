package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.sys.sysuser.service.SysUserService;
import cn.huanzi.qch.baseadmin.sys.sysuser.vo.SysUserVo;
import cn.huanzi.qch.baseadmin.util.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
        SysUserVo sysUserVo = sysUserService.findByLoginName(user.getUsername()).getData();

        //默认登陆成功
        String msg = "{\"code\":\"300\",\"msg\":\"登录成功\",\"url\":\"/index\"}";
        boolean flag = false;

        //登陆IP不在白名单
        String ipAddr = IpUtil.getIpAddr(httpServletRequest);
        String limitedIp = sysUserVo.getLimitedIp();
        if(!StringUtils.isEmpty(limitedIp) && !Arrays.asList(limitedIp.split(",")).contains(ipAddr)){
            msg = "{\"code\":\"400\",\"msg\":\"登陆IP不在白名单，请联系管理员\"}";
            flag = true;
        }

        //禁止多人在线
        if("N".equals(sysUserVo.getLimitMultiLogin()) &&  securityUtil.sessionRegistryGetUserBySessionId(httpServletRequest.getRequestedSessionId()) != null){
            msg = "{\"code\":\"400\",\"msg\":\"该账号禁止多人在线，请联系管理员\"}";
            flag = true;
        }

        //超出有效时间
        if(!StringUtils.isEmpty(sysUserVo.getExpiredTime()) && new Date().getTime() > sysUserVo.getExpiredTime().getTime()){
            msg = "{\"code\":\"400\",\"msg\":\"该账号已失效，请联系管理员\"}";
            flag = true;
        }

        //禁止登陆系统
        if("N".equals(sysUserVo.getValid())){
            msg = "{\"code\":\"400\",\"msg\":\"该账号已被禁止登陆系统，请联系管理员\"}";
            flag = true;
        }

        //校验不通过
        if(flag){
            //清除当前的上下文
            SecurityContextHolder.clearContext();

            //清除remember-me持久化tokens
            securityUtil.rememberMeRemoveUserTokens(user.getUsername());
        }
        else{
            //校验通过，注册session
            securityUtil.sessionRegistryAddUser(httpServletRequest.getSession().getId(),user);
        }

        //判断api加密开关是否开启
        if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())) {
            //加密
            try {
                //api加密
                Result encrypt = ApiSecurityUtil.encrypt(msg);

                msg = JsonUtil.stringify(encrypt);
            } catch (Throwable e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }

        //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        PrintWriter out = httpServletResponse.getWriter();
        out.print(msg);
        out.flush();
        out.close();
        httpServletResponse.flushBuffer();
    }
}

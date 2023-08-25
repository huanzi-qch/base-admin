package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.common.pojo.ParameterRequestWrapper;
import cn.huanzi.qch.baseadmin.exceptionhandler.LoginException;
import cn.huanzi.qch.baseadmin.sys.sysuser.service.SysUserService;
import cn.huanzi.qch.baseadmin.sys.sysuser.vo.SysUserVo;
import cn.huanzi.qch.baseadmin.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * 校验账号、密码前，先进行验证码处理，需要在这里进行登录解密操作
 */
@Component
@Slf4j
public class CaptchaFilterConfig implements Filter {

    @Value("${captcha.enable}")
    private Boolean captchaEnable;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String requestUri = request.getRequestURI();
        /*
            注：详情可在SessionManagementFilter中进行断点调试查看
            security框架会在session的attribute存储登录信息，先从session.getAttribute(this.springSecurityContextKey)中获取登录用户信息
            ，如果没有，再从本地上下文SecurityContextHolder.getContext().getAuthentication()获取，因此想要强制用户下线得进行如下操作

            另外，虽然重启了服务，sessionRegistry.getAllSessions()为空，但之前的用户session未过期同样能访问系统，也是这个原因
         */
        User user = securityUtil.sessionRegistryGetUserBySessionId(session.getId());
        Cookie rememberMeCookie = SecurityUtil.getRememberMeCookie(request);

        //免登陆url除外
        if(!SecurityUtil.checkUrl(requestUri.replaceFirst(contextPath, "")) && user == null){

            //remember me？
            if(rememberMeCookie == null){
                HttpServletResponseUtil.printHtml(response,"<script type='text/javascript'>window.location.href = '" + contextPath + "/logout'</script>");
                return;
            }

            PersistentRememberMeToken token = securityUtil.rememberMeGetTokenForSeries(rememberMeCookie);

            /*
                不允许自动登录
                查无token令牌
                当前URL需要登录才能访问，但当前账号不满足登录限制（七天免登陆、禁止多人在线等登录限制有冲突）
             */
            boolean flag0 = StringUtils.isEmpty(token);
            boolean flag1 = !SecurityUtil.checkUrl(requestUri.replaceFirst(contextPath, ""));
            boolean flag2 = !flag0 && Boolean.valueOf(securityUtil.checkUserByUserData(request, token.getUsername()).get("flag").toString());
            if(flag0 || (flag1 && flag2)){
                log.info("访问{}，尝试自动登录失败，查无token令牌或当前账号不满足登录限制...",requestUri);
                HttpServletResponseUtil.printHtml(response,"<script type='text/javascript'>window.location.href = '" + contextPath + "/logout'</script>");
                return;
            }

            if(flag1) {
                log.info("访问{}，当前session连接开启了免登陆，已自动登录！token：{},userName：{}，最后登录时间：{}",requestUri,rememberMeCookie.getValue(),token.getUsername(),token.getDate());
                //注册新的session
                securityUtil.sessionRegistryAddUser(session.getId(), userDetailsServiceImpl.loadUserByUsername(token.getUsername()));

                //保存登录信息
                user = securityUtil.sessionRegistryGetUserBySessionId(session.getId());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,user.getPassword(),user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetails(request));

                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authentication);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,securityContext);

                //更新token信息
                securityUtil.updateRememberMeByToken(request,response,token);

                //最后登录时间
                SysUserVo sysUserVo = sysUserService.findByLoginName(user.getUsername()).getData();
                sysUserVo.setLastLoginTime(new Date());
                sysUserService.save(sysUserVo);
            }

        }

        //只拦截登录请求，且开发环境下不拦截
        if ("POST".equals(request.getMethod()) && "/login".equals(requestUri.replaceFirst(contextPath,""))) {
            //判断api加密开关是否开启
            if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
                //api解密
                String decrypt = ApiSecurityUtil.decrypt();

                //new一个自定义RequestWrapper
                HashMap hashMap = JsonUtil.parse(decrypt, HashMap.class);
                ParameterRequestWrapper parameterRequestWrapper = new ParameterRequestWrapper(request);
                for (Object key : hashMap.keySet()) {
                    parameterRequestWrapper.addParameter(String.valueOf(key),  hashMap.get(key));
                }

                request = parameterRequestWrapper;
            }

            //从session中获取生成的验证码
            String verifyCode = session.getAttribute("verifyCode").toString();

            if (captchaEnable && !verifyCode.equalsIgnoreCase(request.getParameter("captcha"))) {
                String msg = "验证码错误";
                //Filter抛出的异常无法被我们的全局异常捕获，需要转移异常才能交由全局异常处理
                handlerExceptionResolver.resolveException(request,response,null,new LoginException(msg));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

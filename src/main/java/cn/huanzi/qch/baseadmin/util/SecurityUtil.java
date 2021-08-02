package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.config.security.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring Security工具类
 */
@Component
@Slf4j
public class SecurityUtil {

    @Autowired
    private SessionRegistry sessionRegistry;

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

    //认证数据源，URL/权限映射缓存
    public static Map<String, HashSet<ConfigAttribute>> urlAuthorityMap = new ConcurrentHashMap<>(150);

    /**
     * 从ThreadLocal获取其自己的SecurityContext，从而获取在Security上下文中缓存的登录用户
     */
    public static User getLoginUser() {
        User user = null;
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication auth = ctx.getAuthentication();
        if (auth.getPrincipal() instanceof UserDetails) {
            user = (User) auth.getPrincipal();
        }
        return user;
    }

    /**
     * 检查URL是否包含在“无需权限访问的URL”中
     */
    public static boolean checkUrl(String requestUri){
        String[] requestUris = requestUri.split("/");
        for (int i = 0; i < SecurityConfig.MATCHERS_PERMITALL_URL.length; i++) {
            if(check(requestUris,SecurityConfig.MATCHERS_PERMITALL_URL[i].split("/"))){
                return true;
            }
        }

        return false;
    }
    private static boolean check(String[] requestUris,String[] urls){
        for (int i1 = 0; i1 < requestUris.length; i1++) {
            //判断长度
            if (i1 >= urls.length){
                return false;
            }

            //处理/*、/**情况
            if("*".equals(urls[i1]) || "**".equals(urls[i1])){
                continue;
            }

            //处理带后缀
            if(requestUris[i1].contains(".") && urls[i1].contains(".")){
                String[] split = requestUris[i1].split("\\.");
                String[] split2 = urls[i1].split("\\.");

                // *.后缀的情况
                if("*".equals(split2[0]) && split[1].equals(split2[1])){
                    return true;
                }
            }

            //不相等
            if(!requestUris[i1].equals(urls[i1])){
                return false;
            }

        }

        return true;
    }



    /*    remember-me相关操作     */




    /**
     * 清除remember-me持久化tokens
     */
    public void rememberMeRemoveUserTokens(String userName){
        persistentTokenRepository.removeUserTokens(userName);
    }

    /**
     * 根据rememberMeCookie查询获取数据表中的信息
     */
    public PersistentRememberMeToken rememberMeGetTokenForSeries(Cookie rememberMeCookie){
        return rememberMeCookie == null ? null : persistentTokenRepository.getTokenForSeries(SecurityUtil.decodeCookie(rememberMeCookie.getValue())[0]);
    }

    /**
     * 解密rememberMeCookie
     * 详情可在 PersistentTokenBasedRememberMeServices.processAutoLoginCookie断点，查看调用栈
     */
    public static String[] decodeCookie(String cookieValue) throws InvalidCookieException {
        StringBuilder cookieValueBuilder = new StringBuilder(cookieValue);
        for(int j = 0; j < cookieValueBuilder.length() % 4; ++j) {
            cookieValueBuilder.append("=");
        }
        cookieValue = cookieValueBuilder.toString();

        try {
            Base64.getDecoder().decode(cookieValue.getBytes());
        } catch (IllegalArgumentException var7) {
            throw new InvalidCookieException("Cookie token was not Base64 encoded; value was '" + cookieValue + "'");
        }

        String cookieAsPlainText = new String(Base64.getDecoder().decode(cookieValue.getBytes()));
        String[] tokens = StringUtils.delimitedListToStringArray(cookieAsPlainText, ":");

        for(int i = 0; i < tokens.length; ++i) {
            try {
                tokens[i] = URLDecoder.decode(tokens[i], StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException var6) {
                log.error(var6.getMessage(), var6);
            }
        }

        return tokens;
    }

    /**
     * 获取 "remember-me" cookie
     */
    public static Cookie getRememberMeCookie(HttpServletRequest request){
        for (Cookie cookie : request.getCookies()) {
            if ("remember-me".equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }





    /*  SessionRegistry相关操作  */




    /**
     * 根据user从sessionRegistry获取SessionInformation
     */
    public List<SessionInformation> sessionRegistryGetSessionInformationList(User user){
        return sessionRegistry.getAllSessions(user, true);
    }

    /**
     * 根据sessionId从sessionRegistry获取用户
     */
    public User sessionRegistryGetUserBySessionId(String sessionId){
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);
        if(sessionInformation != null){
            return (User) sessionInformation.getPrincipal();
        }
        return null;
    }

    /**
     * 从sessionRegistry中删除user
     */
    public void sessionRegistryRemoveUser(User user){
        List<SessionInformation> allSessions = this.sessionRegistryGetSessionInformationList(user);
        if (allSessions != null) {
            for (SessionInformation sessionInformation : allSessions) {
                sessionInformation.expireNow();
                sessionRegistry.removeSessionInformation(sessionInformation.getSessionId());
            }

            //清除当前的上下文
            SecurityContextHolder.clearContext();

            //清除remember-me持久化tokens
            this.rememberMeRemoveUserTokens(user.getUsername());
        }
    }

    /**
     * 指定loginName从sessionRegistry中删除user
     */
    public void sessionRegistryRemoveUserByLoginName(String loginName){
        //清除remember-me持久化tokens
        this.rememberMeRemoveUserTokens(loginName);

        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        for (Object allPrincipal : allPrincipals) {
            User user = (User) allPrincipal;
            if(user.getUsername().equals(loginName)){
                List<SessionInformation> allSessions = sessionRegistry.getAllSessions(user, true);
                if (allSessions != null) {
                    for (SessionInformation sessionInformation : allSessions) {
                        sessionInformation.expireNow();
                        sessionRegistry.removeSessionInformation(sessionInformation.getSessionId());
                    }
                }
                break;
            }
        }
    }

    /**
     * 向sessionRegistry注册user
     */
    public void sessionRegistryAddUser(String sessionId, Object user){
        sessionRegistry.registerNewSession(sessionId,user);
    }

    public List<Object> sessionRegistryGetAllPrincipals(){
        return sessionRegistry.getAllPrincipals();
    }
}
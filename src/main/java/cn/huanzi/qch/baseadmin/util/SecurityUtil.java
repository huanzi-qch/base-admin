package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.config.security.SecurityConfig;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.rememberme.InvalidCookieException;
import org.springframework.util.StringUtils;
import sun.rmi.runtime.Log;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Spring Security工具类
 */
@Slf4j
public class SecurityUtil {
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
    public static boolean checkUrl(String requestURI){
        String[] requestURIs = requestURI.split("/");
        for (int i = 0; i < SecurityConfig.MATCHERS_PERMITALL_URL.length; i++) {
            if(check(requestURIs,SecurityConfig.MATCHERS_PERMITALL_URL[i].split("/"))){
                return true;
            }
        }

        return false;
    }
    private static boolean check(String[] requestURIs,String[] urls){
        for (int i1 = 0; i1 < requestURIs.length; i1++) {
            //判断长度
            if (i1 >= urls.length){
                return false;
            }

            //处理/*、/**情况
            if("*".equals(urls[i1]) || "**".equals(urls[i1])){
                continue;
            }

            //处理带后缀
            if(requestURIs[i1].contains(".") && urls[i1].contains(".")){
                String[] split = requestURIs[i1].split("\\.");
                String[] split2 = urls[i1].split("\\.");

                // *.后缀的情况
                if("*".equals(split2[0]) && split[1].equals(split2[1])){
                    return true;
                }
            }

            //不相等
            if(!requestURIs[i1].equals(urls[i1])){
                return false;
            }

        }

        return true;
    }

    /**
     * 解密rememberMeCookie
     * 详情可在 PersistentTokenBasedRememberMeServices.processAutoLoginCookie断点，查看调用栈
     */
    public static String[] decodeCookie(String cookieValue) throws InvalidCookieException {
        for(int j = 0; j < cookieValue.length() % 4; ++j) {
            cookieValue = cookieValue + "=";
        }

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
     * 根据name获取cookie
     */
    public static Cookie getCookieByName(HttpServletRequest request,String name){
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }
}

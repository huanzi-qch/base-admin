package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.config.security.MyPersistentTokenBasedRememberMeServices;
import cn.huanzi.qch.baseadmin.config.security.PasswordConfig;
import cn.huanzi.qch.baseadmin.config.security.SecurityConfig;
import cn.huanzi.qch.baseadmin.sys.sysuser.service.SysUserService;
import cn.huanzi.qch.baseadmin.sys.sysuser.vo.SysUserVo;
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
import org.springframework.security.web.authentication.rememberme.*;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private MyPersistentTokenBasedRememberMeServices myPersistentTokenBasedRememberMeServices;

    @Autowired
    private SysUserService sysUserService;

    //认证数据源，URL/权限映射缓存
    public static Map<String, HashSet<ConfigAttribute>> urlAuthorityMap = new ConcurrentHashMap<>(150);

    @Autowired
    private PasswordConfig passwordConfig;

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
        //对/进行特殊处理
        if("/".equals(requestUri) && !Arrays.asList(SecurityConfig.MATCHERS_PERMITALL_URL).contains(requestUri)){
            return false;
        }

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
            if("**".equals(urls[i1])){
                return true;
            }
            if("*".equals(urls[i1])){
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

    /**
     * 检查用户登录限制
     */
    public Map<String,Object> checkUserByUserData(HttpServletRequest httpServletRequest,String userName){
        //默认登陆成功
        String msg = "登录成功";
        boolean flag = false;

        //密码安全策略，校验是否已被锁定（密码错误次数达上限）
        String checkBanTime = passwordConfig.checkBanTimeByUser(userName);
        if(!"1".equals(checkBanTime)){
            msg = checkBanTime;
            flag = true;
        }

        //如果flag标识已经为true，可以跳过一些无用步骤以节省性能开支

        SysUserVo sysUserVo = null;
        if(!flag){
            sysUserVo = sysUserService.findByLoginName(userName).getData();
        }

        //禁止登陆系统
        if(!flag && "N".equals(sysUserVo.getValid())){
            msg = "该账号已被禁止登陆系统，请联系管理员";
            flag = true;

            //清除remember-me持久化token，删除所有
            this.rememberMeRemoveUserTokensByUserName(userName);
        }

        //超出有效时间
        if(!flag && !StringUtils.isEmpty(sysUserVo.getExpiredTime()) && System.currentTimeMillis() > sysUserVo.getExpiredTime().getTime()){
            msg = "该账号已失效，请联系管理员";
            flag = true;

            //清除remember-me持久化token，删除所有
            this.rememberMeRemoveUserTokensByUserName(userName);
        }

        //禁止多人在线
        if(!flag && "N".equals(sysUserVo.getLimitMultiLogin()) &&  sessionRegistryGetUserByUserName(userName) != null){
            //这里选择合适自己的方案

            //方案一：禁止新用户登陆
            msg = "该账号禁止多人在线，请联系管理员";
            flag = true;

            //方案二：新用户顶掉旧用户
            //this.sessionRegistryRemoveUserByUserName(userName);
        }

        //登陆IP不在白名单
        if(!flag && !StringUtils.isEmpty(sysUserVo.getLimitedIp()) && !Arrays.asList(sysUserVo.getLimitedIp().split(",")).contains(IpUtil.getIpAddr(httpServletRequest))){
            msg = "登陆IP不在白名单，请联系管理员";
            flag = true;
        }

        //校验不通过
        if(flag){
            //清除保存的登录信息
            SecurityContextHolder.clearContext();
            httpServletRequest.getSession().removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        }

        HashMap<String,Object> hashMap = new HashMap<>(2);
        hashMap.put("flag",flag);
        hashMap.put("msg",msg);
        return hashMap;
    }



    /*    remember-me相关操作     */
    /**
     * 清除remember-me持久化tokens
     * PS：清除用户所有的token记录
     */
    public void rememberMeRemoveUserTokensByUserName(String userName){
        persistentTokenRepository.removeUserTokens(userName);
    }

    /**
     * 根据series删除token记录
     */
    public void rememberMeRemoveUserTokensBySeries(String series){
        //强制转换成JdbcTokenRepositoryImpl类，调用自定义sql进行删除
        JdbcTokenRepositoryImpl jdbcTokenRepositoryImpl = (JdbcTokenRepositoryImpl)persistentTokenRepository;
        jdbcTokenRepositoryImpl.getJdbcTemplate().update("delete from persistent_logins where series = ?", new Object[]{series});
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
     * 获取 remember-me cookie
     */
    public static Cookie getRememberMeCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie : cookies) {
                if (AbstractRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 删除 remember-me cookie
     */
    public void removeRememberMeCookie(HttpServletRequest request,HttpServletResponse response){
        //清除remember-me持久化token，删除自己
        Cookie rememberMeCookie = SecurityUtil.getRememberMeCookie(request);
        if(rememberMeCookie != null){
            String series = SecurityUtil.decodeCookie(rememberMeCookie.getValue())[0];
            rememberMeRemoveUserTokensBySeries(series);
        }

        //清除cookie
        myPersistentTokenBasedRememberMeServices.removeCookie(request,response);
    }

    /**
     * 创建remember-me相关数据
     */
    public void addRememberMe(HttpServletRequest request, HttpServletResponse response,String username){
        PersistentRememberMeToken persistentToken = new PersistentRememberMeToken(username, myPersistentTokenBasedRememberMeServices.generateSeriesData(), myPersistentTokenBasedRememberMeServices.generateTokenData(), new Date());
        persistentTokenRepository.createNewToken(persistentToken);
        myPersistentTokenBasedRememberMeServices.addCookie(persistentToken, request, response);
    }

    /**
     * 更新remember-me相关数据
     */
    public void updateRememberMeByToken(HttpServletRequest request, HttpServletResponse response,PersistentRememberMeToken token){
        PersistentRememberMeToken newToken = new PersistentRememberMeToken(token.getUsername(), token.getSeries(), myPersistentTokenBasedRememberMeServices.generateTokenData(), new Date());
        persistentTokenRepository.updateToken(newToken.getSeries(), newToken.getTokenValue(), newToken.getDate());
        myPersistentTokenBasedRememberMeServices.addCookie(newToken, request, response);
    }



    /*  SessionRegistry相关操作  */
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
     * 根据userName从sessionRegistry获取用户
     */
    public User sessionRegistryGetUserByUserName(String userName){
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            User user = (User) principal;
            if(user.getUsername().equals(userName)){
                return user;
            }
        }
        return null;
    }

    /**
     * 根据User从sessionRegistry剔除所有登录用户
     */
    public void sessionRegistryRemoveUserByUser(User user){
        List<SessionInformation> allSessions = sessionRegistry.getAllSessions(user, true);
        if (allSessions != null) {
            for (SessionInformation sessionInformation : allSessions) {
                sessionInformation.expireNow();
                sessionRegistry.removeSessionInformation(sessionInformation.getSessionId());
            }
        }
    }

    /**
     * 根据sessionId从sessionRegistry剔除当前登录用户
     */
    public void sessionRegistryRemoveUserByRequest(HttpServletRequest request){
        sessionRegistryRemoveUserByRequest(request.getRequestedSessionId());

        //清除当前的上下文
        SecurityContextHolder.clearContext();
    }
    public void sessionRegistryRemoveUserByRequest(String sessionId){
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(sessionId);

        if(sessionInformation == null){
            return;
        }
        sessionInformation.expireNow();
        sessionRegistry.removeSessionInformation(sessionId);

    }

    /**
     * 根据userName从sessionRegistry中删除user
     */
    public void sessionRegistryRemoveUserAndRemoveUserTokensByUserName(String userName){
        sessionRegistryRemoveUserByUserName(userName);
        //清除remember-me持久化tokens
        this.rememberMeRemoveUserTokensByUserName(userName);
    }
    public void sessionRegistryRemoveUserByUserName(String userName){
        User user = sessionRegistryGetUserByUserName(userName);
        sessionRegistryRemoveUserByUser(user);
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
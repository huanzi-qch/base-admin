package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.sys.sysauthority.vo.SysAuthorityVo;
import cn.huanzi.qch.baseadmin.util.SecurityUtil;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置认证数据源，实现动态权限加载
 */
@Component
public class MyFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    //权限数据
    private Map<RequestMatcher, Collection<ConfigAttribute>> requestMap = new ConcurrentHashMap<>(5);

    /**
     * 在我们初始化的权限数据中找到对应当前url的权限数据
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        FilterInvocation fi = (FilterInvocation) object;
        HttpServletRequest request = fi.getRequest();
        String requestURI = request.getRequestURI();

        //先从缓存获取
        HashSet<ConfigAttribute> urlAuthorityMapValue = SecurityUtil.urlAuthorityMap.get(requestURI);
        if(!StringUtils.isEmpty(urlAuthorityMapValue)){
            return urlAuthorityMapValue;
        }

        HashSet<ConfigAttribute> hashSet = new HashSet<>(5);

        //遍历我们初始化的权限数据，找到对应的url对应的权限
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap
                .entrySet()) {
            if (entry.getKey().matches(request)) {
                hashSet.addAll(entry.getValue());
            }
        }
        SecurityUtil.urlAuthorityMap.put(requestURI,hashSet);

        return hashSet;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    /**
     * 更新权限集合
     */
    public void setRequestMap(List<SysAuthorityVo> authorityVoList){
        this.requestMap.clear();

        for (SysAuthorityVo sysAuthorityVo : authorityVoList) {
            String authorityName = sysAuthorityVo.getAuthorityName();
            if (StringUtils.isEmpty(sysAuthorityVo.getAuthorityContent())){
                continue;
            }
            for (String url : sysAuthorityVo.getAuthorityContent().split(",")) {
                Collection<ConfigAttribute> value = this.requestMap.get(new AntPathRequestMatcher(url));
                if (StringUtils.isEmpty(value)) {
                    HashSet<ConfigAttribute> configs = new HashSet<>(5);
                    configs.add(new SecurityConfig(authorityName));
                    this.requestMap.put(new AntPathRequestMatcher(url), configs);
                } else {
                    value.add(new SecurityConfig(authorityName));
                }
            }
        }
    }
}

package cn.huanzi.qch.baseadmin.aspect;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.util.ErrorUtil;
import cn.huanzi.qch.baseadmin.limiter.RateLimiter;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * 对外开放的接口，做限流处理
 */
@Slf4j
@Aspect
@DependsOn("rateLimiter")//初始化依赖于
@Component
public class OpenApiAspect {

    @Autowired
    private RateLimiter rateLimiter;

    /**
     * Pointcut 切入点
     * 匹配 cn.huanzi.qch.baseadmin.openapi.controller 包下面的所有方法
     */
    @Pointcut(value = "execution(public * cn.huanzi.qch.baseadmin.openapi.controller.*.*(..))")
    public void openApiAspect() {}

    /**
     * 环绕通知
     */
    @Around(value = "openApiAspect()")
    public Object arround(ProceedingJoinPoint pjp) throws Throwable {
        //判断OpenAPI限流开关是否开启
        if("N".equals(SysSettingUtil.getSysSetting().getSysOpenApiLimiterEncrypt())){
            return pjp.proceed(pjp.getArgs());
        }

        //令牌桶返回true或者false
        if(rateLimiter.execute()){
            return pjp.proceed(pjp.getArgs());
        }else{
            return Result.of(10001,false,"API接口繁忙，请稍后再试！");
        }
    }
}

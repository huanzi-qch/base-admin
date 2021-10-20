##  接口限流<br/> 
　　base-admin为对外开放的open api接口做了限流处理，当然这个功能也可以在系统设置中进行开启、关闭<br/>

##  效果<br/> 
![](https://img2020.cnblogs.com/blog/1353055/202107/1353055-20210715171715550-1384145747.png)<br/>
![](https://img2020.cnblogs.com/blog/1353055/202106/1353055-20210625161917851-660941644.gif)<br/>

##  令牌桶限流<br/> 
　　我们采用令牌桶限流法，并自己实现一个简单令牌桶限流<br/> 
　　使用@Async优雅异步任务线程以恒定速率向令牌桶添加令牌<br/> 
　　一个请求会消耗一个令牌，令牌桶里的令牌大于0，才会放行，反正不允许通过<br/> 
```
/**
 * 简单的令牌桶限流
 * 在SpringCloud分布式下实现限流，需要把令牌桶的维护放到一个公共的地方，
 * 比如Zuul路由，另外，guava里有现成的基于令牌桶的限流实现。
 *
 * https://www.cnblogs.com/huanzi-qch/p/11053061.html
 */
@Slf4j
@DependsOn("asyncTaskExecutor")//初始化依赖于
@Component
public class RateLimiter {

    //桶的大小，即峰值可处理请求数量
    private int limit = 10;

    //每秒新增speed个令牌，即每秒可处理请求数量
    private int speed = 3;

    //桶当前的token数，使用volatile修饰
    private static volatile int tokens = 0;

    //状态标识
    private static boolean asyncTaskFlag = false;

    public RateLimiter(){
        //初始化桶是满的
        RateLimiter.tokens = this.limit;
    }

    /**
     * 根据令牌数判断是否允许执行
     * 运行执行tokens - 1;
     * 存在并发调用情况，需要加锁
     */
    public synchronized boolean execute() {
        if (RateLimiter.tokens > 0) {
            RateLimiter.tokens = RateLimiter.tokens - 1;
            return true;
        }
        return false;
    }

    /**
     * 当前状态
     */
    public boolean getStatus(){
        return RateLimiter.asyncTaskFlag;
    }

    /**
     * 令牌桶限流启动！
     */
    @Async("asyncTaskExecutor")
    public void star() {
        log.info("限流令牌桶任务线程启动！");
        RateLimiter.asyncTaskFlag = true;

        //异步线程循环往桶里添加令牌
        while (asyncTaskFlag){
            try {
                Thread.sleep(1000L);

                int newTokens = RateLimiter.tokens + speed;
                if(newTokens > limit){
                    RateLimiter.tokens = limit;
                }else{
                    RateLimiter.tokens = newTokens;
                }
            } catch (Exception e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }
        log.info("限流令牌桶任务停止！");
    }

    /**
     * 令牌桶限流关闭！
     */
    public void stop(){
        RateLimiter.asyncTaskFlag = false;
    }
}
```
　　简单限流体验！<br/>
![](https://img2018.cnblogs.com/blog/1353055/201906/1353055-20190619172647484-1924740585.gif)<br/>


##  使用<br/> 
　　@Async限流令牌桶任务线程需要手动调用异步才能生效，因此我们在ApplicationRunner中判断OpenAPI限流开关是否开启来进行自动调用<br/> 
```
    /**
     * 启动成功
     */
    @Bean
    public ApplicationRunner applicationRunner() {
        return applicationArguments -> {
            try {
                // 省略其他代码...
                
                //判断OpenAPI限流开关是否开启
                if("Y".equals(SysSettingUtil.getSysSetting().getSysOpenApiLimiterEncrypt())){
                    //令牌桶限流启动！
                    rateLimiter.star();
                }
                
                // 省略其他代码...
            } catch (UnknownHostException e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        };
    }
```
　　在系统设置中进行开启、关闭时进行异步线程的启动/停止，在系统设置进行保存时，ApplicationEventPublisher发布 系统设置，更新/保存事件，触发SysSettingEventListener事件监听<br/> 
```
    /**
     * 系统设置，更新/保存 事件监听
     * 更新sysSettingMap、OpenAPI限流
     */
    @EventListener
    @Order(1)
    public void sysSettingSaveRegisterListener(SysSettingVo sysSettingVo){
        // 省略其他代码...

        //判断OpenAPI限流开启或关闭
        if(!rateLimiter.getStatus() && "Y".equals(SysSettingUtil.getSysSetting().getSysOpenApiLimiterEncrypt())){
            rateLimiter.star();
        }
        if("N".equals(SysSettingUtil.getSysSetting().getSysOpenApiLimiterEncrypt())){
            rateLimiter.stop();
        }
        
        // 省略其他代码...
    }
```
　　在aop进行open api限流处理<br/>
```
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
```
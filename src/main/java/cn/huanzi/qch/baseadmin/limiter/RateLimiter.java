package cn.huanzi.qch.baseadmin.limiter;

import cn.huanzi.qch.baseadmin.util.ErrorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

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
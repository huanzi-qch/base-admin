package cn.huanzi.qch.baseadmin.timer;

import cn.huanzi.qch.baseadmin.config.security.PasswordConfig;
import cn.huanzi.qch.baseadmin.sys.sysuser.service.SysUserService;
import cn.huanzi.qch.baseadmin.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 定时器，定时清理无用数据，以免一直占用内存
 */
@Slf4j
@Component
public class ClearLoginUserScheduler {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private PasswordConfig passwordConfig;

    /**
     * 每天凌晨3点
     */
    @Scheduled(cron="0 0 3 * * ?")
    private void task(){
        /*
            1、定时清除注册表，避免一直存在无效用户
         */
        Date date = new Date();
        int time = 1000 * 60 * 60;//PS：这里的值取session过期时间更合适
        List<Object> allPrincipals = securityUtil.sessionRegistryGetAllPrincipals();
        for (Object allPrincipal : allPrincipals) {
            User user = (User) allPrincipal;
            Date lastLoginTime = sysUserService.findByLoginName(user.getUsername()).getData().getLastLoginTime();

            //当前时间 - 最后登录时间 >= time
            if(date.getTime() - lastLoginTime.getTime() >= time){
                securityUtil.sessionRegistryRemoveUserByUserName(user.getUsername());
                log.info("清理：{}，最后登录时间{}",user.getUsername(),lastLoginTime);
            }
        }

        /*
            2、定时清除密码安全策略集合数据，以免死数据堆积占用内存
         */
        passwordConfig.removeMapDataByAll();
    }
}

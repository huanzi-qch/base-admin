package cn.huanzi.qch.baseadmin.timer;

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
 * 定时清除登录用户
 * 定时器
 */
@Slf4j
@Component
public class ClearLoginUserScheduler {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SecurityUtil securityUtil;

    /**
     * 每天凌晨3点
     */
    @Scheduled(cron="0 0 3 * * ?")
    private void task(){
        Date date = new Date();
        int time = 1000 * 60 * 60;
        List<Object> allPrincipals = securityUtil.sessionRegistryGetAllPrincipals();
        for (Object allPrincipal : allPrincipals) {
            User user = (User) allPrincipal;
            Date lastLoginTime = sysUserService.findByLoginName(user.getUsername()).getData().getLastLoginTime();

            //当前时间 - 最后登录时间 >= 1个小时
            if(date.getTime() - lastLoginTime.getTime() >= time){
                securityUtil.sessionRegistryRemoveUserByUserName(user.getUsername());
                log.info("清理：{}，最后登录时间{}",user.getUsername(),lastLoginTime);
            }
        }
    }
}

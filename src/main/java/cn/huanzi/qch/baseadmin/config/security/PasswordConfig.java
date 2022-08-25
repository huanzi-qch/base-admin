package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.util.MD5Util;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 密码安全策略
 * https://www.cnblogs.com/huanzi-qch/p/16613636.html
 */
@Component
public class PasswordConfig implements PasswordEncoder {
    // 用户连续密码输入错误次数
    private final static ConcurrentHashMap<String,Integer> pwdFailedMap = new ConcurrentHashMap<>(10);
    // 用户连续密码输入错误次数达上限后锁定时长
    private final static ConcurrentHashMap<String, Date> banTimeMap = new ConcurrentHashMap<>(10);
    // 允许连续密码输入错误次数
    private final static Integer maxTryLogin = 3;
    // 连续失败后锁定时长（分钟）
    private final static Integer banMinute = 5;
    // 密码长度限制：[最少,最多]
    private final static Integer[] passwordLength = {6,12};

    /**
     * 加密
     */
    @Override
    public String encode(CharSequence charSequence) {
        return MD5Util.getMd5(charSequence.toString());
    }

    /**
     * 密码匹配
     * charSequence是用户输入的密码，password是存库的密码
     */
    @Override
    public boolean matches(CharSequence charSequence, String password) {
        return password.contentEquals(this.encode(charSequence));
    }

    /**
     * 密码复杂度校验
     * 满足要求返回1
     */
    public String pwdCheck(String password){

        String sysCheckPwdEncrypt = SysSettingUtil.getSysSetting().getSysCheckPwdEncrypt();

        //空校验
        if(password == null || "".equals(password)){
            return "密码不能为空！";
        }

        //是否开启密码安全策略
        if(!"Y".equals(sysCheckPwdEncrypt)){
            return "1";
        }

        //密码长度限制
        if(password.length() < passwordLength[0]){
            return "密码长度不能小于"+passwordLength[0]+"位数！";
        }
        if(password.length() > passwordLength[1]){
            return "密码长度不能大于"+passwordLength[1]+"位数！";
        }

        //数字
        Pattern pat = Pattern.compile("[0-9]+");
        if(!pat.matcher(password).find()){
            return "密码需包含数字！";
        }

        //小写字母
        Pattern pat2 = Pattern.compile("[a-z]+");
        if(!pat2.matcher(password).find()){
            return "密码需包含小写字母！";
        }

        //大写字母
        Pattern pat3 = Pattern.compile("[A-Z]+");
        if(!pat3.matcher(password).find()){
            return "密码需包含大写字母！";
        }

        //特殊字符：~!@#$%^&*()_+/-[]{}\|;':"<>?,.
        Pattern pat4 = Pattern.compile("[~!@#$%^&*()_+/\\-\\[\\]{}\\\\|;':\"<>?,.]+");
        if(!pat4.matcher(password).find()){
            return "密码需包含特殊字符！";
        }


        /*
            也可以直接使用下面这个正则，效果一样，只是错误提示没那么详细
         */
        //数字：(?=.*\d)
        //小写字母：(?=.*[a-z])
        //大写字母：(?=.*[A-Z])
        //特殊字符：(?=.*[~!@#$%^&*()_+/\-\[\]{}\\|;':"<>?,.])
        //长度限制：.{6,12}
        //Pattern pat5 = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[~!@#$%^&*()_+/\\-\\[\\]{}\\\\|;':\"<>?,.]).{"+passwordLength[0]+","+passwordLength[1]+"}$");
        //if(!pat5.matcher(password).find()){
        //    return "密码需包含数字、小写字母、大写字母、特殊字符且长度在"+passwordLength[0]+"-"+passwordLength[1]+"之间！";
        //}

        return "1";
    }

    /**
     * 密码错误一次，并返回可剩余次数提示
     */
    public String addPwdFailedCount(String username) {
        String sysCheckPwdEncrypt = SysSettingUtil.getSysSetting().getSysCheckPwdEncrypt();

        //是否开启密码安全策略
        if(!"Y".equals(sysCheckPwdEncrypt)){
            return "1";
        }

        Integer result = 0;
        if (pwdFailedMap.containsKey(username)) {
            result = pwdFailedMap.get(username);

            //校验锁定时间是否到期
            if (banTimeMap.containsKey(username)) {
                Date banTime = banTimeMap.get(username);
                long diff = banTime.getTime() - new Date().getTime();

                if(diff <= 0){
                    banTimeMap.remove(username);
                    pwdFailedMap.remove(username);
                    result = 0;
                }
            }
        }

        ++result;

        if (result >= maxTryLogin) {
            //当前时间 + banMinute
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MINUTE, banMinute);
            Date banTime = cal.getTime();
            banTimeMap.put(username, banTime);

            return "密码已经输错" + maxTryLogin + "次，" + checkBanTimeByUser(username);
        } else {
            pwdFailedMap.put(username, result);

            return "密码已经输错" + result + "次，输错" + (maxTryLogin - result) + "次后帐号将会被锁定！";
        }
    }

    /**
     * 检查用户是否已经被锁定
     * 未被锁定返回1
     */
    public String checkBanTimeByUser(String username) {
        String sysCheckPwdEncrypt = SysSettingUtil.getSysSetting().getSysCheckPwdEncrypt();

        //是否开启密码安全策略
        if(!"Y".equals(sysCheckPwdEncrypt)){
            return "1";
        }

        if (banTimeMap.containsKey(username)) {
            Date banTime = banTimeMap.get(username);
            long diff = banTime.getTime() - new Date().getTime();

            //校验锁定时间是否到期
            if(diff <= 0){
                banTimeMap.remove(username);
                pwdFailedMap.remove(username);
                return "1";
            }

            //毫秒转分钟 1000 * 60
            long minute = (long)Math.ceil((double)diff / 60000);
            return "帐号已被锁定，请" + minute + "分钟后，再登录系统！";
        }

        return "1";
    }

    /**
     * 清除对应集合内容
     */
    public void removeMapDataByUser(String username){
        String sysCheckPwdEncrypt = SysSettingUtil.getSysSetting().getSysCheckPwdEncrypt();

        //是否开启密码安全策略
        if("Y".equals(sysCheckPwdEncrypt)){
            pwdFailedMap.remove(username);
            banTimeMap.remove(username);
        }
    }

    /**
     * 清除所有集合内容
     */
    public void removeMapDataByAll(){
        pwdFailedMap.clear();
        banTimeMap.clear();
    }

    /**
     * 随机六位数复杂密码
     */
    public String randomPassword(){
        final char[] char0 = "0123456789".toCharArray();
        final char[] char1 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        final char[] char2 = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        final char[] char3 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        final char[] char4 = "~!@#$%^&*()_+/-[]{}\\|;':\"<>?,.".toCharArray();

        //随机六位数密码：两个数字 + 三个大/小写字母 + 一个特殊字符
        Random random = new Random();
        return  String.valueOf(char0[random.nextInt(char0.length - 1)]) +
                char0[random.nextInt(char0.length - 1)] +
                char1[random.nextInt(char1.length - 1)] +
                char2[random.nextInt(char2.length - 1)] +
                char3[random.nextInt(char3.length - 1)] +
                char4[random.nextInt(char4.length - 1)];
    }

}

package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.util.MD5Util;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Random;
import java.util.regex.Pattern;

@Component
public class PasswordConfig implements PasswordEncoder {

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
     * 密码复杂度限制
     *  返回1满足复杂度要求，否则不满足要求
     */
    /*
        强密码(必须包含大小写字母和数字的组合，不能使用特殊字符，长度在 8-10 之间)：^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9]{8,10}$
        强密码(必须包含大小写字母和数字的组合，可以使用特殊字符，长度在8-10之间)：^(?=.*\d)(?=.*[a-z])(?=.*[A-Z]).{8,10}$
     */
    public String password(String password){
        String sysCheckPwdEncrypt = SysSettingUtil.getSysSetting().getSysCheckPwdEncrypt();

        if(StringUtils.isEmpty(password)){
            return "密码不能为空！";
        }

        if(!"Y".equals(sysCheckPwdEncrypt)){
            return "1";
        }

        //密码长度需大于或等于六位数
        if(password.length() < 6){
            return  "密码长度需大于或等于六位数！";
        }

        //数字
        Pattern pat = Pattern.compile("[0-9]+");

        //字母
        Pattern pat1 = Pattern.compile("[a-zA-Z]+");

        //大写字母
        Pattern pat2 = Pattern.compile("[A-Z]+");

        //小写字母
        Pattern pat3 = Pattern.compile("[a-z]+");

        //特殊字符：~!@#$%^&*()_+/-[]{}\|;':"<>?,.
        Pattern pat4 = Pattern.compile("[~!@#$%^&*()_+/\\-\\[\\]{}\\\\|;':\"<>?,.]+");

        //密码需包含数字 + 大写字母 + 小写字母 + 特殊字符！
        if(!pat.matcher(password).find() ||
                !pat1.matcher(password).find() ||
                !pat2.matcher(password).find() ||
                !pat3.matcher(password).find() ||
                !pat4.matcher(password).find()){
            return  "密码需包含数字 + 大写字母 + 小写字母 + 特殊字符！";
        }
        return "1";
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

//    public static void main(String[] args) {
//        System.out.println(password("",0));//密码不能为空！
//        System.out.println(password("sa",0));//1
//
//        System.out.println(password("12345",1));//密码长度需大于或等于六位数！
//        System.out.println(password("12345A",1));//1
//
//        System.out.println(password("12345",2));//密码长度需大于或等于六位数！
//        System.out.println(password("123456",2));//密码需包含数字 + 字母！
//        System.out.println(password("BB23456",2));//1
//
//        System.out.println(password("12345",3));//密码长度需大于或等于六位数！
//        System.out.println(password("BB23456",3));//密码需包含数字 + 大写字母 + 小写字母 + 特殊字符！
//        System.out.println(password("B23dfa",3));//密码需包含数字 + 大写字母 + 小写字母 + 特殊字符！
//        System.out.println(password("BB23^5a",3));//1
//
//        System.out.println("六位数随机密码测试：");
//        for (int i = 0; i < 10; i++) {
//            System.out.println(randomPassword());
//        }
//    }
}

package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.util.MD5Util;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
}

package cn.huanzi.qch.baseadmin.exceptionhandler;

/**
 * 登录异常
 */
public class LoginException extends ServiceException {

    public LoginException(String msg) {
        super("20001",msg);
    }
}

package cn.huanzi.qch.baseadmin.exceptionhandler;

/**
 * 自定义异常枚举类
 */
public enum ErrorEnum {
    //未知异常
    UNKNOWN("10000","未知异常!"),

    //自定义系列
    SAVE_FAILURE("10001","【Common Service】保存失败！"),
    ENCRYPT_FAILURE("10002","【Api Security】加密异常！"),
    DECRYPT_FAILURE("10002","【Api Security】解密异常！"),

    //400系列
    BAD_REQUEST("400","请求的数据格式不符!"),
    UNAUTHORIZED("401","登录凭证过期!"),
    FORBIDDEN("403","抱歉，你无权访问，请联系管理员！"),
    NOT_FOUND("404", "抱歉，你访问的资源不存在!"),

    //500系列
    INTERNAL_SERVER_ERROR("500", "服务器内部错误，请联系管理员！"),
    SERVICE_UNAVAILABLE("503","服务器正忙，请稍后再试!");



    /** 错误码 */
    private String code;

    /** 错误描述 */
    private String msg;

    ErrorEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}

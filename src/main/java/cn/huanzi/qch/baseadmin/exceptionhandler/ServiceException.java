package cn.huanzi.qch.baseadmin.exceptionhandler;

/**
 * 自定义业务异常
 */
public class ServiceException extends RuntimeException {

    /**
     * 自定义异常枚举类
     */
    private ErrorEnum errorEnum;

    /**
     * 错误码
     */
    private String code;

    /**
     * 错误信息
     */
    private String errorMsg;


    public ServiceException() {
        super();
    }

    public ServiceException(ErrorEnum errorEnum) {
        super("{code:" + errorEnum.getCode() + ",errorMsg:" + errorEnum.getMsg() + "}");
        this.errorEnum = errorEnum;
        this.code = errorEnum.getCode();
        this.errorMsg = errorEnum.getMsg();
    }

    public ServiceException(String code,String errorMsg) {
        super("{code:" + code + ",errorMsg:" + errorMsg + "}");
        this.code = code;
        this.errorMsg = errorMsg;
    }

    public ErrorEnum getErrorEnum() {
        return errorEnum;
    }

    public void setErrorEnum(ErrorEnum errorEnum) {
        this.errorEnum = errorEnum;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

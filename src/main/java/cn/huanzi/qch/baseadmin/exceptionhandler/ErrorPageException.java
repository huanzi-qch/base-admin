package cn.huanzi.qch.baseadmin.exceptionhandler;

/**
 * 自定义错误页面异常
 */
public class ErrorPageException extends ServiceException {

    public ErrorPageException(String code,String msg) {
        super(code, msg);
        ErrorEnum errorEnum;
        switch (code) {
            case "400":
                errorEnum = ErrorEnum.BAD_REQUEST;
                break;
            case "401":
                errorEnum = ErrorEnum.UNAUTHORIZED;
                break;
            case "403":
                errorEnum = ErrorEnum.FORBIDDEN;
                break;
            case "404":
                errorEnum = ErrorEnum.NOT_FOUND;
                break;
            case "500":
                errorEnum = ErrorEnum.INTERNAL_SERVER_ERROR;
                break;
            case "503":
                errorEnum = ErrorEnum.SERVICE_UNAVAILABLE;
                break;
            default:
                errorEnum = ErrorEnum.UNKNOWN;
                break;
        }
        this.setErrorEnum(errorEnum);
    }
}

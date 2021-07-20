package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.common.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * API接口 加解密工具类
 * 详情请阅读博客：https://www.cnblogs.com/huanzi-qch/p/10913636.html
 */
@Slf4j
public class ApiSecurityUtil {

    /**
     * API解密
     */
    public static String decrypt(){
        try {
            //从RequestContextHolder中获取request对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            //AES加密后的数据
            String data = request.getParameter("data");
            //后端RSA公钥加密后的AES的key
            String aesKey = request.getParameter("aesKey");

            //后端私钥解密的到AES的key
            byte[] plaintext = RsaUtil.decryptByPrivateKey(Base64.decodeBase64(aesKey), RsaUtil.getPrivateKey());
            aesKey = new String(plaintext);

            //AES解密得到明文data数据
            return AesUtil.decrypt(data, aesKey);
        } catch (Throwable e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
            throw new RuntimeException("ApiSecurityUtil.decrypt：解密异常！");
        }
    }

    /**
     * API加密
     */
    public static Result encrypt(Object object){
        try {
            //从RequestContextHolder中获取request对象
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            //前端公钥
            String publicKey = request.getParameter("publicKey");

            //随机获取AES的key，加密data数据
            String key = AesUtil.getKey();

            String dataString;
            if(object instanceof String){
                dataString = String.valueOf(object);
            }else{
                dataString = JsonUtil.stringify(object);
            }

            //随机AES的key加密后的密文
            String data = AesUtil.encrypt(dataString, key);

            //用前端的公钥来解密AES的key，并转成Base64
            String aesKey = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(key.getBytes(), publicKey));

            return Result.of(JsonUtil.parse("{\"data\":\"" + data + "\",\"aesKey\":\"" + aesKey + "\"}", Object.class));
        } catch (Throwable e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
            throw new RuntimeException("ApiSecurityUtil.encrypt：加密异常！");
        }
    }
}

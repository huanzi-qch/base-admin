package cn.huanzi.qch.baseadmin.aspect;

import cn.huanzi.qch.baseadmin.annotation.Decrypt;
import cn.huanzi.qch.baseadmin.annotation.Encrypt;
import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.util.AesUtil;
import cn.huanzi.qch.baseadmin.util.ErrorUtil;
import cn.huanzi.qch.baseadmin.util.RsaUtil;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

/**
 * AES + RSA 加解密AOP处理
 */
@Slf4j
@Aspect
@Component
public class SafetyAspect {

    /**
     * Pointcut 切入点
     * 匹配
     * cn.huanzi.qch.baseadmin.sys.*.controller、
     * cn.huanzi.qch.baseadmin.*.controller包下面的所有方法
     */
    @Pointcut(value = "execution(public * cn.huanzi.qch.baseadmin.sys.*.controller.*.*(..)) || " +
            "execution(public * cn.huanzi.qch.baseadmin.*.controller.*.*(..))")
    public void safetyAspect() {}

    /**
     * 环绕通知
     */
    @Around(value = "safetyAspect()")
    public Object around(ProceedingJoinPoint pjp) {
       try {

           //判断api加密开关是否开启
           if("N".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
               return pjp.proceed(pjp.getArgs());
           }

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            //request对象
            HttpServletRequest request = attributes.getRequest();

            //http请求方法  post get
            String httpMethod = request.getMethod().toLowerCase();

            //method方法
            Method method = ((MethodSignature) pjp.getSignature()).getMethod();

            //method方法上面的注解
            Annotation[] annotations = method.getAnnotations();

            //方法的形参参数
            Object[] args = pjp.getArgs();

            //是否有@Decrypt
            boolean hasDecrypt = false;
            //是否有@Encrypt
            boolean hasEncrypt = false;
            for (Annotation annotation : annotations) {
                if (annotation.annotationType() == Decrypt.class) {
                    hasDecrypt = true;
                }
                if (annotation.annotationType() == Encrypt.class) {
                    hasEncrypt = true;
                }
            }

            //前端公钥
            String publicKey = null;

            //jackson
            ObjectMapper mapper = new ObjectMapper();
            //jackson 序列化和反序列化 date处理
            mapper.setDateFormat( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

            //执行方法之前解密，且只拦截post请求
            if ("post".equals(httpMethod) && hasDecrypt) {
                //AES加密后的数据
                String data = request.getParameter("data");
                //后端RSA公钥加密后的AES的key
                String aesKey = request.getParameter("aesKey");
                //前端公钥
                publicKey = request.getParameter("publicKey");

                log.info("前端公钥：" + publicKey);

                //后端私钥解密的到AES的key
                byte[] plaintext = RsaUtil.decryptByPrivateKey(Base64.decodeBase64(aesKey), RsaUtil.getPrivateKey());
                aesKey = new String(plaintext);
                log.info("解密出来的AES的key：" + aesKey);

                //RSA解密出来字符串多一对双引号
                aesKey = aesKey.substring(1, aesKey.length() - 1);

                //AES解密得到明文data数据
                String decrypt = AesUtil.decrypt(data, aesKey);
                log.info("解密出来的data数据：" + decrypt);

                //设置到方法的形参中，目前只能设置只有一个参数的情况
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                //注：参数最好用Vo对象来接参，单用String来接，args有长度但获取为空，很奇怪不知道为什么
                if(args.length > 0){
                    args[0] = mapper.readValue(decrypt, args[0].getClass());
                }
            }

            //执行并替换最新形参参数   PS：这里有一个需要注意的地方，method方法必须是要public修饰的才能设置值，private的设置不了
            Object o = pjp.proceed(args);

            //返回结果之前加密
            if (hasEncrypt) {
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                //每次响应之前随机获取AES的key，加密data数据
                String key = AesUtil.getKey();
                log.info("AES的key：" + key);
                String dataString = mapper.writeValueAsString(o);
                log.info("需要加密的data数据：" + dataString);
                String data = AesUtil.encrypt(dataString, key);

                //用前端的公钥来解密AES的key，并转成Base64
                String aesKey = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(key.getBytes(), publicKey));

                //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
                o = Result.of(mapper.readValue("{\"data\":\"" + data + "\",\"aesKey\":\"" + aesKey + "\"}", Object.class));
            }

            //返回
            return o;

        } catch (Throwable e) {
           //输出到日志文件中
           log.error(ErrorUtil.errorInfoToString(e));
            return Result.of(null, false, "加解密异常：\n\t" + e.getMessage());
        }
    }
}

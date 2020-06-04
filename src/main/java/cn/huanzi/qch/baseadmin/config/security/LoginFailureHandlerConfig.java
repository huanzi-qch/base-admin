package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.util.AesUtil;
import cn.huanzi.qch.baseadmin.util.ErrorUtil;
import cn.huanzi.qch.baseadmin.util.RsaUtil;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

/**
 * 登录失败处理
 */
@Component
@Slf4j
public class LoginFailureHandlerConfig implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        String msg = "{\"code\":\"400\",\"msg\":\"用户名或密码错误\"}";

        //判断api加密开关是否开启
        if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
            //加密
            try {
                //前端公钥
                String publicKey = httpServletRequest.getParameter("publicKey");

                log.info("前端公钥：" + publicKey);

                //jackson
                ObjectMapper mapper = new ObjectMapper();
                //jackson 序列化和反序列化 date处理
                mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                //每次响应之前随机获取AES的key，加密data数据
                String key = AesUtil.getKey();
                log.info("AES的key：" + key);
                log.info("需要加密的data数据：" + msg);
                String data = AesUtil.encrypt(msg, key);

                //用前端的公钥来解密AES的key，并转成Base64
                String aesKey = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(key.getBytes(), publicKey));
                msg = "{\"data\":{\"data\":\"" + data + "\",\"aesKey\":\"" + aesKey + "\"}}";
            } catch (Throwable ee) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(ee));
            }
        }

        //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        PrintWriter out = httpServletResponse.getWriter();
        out.print(msg);
        out.flush();
        out.close();
    }
}

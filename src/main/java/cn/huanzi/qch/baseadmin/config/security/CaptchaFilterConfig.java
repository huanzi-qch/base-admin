package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.common.pojo.ParameterRequestWrapper;
import cn.huanzi.qch.baseadmin.util.AesUtil;
import cn.huanzi.qch.baseadmin.util.ErrorUtil;
import cn.huanzi.qch.baseadmin.util.RsaUtil;
import cn.huanzi.qch.baseadmin.util.SysSettingUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * 校验账号、密码前，先进行验证码处理，需要在这里进行登录解密操作
 */
@Component
@Slf4j
public class CaptchaFilterConfig implements Filter {

    @Value("${captcha.enable}")
    private Boolean captchaEnable;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Autowired
    private SessionRegistry sessionRegistry;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        /*
            注：详情可在SessionManagementFilter中进行断点调试查看
            security框架会在session的attribute存储登录信息，先从session.getAttribute(this.springSecurityContextKey)中获取登录用户信息
            ，如果没有，再从本地上下文SecurityContextHolder.getContext().getAuthentication()获取，因此想要强制用户下线得进行如下操作

            另外，虽然重启了服务，sessionRegistry.getAllSessions()为空，但之前的用户session未过期同样能访问系统，也是这个原因
         */
        SessionInformation sessionInformation = sessionRegistry.getSessionInformation(session.getId());
        if(sessionInformation == null && session.getAttribute("SPRING_SECURITY_CONTEXT") != null){
            //直接输出js脚本跳转强制用户下线
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().print("<script type='text/javascript'>window.location.href = '" + contextPath + "/logout'</script>");
        }

        //只拦截登录请求，且开发环境下不拦截
        if ("POST".equals(request.getMethod()) && "/login".equals(request.getRequestURI().replaceFirst(contextPath,""))) {
            //前端公钥
            String publicKey = null;

            //jackson
            ObjectMapper mapper = new ObjectMapper();
            //jackson 序列化和反序列化 date处理
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

            //判断api加密开关是否开启
            if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
                //解密
                try {
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

                    //new一个自定义RequestWrapper
                    HashMap hashMap = mapper.readValue(decrypt, HashMap.class);
                    ParameterRequestWrapper parameterRequestWrapper = new ParameterRequestWrapper(request);
                    for (Object key : hashMap.keySet()) {
                        parameterRequestWrapper.addParameter((String) key,  hashMap.get(key));
                    }

                    servletRequest = parameterRequestWrapper;
                    request = (HttpServletRequest) servletRequest;
                } catch (Throwable e) {
                    //输出到日志文件中
                    log.error(ErrorUtil.errorInfoToString(e));
                }
            }

            //从session中获取生成的验证码
            String verifyCode = session.getAttribute("verifyCode").toString();

            if (captchaEnable && !verifyCode.toLowerCase().equals(request.getParameter("captcha").toLowerCase())) {
                String dataString = "{\"code\":\"400\",\"msg\":\"验证码错误\"}";

                //判断api加密开关是否开启
                if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
                    //加密
                    try {
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    //每次响应之前随机获取AES的key，加密data数据
                    String key = AesUtil.getKey();
                    log.info("AES的key：" + key);
                    log.info("需要加密的data数据：" + dataString);
                    String data = AesUtil.encrypt(dataString, key);

                    //用前端的公钥来解密AES的key，并转成Base64
                    String aesKey = Base64.encodeBase64String(RsaUtil.encryptByPublicKey(key.getBytes(), publicKey));
                    dataString = "{\"data\":{\"data\":\"" + data + "\",\"aesKey\":\"" + aesKey + "\"}}";
                } catch (Throwable e) {
                        //输出到日志文件中
                        log.error(ErrorUtil.errorInfoToString(e));
                    }
                }

                //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=utf-8");
                PrintWriter out = response.getWriter();
                out.print(dataString);
                out.flush();
                out.close();
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}

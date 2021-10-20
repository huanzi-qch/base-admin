## API加解密<br/> 

![](https://img2018.cnblogs.com/blog/1353055/201905/1353055-20190523171207110-431742561.png)  <br/>
　　说人话就是前、后端各自生成自己的RSA秘钥对（公钥、私钥），然后交换公钥（后端给前端的是正常的明文公钥，前端给后端的是用后端公钥加密后的密文公钥；PS：其实我觉得直接交换两个明文公钥就行了），后端生成AES的明文key，用明文key进行AES加密得到密文数据，用前端的公钥进行RSA加密得到密文key，API交互时并将密文数据与密文key进行传输，前端用自己的私钥进行RAS解密的到明文key，用明文key进行AES解密得到明文数据；前端给后端发送数据时同理，这样一来，传输的数据都是密文，且只有秘钥才能解密<br/>

　　思路总结 <br/>
　　1、前端：重写$.ajax方法（或者封装一个ajax），发送数据前用AES加密数据（key随机生成），用后端的RSA公钥加密AES的key，将加密后的data数据、加密后的AES的key、前端RSA公钥发送到后端；触发回调后，先用前端RSA私钥解密AES的key，在用明文key去解密<br/>
　　2、后端：写两个自定义注解Encrypt、Decrypt，AOP拦截所有带自定义注解的post请求进行加密解密，有@Encrypt需要对返回值进行加密，有@Decrypt需要对参数进行解密，加密解密过程与前端的操作同理<br/>
　　3、API加密中，由于登录校验是Spring Security做的，因此我们要在UsernamePasswordAuthenticationFilter获取账号、密码之前完成解密操作，正好我们的校验验证码操作就是在它之前，同时要做响应数据的加密操作，所以登录部分的API加密光使用AOP来还是不够的，需要在CaptchaFilterConfig进行解密操作，解密后new一个自定义RequestWrapper设置Parameter，并将这个新对象传到doFilter交由下一步处理，登录成功LoginSuccessHandlerConfig/失败LoginFailureHandlerConfig处理也要进行加解密处理<br/>

　　要注意的是：<br/>
　　1、我们在aop只设置了第一个参数，因此controller方法需要是实体接参且第一个参数就是，所有要求，要么有一个实体Vo参数，要么没有参数；<br/>
　　2、对于返回值，需要是统一的返回值，因为我们目前是按统一的返回值设置值的，例如本例中的Result，是我们约定好的统一返回值（后续升级可以用反射来设置值）；<br/>
　　3、还有一个需要注意的地方，method方法必须是要public修饰的才能设置方法的形参值，private的设置不了；<br/>

　　详情请看我之前的博客：[前后端API交互数据加密——AES与RSA混合加密完整实例](https://www.cnblogs.com/huanzi-qch/p/10913636.html)<br/> 

## 效果<br/>
　　请求参数加密<br/> 
![](https://img2018.cnblogs.com/blog/1353055/201909/1353055-20190917143928654-548189551.png)<br/> 
　　响应数据加密<br/> 
![](https://img2018.cnblogs.com/blog/1353055/201909/1353055-20190917143958364-1128612216.png)<br/> 
　　开启API加密<br/> 
![](https://img2020.cnblogs.com/blog/1353055/202006/1353055-20200604120015648-265532162.png)<br/> 
　　关闭API加密<br/> 
![](https://img2020.cnblogs.com/blog/1353055/202006/1353055-20200604120034222-1818979193.png)<br/> 


## 代码 <br/>
　　由于在不少地方都需要进行加解密操作，因此封装ApiSecurityUtil工具类<br/>
```

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
```
　　访问login或者index时进行后端密钥对生成，同时前端保存后端公钥<br/>
```
    /**
     * 跳转登录页面
     */
    @GetMapping("loginPage")
    public ModelAndView login(){
        ModelAndView modelAndView = new ModelAndView();
        
        //省略其他代码...

        //后端公钥
        String publicKey = RsaUtil.getPublicKey();
        modelAndView.addObject("publicKey", publicKey);
        
        //省略其他代码...

        return modelAndView;
    }
    
    PS：index同理..
```
　　前端重写jQuery的ajax方法，发送请求前加密请求数据、收到响应后解密响应数据<br/>
```
/**
 * jQuery扩展
 */
jQueryExtend = {
    /**
     * 是否已经进行jq的ajax加密重写
     */
    ajaxExtendFlag : false,

    //省略其他代码...

    /**
     * 重写jq的ajax加密，并保留原始ajax，命名为_ajax
     */
    ajaxExtend : function(){
        //判断api加密开关
        if(sessionStorage.getItem('sysApiEncrypt') === "Y" && !jQueryExtend.ajaxExtendFlag){
            jQueryExtend.ajaxExtendFlag = true;
            let _ajax = $.ajax;//首先备份下jquery的ajax方法
            $.ajax = function (opt) {
                //默认值
                // opt = {
                //     type: 'post',
                //     url: url,
                //     contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
                //     dataType: 'json',
                //     data: data,
                //     success: success,
                //     error: function (xhr, status, error) {
                //         console.log("ajax错误！");
                //     }
                // };

                //备份opt中error和success方法
                let fn = {
                    error: function (XMLHttpRequest, textStatus, errorThrown) {
                    },
                    success: function (data, textStatus) {
                    }
                };
                if (opt.error) {
                    fn.error = opt.error;
                }
                if (opt.success) {
                    fn.success = opt.success;
                }

                //加密再传输
                if (opt.type.toLowerCase() === "post") {
                    let data = opt.data;
                    //发送请求之前随机获取AES的key
                    let aesKey = aesUtil.genKey();
                    data = {
                        data: aesUtil.encrypt(data, aesKey),//AES加密后的数据
                        aesKey: rsaUtil.encrypt(aesKey, sessionStorage.getItem('javaPublicKey')),//后端RSA公钥加密后的AES的key
                        publicKey: window.jsPublicKey//前端公钥
                    };
                    opt.data = data;
                }

                //扩展增强处理
                let _opt = $.extend(opt, {
                    //成功回调方法增强处理
                    success: function (data, textStatus) {
                        if (opt.type.toLowerCase() === "post") {
                            data = aesUtil.decrypt(data.data.data, rsaUtil.decrypt(data.data.aesKey, window.jsPrivateKey));
                        }
                        //先获取明文aesKey，再用明文key去解密数据
                        fn.success(data, textStatus);
                    }
                });
                return _ajax(_opt);
            };
        }
    },
};

//重写jq的ajax加密
jQueryExtend.ajaxExtend();
```
　　后端aop处理、以及登录、登出操作相关处理<br/>
```

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
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
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

        //执行方法之前解密，且只拦截post请求
        if ("post".equals(httpMethod) && hasDecrypt) {
            //api解密
            String decrypt = ApiSecurityUtil.decrypt();

            //注：参数最好用Vo对象来接参，单用String来接，args有长度但获取为空，很奇怪不知道为什么
            if(args.length > 0){
                args[0] = JsonUtil.parse(decrypt, args[0].getClass());
            }
        }

        //执行并替换最新形参参数   PS：这里有一个需要注意的地方，method方法必须是要public修饰的才能设置值，private的设置不了
        Object o = pjp.proceed(args);

        //返回结果之前加密
        if (hasEncrypt) {
            //api加密，转json字符串并转成Object对象，设置到Result中并赋值给返回值o
            o = ApiSecurityUtil.encrypt(o);
        }

        //返回
        return o;
    }
}
```
```

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
    private SecurityUtil securityUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String requestUri = request.getRequestURI();
        
        //省略其他代码...

        //只拦截登录请求，且开发环境下不拦截
        if ("POST".equals(request.getMethod()) && "/login".equals(requestUri.replaceFirst(contextPath,""))) {
            //判断api加密开关是否开启
            if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
                //api解密
                String decrypt = ApiSecurityUtil.decrypt();

                //new一个自定义RequestWrapper
                HashMap hashMap = JsonUtil.parse(decrypt, HashMap.class);
                ParameterRequestWrapper parameterRequestWrapper = new ParameterRequestWrapper(request);
                for (Object key : hashMap.keySet()) {
                    parameterRequestWrapper.addParameter(String.valueOf(key),  hashMap.get(key));
                }

                request = parameterRequestWrapper;
            }

            //从session中获取生成的验证码
            String verifyCode = session.getAttribute("verifyCode").toString();

            if (captchaEnable && !verifyCode.toLowerCase().equals(request.getParameter("captcha").toLowerCase())) {
                String dataString = "{\"code\":\"400\",\"msg\":\"验证码错误\"}";

                //判断api加密开关是否开启
                if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
                    //api加密
                    Result encrypt = ApiSecurityUtil.encrypt(dataString);

                    dataString = JsonUtil.stringify(encrypt);
                }

                //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
                HttpServletResponseUtil.printJson(response,dataString);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
```
```

/**
 * 登录成功处理，登陆成功后还需要验证账号的有效性
 */
@Component
@Slf4j
public class LoginSuccessHandlerConfig implements AuthenticationSuccessHandler {
    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private SysUserService sysUserService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {

        //查询当前与系统交互的用户，存储在本地线程安全上下文，校验账号有效性
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Map<String, Object> map = securityUtil.checkUserByUserData(httpServletRequest,user.getUsername());
        String msg = map.get("msg").toString();

        //省略其他代码...

        //判断api加密开关是否开启
        if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())) {
            //api加密
            Result encrypt = ApiSecurityUtil.encrypt(msg);

            msg = JsonUtil.stringify(encrypt);
        }

        //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
        HttpServletResponseUtil.printJson(httpServletResponse,msg);
    }
}
```
```

/**
 * 登录失败处理
 */
@Component
@Slf4j
public class LoginFailureHandlerConfig implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        String msg = "{\"code\":\"400\",\"msg\":\"用户名或密码错误\"}";

        //省略其他代码...

        //判断api加密开关是否开启
        if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
            //加密
            try {
                //api加密
                Result encrypt = ApiSecurityUtil.encrypt(msg);

                msg = JsonUtil.stringify(encrypt);
            } catch (Throwable ee) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(ee));
            }
        }

        //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
        HttpServletResponseUtil.printJson(httpServletResponse,msg);
    }
}

```
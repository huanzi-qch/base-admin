## 用户登录 <br/>
>配置文件分支选择，dev环境无需输入验证码，同时为了方便演示，密码输入框的类型改成text部署正式环境前记得改回来

　　支持七天免登陆，同时支持多种登录限制<br/>
　　　　1、允许/禁止账号多人在线<br/>
　　　　2、软删除<br/>
　　　　3、限制登录IP地址<br/>
　　　　4、账号过期<br/>
　　　　更多登录限制，还可以继续扩展<br/>
　　<br/>

## 代码 <br/>
　　SecurityConfig中配置登录相关配置<br/>
```
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CaptchaFilterConfig captchaFilterConfig;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private PasswordConfig passwordConfig;

    @Autowired
    private LoginFailureHandlerConfig loginFailureHandlerConfig;

    @Autowired
    private LoginSuccessHandlerConfig loginSuccessHandlerConfig;

    @Autowired
    private LogoutHandlerConfig logoutHandlerConfig;

    //省略其他代码...

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                //用户认证处理
                .userDetailsService(userDetailsServiceImpl)
                //密码处理
                .passwordEncoder(passwordConfig);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        
        //省略其他代码...
        
        http
                //登录处理
                .addFilterBefore(captchaFilterConfig, UsernamePasswordAuthenticationFilter.class)
                .formLogin()
                .loginProcessingUrl("/login")
                //未登录时默认跳转页面
                .loginPage("/loginPage")
                .failureHandler(loginFailureHandlerConfig)
                .successHandler(loginSuccessHandlerConfig)
                .permitAll()
                .and();
        http
                //登出处理
                .logout()
                .addLogoutHandler(logoutHandlerConfig)
                .logoutUrl("/logout")
                .logoutSuccessUrl("/loginPage")
                .permitAll()
                .and();

        http
                //开启记住我
                .rememberMe()
                .tokenValiditySeconds(604800)//七天免登陆
                .tokenRepository(persistentTokenRepository())
                .userDetailsService(userDetailsServiceImpl)
                .rememberMeServices(myPersistentTokenBasedRememberMeServices())
                .and();
                
        //省略其他代码...
              
    }

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl persistentTokenRepository = new JdbcTokenRepositoryImpl();
        persistentTokenRepository.setDataSource(dataSource);
        return persistentTokenRepository;
    }

    @Bean()
    public MyPersistentTokenBasedRememberMeServices myPersistentTokenBasedRememberMeServices() {
        MyPersistentTokenBasedRememberMeServices rememberMeServices = new MyPersistentTokenBasedRememberMeServices(UUIDUtil.getUuid(), userDetailsServiceImpl,persistentTokenRepository());
        rememberMeServices.setAlwaysRemember(true);
        return rememberMeServices;
    }

    //省略其他代码...
    
}
```
　　登录流程<br/>
　　　　1、访问“无需权限访问的URL”以外的URL，跳转/loginPage登录页面<br/>
　　　　2、携带请求数据，发起登录请求/login<br/>
　　　　3、进入到前置过滤器CaptchaFilterConfig，校验账号、密码前，先进行验证码处理，需要在这里进行登录解密操作<br/>
　　　　4、执行UserDetailsServiceImpl，查询用户信息（账号、密码、权限等）<br/>
　　　　5.1、如果上一步能查不到用户信息，进入LoginFailureHandlerConfig，返回用户名或密码错误<br/>
　　　　5.2、如果上一步查到用户信息，进入到LoginSuccessHandlerConfig（还需要验证账号的有效性），校验通过则注册SessionRegistry、以及设置其他相关数据<br/>
　　<br/>

　　登出流程<br/>
　　　　1、发起登出请求/logout，<br/>
　　　　2、进入到LogoutHandlerConfig注销处理，注销SessionRegistry、以及清除其他相关数据<br/>
　　<br/>

## 七天免登陆 <br/>
　　登录时勾选“七天免登陆”，七天内访问系统都不需要重新登录，系统会通过cookie信息自动登录<br/>
　　RememberMe是Security自带的功能，但并不能满足我们的需求，因为我们是自己维护SessionRegistry<br/>
　　在请求进入前置过滤器CaptchaFilterConfig，执行对RememberMe的处理，从而实现自动登录<br/>
```
@Component
@Slf4j
public class CaptchaFilterConfig implements Filter {

    //省略其他代码...

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
        /*
            注：详情可在SessionManagementFilter中进行断点调试查看
            security框架会在session的attribute存储登录信息，先从session.getAttribute(this.springSecurityContextKey)中获取登录用户信息
            ，如果没有，再从本地上下文SecurityContextHolder.getContext().getAuthentication()获取，因此想要强制用户下线得进行如下操作

            另外，虽然重启了服务，sessionRegistry.getAllSessions()为空，但之前的用户session未过期同样能访问系统，也是这个原因
         */
        User user = securityUtil.sessionRegistryGetUserBySessionId(session.getId());
        Cookie rememberMeCookie = SecurityUtil.getRememberMeCookie(request);
        if(user == null && rememberMeCookie != null){

            //remember me？
            PersistentRememberMeToken token = securityUtil.rememberMeGetTokenForSeries(rememberMeCookie);

            /*
                不允许自动登录
                查无token令牌
                当前URL需要登录才能访问，但当前账号不满足登录限制（七天免登陆、禁止多人在线等登录限制有冲突）
             */
            boolean flag0 = StringUtils.isEmpty(token);
            boolean flag1 = !SecurityUtil.checkUrl(requestUri.replaceFirst(contextPath, ""));
            boolean flag2 = !flag0 && Boolean.valueOf(securityUtil.checkUserByUserData(request, token.getUsername()).get("flag").toString());
            if(flag0 || (flag1 && flag2)){
                log.info("访问{}，尝试自动登录失败，查无token令牌或当前账号不满足登录限制...",requestUri);
                HttpServletResponseUtil.printHtml(response,"<script type='text/javascript'>window.location.href = '" + contextPath + "/logout'</script>");
                return;
            }

            if(flag1) {
                log.info("访问{}，当前session连接开启了免登陆，已自动登录！token：{},userName：{}，最后登录时间：{}",requestUri,rememberMeCookie.getValue(),token.getUsername(),token.getDate());
                //注册新的session
                securityUtil.sessionRegistryAddUser(session.getId(), userDetailsServiceImpl.loadUserByUsername(token.getUsername()));

                //保存登录信息
                user = securityUtil.sessionRegistryGetUserBySessionId(session.getId());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,user.getPassword(),user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetails(request));

                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authentication);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,securityContext);

                //更新token信息
                securityUtil.updateRememberMeByToken(request,response,token);

                //最后登录时间
                SysUserVo sysUserVo = sysUserService.findByLoginName(user.getUsername()).getData();
                sysUserVo.setLastLoginTime(new Date());
                sysUserService.save(sysUserVo);
            }

        }

        //省略其他代码...

        filterChain.doFilter(request, response);
    }
}

```
![](https://img2020.cnblogs.com/blog/1353055/202105/1353055-20210518172357607-188016356.png)<br/>

## 获取当前登录用户<br/>

后台获取<br/>
```
@Autowired
private SysUserService sysUserService;

User user = SecurityUtil.getLoginUser();
SysUserVo sysUserVo = sysUserService.findByLoginName(SecurityUtil.getLoginUser().getUsername()).getData();
```

前端并没有进行封装，如果需要，可以在index.html中这样做<br/>
```
<script th:inline="javascript">
    //省略其他代码...
    
    //可以将登录用户信息存在sessionStorage中
    sessionStorage.setItem('loginUser', [[${loginUser}]]);
</script>
```
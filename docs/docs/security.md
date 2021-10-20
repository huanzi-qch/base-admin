## Security安全框架 <br/>
　　安全框架，我们使用的是目前最流行的两大安全框架之一：SpringSecruity<br/>
　　Spring Security官网：https://spring.io/projects/spring-security<br/>
　　Spring Security是一个功能强大且高度可定制的身份验证和访问控制框架，侧重于为Java应用程序提供身份验证和授权。Security通过大量的拦截器进行校验，具体请看官网列出的列表：https://docs.spring.io/spring-security/site/docs/4.2.4.RELEASE/reference/htmlsingle/#ns-custom-filters<br/>

## 核心配置 <br/>
　　核心配置在SecurityConfig<br/>
```
|-- base-admin
    |-- src
    |   |-- main
    |   |   |-- java
    |   |   |   |-- cn
    |   |   |       |-- huanzi
    |   |   |           |-- qch
    |   |   |               |-- baseadmin
    |   |   |                   |-- BaseAdminApplication.java   【APP启动类】
    |   |   |                   |-- 省略其他部分...
    |   |   |                   |-- config  【配置】
    |   |   |                   |   |-- 省略其他部分...
    |   |   |                   |   |-- security    【Spring Security安全框架配置】
    |   |   |                   |   |   |-- CaptchaFilterConfig.java        【前置过滤器，校验账号、密码前，先进行其他处理】
    |   |   |                   |   |   |-- DynamicallyUrlInterceptor.java  【自定义动态数据拦截器】
    |   |   |                   |   |   |-- ErrorPageConfig.java            【自定义errorPage】
    |   |   |                   |   |   |-- LoginFailureHandlerConfig.java  【登录失败处理】
    |   |   |                   |   |   |-- LoginSuccessHandlerConfig.java  【登录成功处理，登陆成功后还需要验证账号的有效性】
    |   |   |                   |   |   |-- LogoutHandlerConfig.java        【注销处理】
    |   |   |                   |   |   |-- MyAccessDecisionManager.java    【权限认证管理器】
    |   |   |                   |   |   |-- MyFilterInvocationSecurityMetadataSource.java   【权限认证数据源，实现动态权限加载】
    |   |   |                   |   |   |-- MyPersistentTokenBasedRememberMeServices.java   【RememberMeServices】
    |   |   |                   |   |   |-- PasswordConfig.java             【密码处理】
    |   |   |                   |   |   |-- SecurityConfig.java             【核心配置】
    |   |   |                   |   |   |-- UserDetailsServiceImpl.java     【用户认证处理】
    |   |   |                   |   |-- 省略其他部分...
    |   |   |                   |-- 省略其他部分...
    |-- 省略其他部分...
```
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

    @Autowired
    private SysAuthorityService sysAuthorityService;

    @Autowired
    private MyFilterInvocationSecurityMetadataSource myFilterInvocationSecurityMetadataSource;

    @Autowired
    private DataSource dataSource;

    //无需权限访问的URL，不建议用/**/与/*.后缀同时去适配，有可以会受到CaptchaFilterConfig判断的影响
    public static final String[] MATCHERS_PERMITALL_URL = {
            "/login",
            "/logout",
            "/loginPage",
            "/favicon.ico",
            "/common/**",
            "/webjars/**",
            "/getVerifyCodeImage",
            "/error/*",
            "/openApi/*"
    };

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
        http
                // 关闭csrf防护
                .csrf().disable()
                .headers().frameOptions().disable()
                .and();

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
                //定制url访问权限，动态权限读取，参考：https://www.jianshu.com/p/0a06496e75ea
                .addFilterAfter(dynamicallyUrlInterceptor(), FilterSecurityInterceptor.class)
                .authorizeRequests()

                //无需权限访问
                .antMatchers(MATCHERS_PERMITALL_URL).permitAll()

                //其他接口需要登录后才能访问
                .anyRequest().authenticated()
                .and();

        http
                //开启记住我
                .rememberMe()
                .tokenValiditySeconds(604800)//七天免登陆
                .tokenRepository(persistentTokenRepository())
                .userDetailsService(userDetailsServiceImpl)
                .rememberMeServices(myPersistentTokenBasedRememberMeServices())
                .and();
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

    @Bean
    public DynamicallyUrlInterceptor dynamicallyUrlInterceptor(){
        //首次获取
        myFilterInvocationSecurityMetadataSource.setRequestMap(sysAuthorityService.list(new SysAuthorityVo()).getData());
        //初始化拦截器并添加数据源
        DynamicallyUrlInterceptor interceptor = new DynamicallyUrlInterceptor();
        interceptor.setSecurityMetadataSource(myFilterInvocationSecurityMetadataSource);

        //配置RoleVoter决策
        List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>(1);
        decisionVoters.add(new RoleVoter());

        //设置认证决策管理器
        interceptor.setAccessDecisionManager(new MyAccessDecisionManager(decisionVoters));
        return interceptor;
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
```

>权限控制，无非就是：前端控件是否可见、是否允许请求/访问URL，这里分享[一个简单的URL访问权限校验](https://www.cnblogs.com/huanzi-qch/p/15252779.html)
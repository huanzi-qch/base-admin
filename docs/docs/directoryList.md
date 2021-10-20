## 项目结构说明

>目录结构由mddir工具自动生成 <br/>
1、安装：npm install mddir -g <br/>
2、打开cmd，进入到指定文件夹后使用命令，就会生成这个directoryList.md文件：mddir <br/>

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
    |   |   |                   |-- annotation  【自定义注解】
    |   |   |                   |   |-- Between.java    【SqlUtil拼接日期范围查询】
    |   |   |                   |   |-- Decrypt.java    【API加解密时判断是否需要进行解密】
    |   |   |                   |   |-- Encrypt.java    【API加解密时判断是否需要进行加密】
    |   |   |                   |   |-- In.java         【SqlUtil拼接in查询】
    |   |   |                   |   |-- Like.java       【SqlUtil拼接like查看】
    |   |   |                   |-- aspect  【aop切面】
    |   |   |                   |   |-- OpenApiAspect.java  【open api接口限流aop切面】
    |   |   |                   |   |-- SafetyAspect.java   【API加解密aop切面】
    |   |   |                   |-- autogenerator   【代码生成器】
    |   |   |                   |   |-- AutoGenerator.java      【第一代代码生成器，V1.0】
    |   |   |                   |   |-- AutoGeneratorPlus.java  【第二代代码生成器，V2.0】
    |   |   |                   |-- common  【一套通用代码，单表继承即可实现CRUD、分页等基础接口】
    |   |   |                   |   |-- controller  【通用Controller】
    |   |   |                   |   |   |-- CommonController.java
    |   |   |                   |   |-- pojo        【公用实体类、例如统一返回对象Result】
    |   |   |                   |   |   |-- HolidayVo.java
    |   |   |                   |   |   |-- IpVo.java
    |   |   |                   |   |   |-- MonitorVo.java
    |   |   |                   |   |   |-- PageCondition.java
    |   |   |                   |   |   |-- PageInfo.java
    |   |   |                   |   |   |-- ParameterRequestWrapper.java
    |   |   |                   |   |   |-- Result.java
    |   |   |                   |   |-- repository  【通用repository】
    |   |   |                   |   |   |-- CommonRepository.java
    |   |   |                   |   |-- service     【通用service】
    |   |   |                   |       |-- CommonService.java
    |   |   |                   |       |-- CommonServiceImpl.java
    |   |   |                   |-- config  【配置】
    |   |   |                   |   |-- async   【优雅异步配置】
    |   |   |                   |   |   |-- AsyncConfig.java
    |   |   |                   |   |-- cors    【cors安全跨域】
    |   |   |                   |   |   |-- CorsConfig.java
    |   |   |                   |   |-- logback 【实时日志配置】
    |   |   |                   |   |   |-- LoggingWSServer.java
    |   |   |                   |   |-- monitor 【实时监控配置】
    |   |   |                   |   |   |-- MonitorWSServer.java
    |   |   |                   |   |-- security    【Spring Security安全框架配置】
    |   |   |                   |   |   |-- CaptchaFilterConfig.java
    |   |   |                   |   |   |-- DynamicallyUrlInterceptor.java
    |   |   |                   |   |   |-- ErrorPageConfig.java
    |   |   |                   |   |   |-- LoginFailureHandlerConfig.java
    |   |   |                   |   |   |-- LoginSuccessHandlerConfig.java
    |   |   |                   |   |   |-- LogoutHandlerConfig.java
    |   |   |                   |   |   |-- MyAccessDecisionManager.java
    |   |   |                   |   |   |-- MyFilterInvocationSecurityMetadataSource.java
    |   |   |                   |   |   |-- MyPersistentTokenBasedRememberMeServices.java
    |   |   |                   |   |   |-- PasswordConfig.java
    |   |   |                   |   |   |-- SecurityConfig.java
    |   |   |                   |   |   |-- UserDetailsServiceImpl.java
    |   |   |                   |   |-- websocket   【websocket配置】
    |   |   |                   |       |-- MyEndpointConfigure.java
    |   |   |                   |       |-- WebSocketConfig.java
    |   |   |                   |-- eventlistener   【Spring的消息订阅、发布】
    |   |   |                   |   |-- SecurityMetadataSourceEventListener.java
    |   |   |                   |   |-- SysSettingEventListener.java
    |   |   |                   |   |-- eventsource
    |   |   |                   |       |-- SecurityMetadataSourceEventSource.java
    |   |   |                   |-- limiter 【限流桶，限流处理】
    |   |   |                   |   |-- RateLimiter.java
    |   |   |                   |-- openapi 【对外开放的接口，openApi】
    |   |   |                   |   |-- controller
    |   |   |                   |   |   |-- OpenApiController.java
    |   |   |                   |   |-- service
    |   |   |                   |       |-- OpenApiService.java
    |   |   |                   |       |-- OpenApiServiceImpl.java
    |   |   |                   |-- sys 【系统管理相关功能】
    |   |   |                   |   |-- sysauthority    【系统权限表，单表全套代码，继承通用代码实现CRUD、分页等基础接口】
    |   |   |                   |   |   |-- controller
    |   |   |                   |   |   |   |-- SysAuthorityController.java
    |   |   |                   |   |   |-- pojo
    |   |   |                   |   |   |   |-- SysAuthority.java
    |   |   |                   |   |   |-- repository
    |   |   |                   |   |   |   |-- SysAuthorityRepository.java
    |   |   |                   |   |   |-- service
    |   |   |                   |   |   |   |-- SysAuthorityService.java
    |   |   |                   |   |   |   |-- SysAuthorityServiceImpl.java
    |   |   |                   |   |   |-- vo
    |   |   |                   |   |       |-- SysAuthorityVo.java
    |   |   |                   |   |-- sysmenu 【系统菜单表，同上】
    |   |   |                   |   |   |-- ...
    |   |   |                   |   |-- syssetting 【系统设置表，同上】
    |   |   |                   |   |   |-- ...
    |   |   |                   |   |-- sysshortcutmenu 【系统用户个性菜单表，同上】
    |   |   |                   |   |   |-- ...
    |   |   |                   |   |-- sysuser 【系统用户表，同上】
    |   |   |                   |   |   |-- ...
    |   |   |                   |   |-- sysuserauthority 【系统用户-权限关联表，同上】
    |   |   |                   |   |   |-- ...
    |   |   |                   |   |-- sysusermenu【系统用户-菜单关联表，同上】
    |   |   |                   |       |-- ...
    |   |   |                   |-- timer   【定时器】
    |   |   |                   |   |-- ClearLoginUserScheduler.java
    |   |   |                   |-- user    【登录用户】
    |   |   |                   |   |-- controller
    |   |   |                   |   |   |-- UserController.java
    |   |   |                   |   |-- service
    |   |   |                   |       |-- UserService.java
    |   |   |                   |       |-- UserServiceImpl.java
    |   |   |                   |-- util    【工具类】
    |   |   |                       |-- AesUtil.java
    |   |   |                       |-- ApiSecurityUtil.java
    |   |   |                       |-- ByteUtil.java
    |   |   |                       |-- CopyUtil.java
    |   |   |                       |-- DateUtil.java
    |   |   |                       |-- ErrorUtil.java
    |   |   |                       |-- HolidayUtil.java
    |   |   |                       |-- HttpServletResponseUtil.java
    |   |   |                       |-- IpUtil.java
    |   |   |                       |-- JsonUtil.java
    |   |   |                       |-- MD5Util.java
    |   |   |                       |-- MenuUtil.java
    |   |   |                       |-- RsaUtil.java
    |   |   |                       |-- SecurityUtil.java
    |   |   |                       |-- SpringUtils.java
    |   |   |                       |-- SqlUtil.java
    |   |   |                       |-- SysSettingUtil.java
    |   |   |                       |-- SystemMonitorUtil.java
    |   |   |                       |-- UUIDUtil.java
    |   |   |                       |-- VerifyCodeImageUtil.java
    |   |   |-- resources
    |   |       |-- application.properties  【配置文件】
    |   |       |-- application.yml 【配置文件】
    |   |       |-- banner.txt  【自定义banner，启动时控制台打印标语】
    |   |       |-- logback-spring.xml  【logback日志配置】
    |   |       |-- static
    |   |       |   |-- common  【公用组件，以及js/css等】
    |   |       |   |   |-- common.css
    |   |       |   |   |-- common.js
    |   |       |   |   |-- encrypt
    |   |       |   |   |   |-- cryptojs.js
    |   |       |   |   |   |-- jsencrypt.js
    |   |       |   |   |-- jquery
    |   |       |   |   |   |-- jquery.js
    |   |       |   |   |-- layui
    |   |       |   |   |   |-- ...
    |   |       |   |   |-- ueditor
    |   |       |   |       |-- ...
    |   |       |   |-- sql     【sql脚本】
    |   |       |   |   |-- base_admin.sql
    |   |       |   |-- sys     【系统管理功能的js/css脚本】
    |   |       |   |   |-- authority
    |   |       |   |   |   |-- css
    |   |       |   |   |   |   |-- authority.css
    |   |       |   |   |   |-- js
    |   |       |   |   |       |-- authority.js
    |   |       |   |   |-- menu
    |   |       |   |   |   |-- ..
    |   |       |   |   |-- setting
    |   |       |   |   |   |-- ..
    |   |       |   |   |-- user
    |   |       |   |       |-- ..
    |   |       |   |-- user    【登录用户的js/css脚本】
    |   |       |       |-- css
    |   |       |       |   |-- shortcutmenu.css
    |   |       |       |   |-- userinfo.css
    |   |       |       |-- js
    |   |       |           |-- shortcutmenu.js
    |   |       |           |-- userinfo.js
    |   |       |-- tlf 【第二版代码生成器V2.0的模板文件】
    |   |       |   |-- controller.tlf
    |   |       |   |-- entity.tlf
    |   |       |   |-- entityvo.tlf
    |   |       |   |-- repository.tlf
    |   |       |   |-- service.tlf
    |   |       |   |-- serviceimpl.tlf
    |   |       |-- view    【前端页面文件】
    |   |           |-- index.html
    |   |           |-- logging.html
    |   |           |-- login.html
    |   |           |-- monitor.html
    |   |           |-- common  【公用前端文件】
    |   |           |   |-- head.html
    |   |           |   |-- error
    |   |           |       |-- 403.html
    |   |           |       |-- 404.html
    |   |           |       |-- 500.html
    |   |           |-- sys     【系统管理相关前端页面】
    |   |           |   |-- authority
    |   |           |   |   |-- authority.html
    |   |           |   |-- menu
    |   |           |   |   |-- menu.html
    |   |           |   |-- setting
    |   |           |   |   |-- setting.html
    |   |           |   |-- user
    |   |           |       |-- user.html
    |   |           |-- user    【登录用户前端文件】
    |   |               |-- shortcmenu.html
    |   |               |-- userinfo.html
    |-- LICENSE     【开源协议】
    |-- pom.xml     【pom文件】
    |-- README.md   【md说明文档】

```
## 简介<br/> 
Base Admin一套简单通用的后台管理系统<br/> 
这套Base Admin是一套简单通用的后台管理系统，主要功能有：权限管理、菜单管理、用户管理，系统设置、实时日志，实时监控，API加密，以及登录用户修改密码、配置个性菜单等<br/> 

## 技术栈<br/> 
前端：layui<br/> 
java后端：SpringBoot + Thymeleaf + WebSocket + Spring Security + SpringData-Jpa + MySql<br/> 

## 运行效果图<br/> 
![](https://huanzi-qch.gitee.io/file-server/images/base-admin.png) 

## 仓库地址<br/> 
国外：https://github.com/huanzi-qch/base-admin<br/> 
国内：https://gitee.com/huanzi-qch/base-admin<br/> 

## 前往博客查看详情<br/> 
具体介绍请看我的博客[《开源一套简单通用的后台管理系统》](https://www.cnblogs.com/huanzi-qch/p/11534203.html)<br/> 

## 常见问题<br/>
0、maven下载jar包长时间无反应？
```text
原因：网络原因连不上maven仓库或其他未知原因导致IDE间接性抽风，导致无法下载联网下载jar包

解决：网络原因自行解决，如果网络没问题就不要一直傻傻的等了，重启IDE，让它重新联网下载
```
1、IDE编译报错，识别不到实体类的set、get方法？
```text
原因：项目使用lombok开发，lombok会在生成class字节码文件帮我们生成set、get等方法，java文件没有set、get等方法，IDE索引不到set、get方法所以编译报错

解决：IDE安装lombok插件即可能识别到对应set、get方法，重启生效
``` 
2、数据库文件在哪？
```text
原因：没有好好看文档，建议先好好看下博客介绍，博客文末“代码开源”处已经早有说明

解决：base_admin.sql文件在resources/static/sql下面
```
3、如何启动程序？
```text
原因：对springboot项目不熟，建议先去了解一下springboot，感兴趣的可以去看我的springBoot开源项目

解决：等待IDE识别成springboot项目后，在BaseAdminApplication.java中运行main函数启动程序
```
4、测试账号/密码是多少？    PS：dev分支环境，默认关闭验证码校验
```text
账号/密码

sa/123456
```
5、如何逆向工程生成后端代码？我封装有一个工具类CodeDOM.java用于生成单表全套后端代码
```text
首先建好数据表，在该类中配置好数据源以及项目所在路径，在main函数的tables数组指定要生成代码表，运行main函数即可生成全套后端增删改查、分页代码

一套通用common代码，每个单表去继承从而实现这套基础代码，使用CodeDOM.java代码自动生成一套单表的基础增、删、改、查接口，大大提高开发效率，
详情见博客介绍：https://www.cnblogs.com/huanzi-qch/p/10281773.html
```
6、如何跳过登录，直接测试接口？
```text
场景：有的同学发现在“无需权限访问”那里配置了测试接口，但还是跳转到了登录页面，例如：/sys/sysUser/getUserById

原因：这是因为跟“权限管理”那里配置的url接口冲突了（例如：/sys/**），权限管理那里的配置优先级更高，因此还是会跳去登录页面

解决：暂时删除所有跟测试接口冲突的“权限管理”配置的url接口（例如：/sys/**，/sys/sysUser/*等）
```
7、ORM框架不想用JPA，如何快速转MyBatis-Plus？
```text
JPA、MyBatis-Plus我都有进行封装，编码风格高度统一，都是单表继承基础通用的代码，有代码自动生成工具，本项目用的就是JPA的封装，快速切换可看之前的博客

MP：SpringBoot系列——MyBatis-Plus整合封装（https://www.cnblogs.com/huanzi-qch/p/13561164.html）
JPA：SpringBoot系列——Spring-Data-JPA（究极进化版） 自动生成单表基础增、删、改、查接口（https://www.cnblogs.com/huanzi-qch/p/10281773.html）
```
8、我想升级成前后端分离项目，麻烦吗？应当如何下手？
```text
难度肯定还是有的，如何你对这个项目比较熟悉，相信你已早有思路，如果没有可以参考下面的博客文章

springboot+spring security +oauth2.0 demo搭建（password模式）（认证授权端与资源服务端分离的形式）（https://www.cnblogs.com/hetutu-5238/p/10022963.html）
GitHub地址（https://github.com/hetutu5238/zmc_security_oauth2）
```
9、运行jar包，启动失败？
```text
十有八九是打的jar包有问题，参考博客重新打包，SpringBoot系列——jar包与war包的部署：https://www.cnblogs.com/huanzi-qch/p/9948060.html
```

## QQ群<br/>
有事请加群，有问题进群大家一起交流！
QQ群名：Java交流群-huanzi-qch
QQ群号：1015379123
![](https://huanzi-qch.gitee.io/file-server/images/qq.png) 
<br/>注：如果图片加载不出来请点击查看[这里](https://huanzi-qch.gitee.io/file-server/images/qq.png)

## 捐献<br/>
请注意，作者五行缺钱，如果喜欢这个项目，请随意打赏！

支付宝<br/>
![](https://huanzi-qch.gitee.io/file-server/images/zhifubao.png) 
<br/>注：如果图片加载不出来请点击查看[这里](https://huanzi-qch.gitee.io/file-server/images/zhifubao.png) 

微信<br/>
![](https://huanzi-qch.gitee.io/file-server/images/weixin.png) 
<br/>注：如果图片加载不出来请点击查看[这里](https://huanzi-qch.gitee.io/file-server/images/weixin.png) 
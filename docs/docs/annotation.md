## 自定义注解 <br/>
　　目前我们有以下几个自定义注解<br/>
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
    |   |   |                   |-- annotation  【自定义注解】
    |   |   |                   |   |-- Between.java    【SqlUtil拼接日期范围查询】
    |   |   |                   |   |-- Decrypt.java    【API加解密时判断是否需要进行解密】
    |   |   |                   |   |-- Encrypt.java    【API加解密时判断是否需要进行加密】
    |   |   |                   |   |-- In.java         【SqlUtil拼接in查询】
    |   |   |                   |   |-- Like.java       【SqlUtil拼接like查看】
    |-- 省略其他部分...
```

## API加解密<br/>

　　@Encrypt、@Decrypt注解，用于在API加解密时判断是否需要进行加、解密<br/>
```
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypt {

}

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Decrypt {

}
```
　　使用<br/>
```
    @PostMapping("resetPassword")
    @Decrypt
    @Encrypt
    public Result<SysUserVo> resetPassword(SysUserVo sysUserVo){
        return sysUserService.resetPassword(sysUserVo.getUserId());
    }
```
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

## SQL语句拼接<br/>

　　@Between、@In、@Like注解，用于SqlUtil拼接SQL语句<br/>
```
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Between {

    /**
     * 最小值的实体属性名
     */
    String min();

    /**
     * 最大值的实体属性名
     */
    String max();
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface In {

    /**
     * in的具体集合的属性名
     */
    String values();
}

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Like {

}
```
　　使用<br/>
```
/**
 * 拼接SQL工具类
 * 详情请阅读博客：https://www.cnblogs.com/huanzi-qch/p/9754846.html
 */
@Slf4j
public class SqlUtil {

    /**
     * 日期转换格式
     */
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 数据库驱动类，用于判断数据库类型
     * MySQL：com.mysql.cj.jdbc.driver（默认）
     * postgresql：org.postgresql.driver
     * Oracle：oracle.jdbc.oracledriver
     */
    @Value("${string.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private static String sqlType;

    /**
     * 根据实体、Vo直接拼接全部SQL
     * @param entityClass 实体类
     * @param entityVo    继承了PageCondition分页条件的Vo类
     * @return sql
     */
    public static StringBuilder joinSqlByEntityAndVo(Class<?> entityClass,Object entityVo){
        //select 所有字段 from table
        StringBuilder sql = SqlUtil.appendFields(entityClass);

        //拼接查询字段
        SqlUtil.appendQueryColumns(entityClass,entityVo,sql);

        //拼接排序字段
        SqlUtil.orderByColumn((PageCondition)entityVo,sql);

        return sql;
    }

    /**
     * 自动拼接原生SQL的“and”查询条件,
     * 支持自定义注解，注解改成打在vo类中，不应该破坏公用的entity实体映射类：@Like @Between @In
     *
     * @param entityClass      实体类
     * @param entityVo         继承了PageCondition分页条件的Vo类
     * @param sql              待拼接SQL
     * @param ignoreProperties 忽略属性
     */
    public static void appendQueryColumns(Class<?> entityClass, Object entityVo, StringBuilder sql, String... ignoreProperties) {
        try {

            List<String> ignoreList1 = Arrays.asList(ignoreProperties);
            //默认忽略分页参数
            List<String> ignoreList2 = Arrays.asList("class", "pageable", "page", "rows", "sidx", "sord");

            //反射获取Class的属性（Field表示类中的成员变量）
            Class<?> entityVoClass = entityVo.getClass();

            //可以直接传进来，也可以根据entityVoClass来创建entityClass，如果选择动态拼接，对命名规则有一定要求
//            Class<?> entityClass = Class.forName(entityVoClass.getName().replaceFirst("Vo",""));

            for (Field field : entityVoClass.getDeclaredFields()) {
                //获取授权
                field.setAccessible(true);
                //属性名称
                String fieldName = field.getName();
                //属性的值
                Object fieldValue = field.get(entityVo);

                //检查entity中是否也存在该字段，如果没有，直接跳过
                try {
                    entityClass.getDeclaredField(fieldName);
                }catch (NoSuchFieldException e){
                    log.debug("entity中没有这个字段，拼接查询SQL直接跳过：{}",e.getMessage());
                    continue;
                }

                String column = SqlUtil.translate(fieldName);

                //值是否为空
                if (!StringUtils.isEmpty(fieldValue)) {
                    //映射关系：对象属性(驼峰)->数据库字段(下划线)
                    if (!ignoreList1.contains(fieldName) && !ignoreList2.contains(fieldName)) {
                        //开启模糊查询
                        if (field.isAnnotationPresent(Like.class)) {
                            sql.append(" and ").append(column).append(" like '%").append(SqlUtil.escapeSql(String.valueOf(fieldValue))).append("%'");
                        }
                        //开启等值查询
                        else {
                            sql.append(" and ").append(column).append(" = '").append(SqlUtil.escapeSql(String.valueOf(fieldValue))).append("'");
                        }
                    }
                } else {
                    //开启区间查询
                    if (field.isAnnotationPresent(Between.class)) {
                        //获取最小值
                        Field minField = entityVoClass.getDeclaredField(field.getAnnotation(Between.class).min());
                        minField.setAccessible(true);
                        Object minVal = minField.get(entityVo);
                        //获取最大值
                        Field maxField = entityVoClass.getDeclaredField(field.getAnnotation(Between.class).max());
                        maxField.setAccessible(true);
                        Object maxVal = maxField.get(entityVo);
                        //开启区间查询，需要使用对应的函数
                        if ("java.util.Date".equals(field.getType().getName())) {
                            //MySQL
                            if(sqlType.toLowerCase().contains("com.mysql.cj.jdbc.driver")){
                                if (!StringUtils.isEmpty(minVal)) {
                                    sql.append(" and ").append(column).append(" > str_to_date( '").append(simpleDateFormat.format((Date) minVal)).append("','%Y-%m-%d %H:%i:%s')");
                                }
                                if (!StringUtils.isEmpty(maxVal)) {
                                    sql.append(" and ").append(column).append(" < str_to_date( '").append(simpleDateFormat.format((Date) maxVal)).append("','%Y-%m-%d %H:%i:%s')");
                                }
                            }
                            //postgresql
                            if(sqlType.toLowerCase().contains("org.postgresql.driver")){
                                if (!StringUtils.isEmpty(minVal)) {
                                    sql.append(" and ").append(column).append(" > cast('").append(simpleDateFormat.format((Date) minVal)).append("' as timestamp)");
                                }
                                if (!StringUtils.isEmpty(maxVal)) {
                                    sql.append(" and ").append(column).append(" < cast('").append(simpleDateFormat.format((Date) maxVal)).append("' as timestamp)");
                                }
                            }
                            //Oracle
                            if(sqlType.toLowerCase().contains("oracle.jdbc.oracledriver")){
                                if (!StringUtils.isEmpty(minVal)) {
                                    sql.append(" and ").append(column).append(" > to_date( '").append(simpleDateFormat.format((Date) minVal)).append("','yyyy-mm-dd hh24:mi:ss')");
                                }
                                if (!StringUtils.isEmpty(maxVal)) {
                                    sql.append(" and ").append(column).append(" < to_date( '").append(simpleDateFormat.format((Date) maxVal)).append("','yyyy-mm-dd hh24:mi:ss')");
                                }
                            }
                        }
                    }

                    //开启in查询
                    if (field.isAnnotationPresent(In.class)) {
                        //获取要in的值
                        Field values = entityVoClass.getDeclaredField(field.getAnnotation(In.class).values());
                        values.setAccessible(true);
                        List<String> valuesList = (List<String>) values.get(entityVo);
                        if (valuesList != null && valuesList.size() > 0) {
                            StringBuilder inValues = new StringBuilder(512);
                            for (int i = 0; i < valuesList.size(); i++) {
                                inValues.append("'").append(SqlUtil.escapeSql(valuesList.get(i))).append("'");
                                if(i < valuesList.size()-1){
                                    inValues.append(",");
                                }
                            }

                            sql.append(" and ").append(column).append(" in (").append(inValues).append(")");
                        }
                    }
                }
            }
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        }
    }

    /**
     *
     * @param entityClass 自动拼接实体类
     * @param ignoreProperties 动态参数  忽略拼接的字段
     * @return sql
     */
    public static StringBuilder appendFields(Class<?> entityClass, String... ignoreProperties) {
        StringBuilder sql = new StringBuilder(1024);
        List<String> ignoreList = Arrays.asList(ignoreProperties);
        sql.append("select ");

        for (Field field : entityClass.getDeclaredFields()) {
            //获取授权
            field.setAccessible(true);
            String fieldName = field.getName();//属性名称

            //非临时字段、非忽略字段
            if (!field.isAnnotationPresent(Transient.class) && !ignoreList.contains(fieldName)) {
                //拼接查询字段  驼峰属性转下划线
                sql.append(SqlUtil.translate(fieldName)).append(" ").append(",");
            }
        }
        //处理逗号（删除最后一个字符）
        sql.deleteCharAt(sql.length() - 1);

        String tableName = entityClass.getAnnotation(Table.class).name();
        sql.append("from ").append(tableName).append(" where '1' = '1' ");
        return sql;
    }

    /**
     * 拼接排序SQL
     *
     * @param pageCondition 继承了PageCondition分页条件的Vo类
     * @param sql    待拼接的SQL
     */
    public static void orderByColumn(PageCondition pageCondition, StringBuilder sql) {
        String sidx = pageCondition.getSidx();
        String sord = pageCondition.getSord();

        if (!StringUtils.isEmpty(sidx)) {
            //1.获取Bean
            BeanWrapper srcBean = new BeanWrapperImpl(pageCondition);
            //2.获取Bean的属性描述
            PropertyDescriptor[] pds = srcBean.getPropertyDescriptors();
            //3.获取符合的排序字段名
            for (PropertyDescriptor p : pds) {
                String propertyName = p.getName();
                if (sidx.equals(propertyName)) {
                    sql.append(" order by ").append(translate(sidx)).append("desc".equalsIgnoreCase(sord) ? " desc" : " asc");
                }
            }
        }
    }

    /**
     * 实体属性转表字段，驼峰属性转下划线，并全部转小写
     */
    private static String translate(String fieldName){
        return new PropertyNamingStrategy.SnakeCaseStrategy().translate(fieldName).toLowerCase();
    }

    /**
     * sql转义
     * 动态拼写SQL，需要进行转义防范SQL注入！
     */
    private static String escapeSql(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char src = str.charAt(i);
            switch (src) {
                case '\'':
                    sb.append("''");// hibernate转义多个单引号必须用两个单引号
                    break;
                case '\"':
                case '\\':
                    sb.append('\\');
                default:
                    sb.append(src);
                    break;
            }
        }
        return sb.toString();
    }
}
```
　　SQL拼接效果<br/>
```
@Data
public class SysUserVo extends PageCondition implements Serializable {

    @In(values = "userIdList")//in查询
    private String userId;//用户id

    @Like
    private String loginName;//登录名

    private String userName;//用户名称

    private String password;//登录密码

    private String valid;//软删除标识，Y/N

    private String limitedIp;//限制允许登录的IP集合

    private Date expiredTime;//账号失效时间，超过时间将不能登录系统

    private Date lastChangePwdTime;//最近修改密码时间，超出时间间隔，提示用户修改密码

    private String limitMultiLogin;//是否允许账号同一个时刻多人在线，Y/N

    @Between(min = "createTimeStart", max = "createTimeEnd")//开启区间查询
    private Date createTime;//创建时间

    private Date updateTime;//修改时间

    private String oldPassword;//修改密码时输入的旧密码

    private String newPassword;//修改密码时输入的新密码

    private Date createTimeStart;
    private Date createTimeEnd;
    private List<String> userIdList;
}


public static void main(String[] args) {
    SqlUtil.sqlType = "com.mysql.cj.jdbc.Driver";
    SysUserVo sysUserVo = new SysUserVo();
    //like查询
    sysUserVo.setLoginName("张三");

    //日期区域查询
    sysUserVo.setCreateTimeStart(new Date());
    sysUserVo.setCreateTimeEnd(new Date());

    //in查询
    ArrayList<String> userIds = new ArrayList<>();
    userIds.add("1");
    userIds.add("2");
    sysUserVo.setUserIdList(userIds);

    //排序  asc desc
    sysUserVo.setSidx("createTime");
    sysUserVo.setSord("desc");

    //根据实体、Vo直接拼接全部SQL
    StringBuilder sql = SqlUtil.joinSqlByEntityAndVo(SysUser.class,sysUserVo);
    System.out.println(sql.toString());
}
```
![](https://img2020.cnblogs.com/blog/1353055/202010/1353055-20201010151101874-554903797.png)

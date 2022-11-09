package cn.huanzi.qch.baseadmin.autogenerator;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成工具 V2.0
 * 详情请阅读博客：https://www.cnblogs.com/huanzi-qch/p/14927738.html
 */
public class AutoGeneratorPlus {

    /**
     * 程序自动设置
     */
    private String tableName;//表名
    private String tableComment;//表注释
    private String filePath;//最终文件生成位置

    /**
     * 数据连接相关，需要手动设置
     */
    private static final String URL = "jdbc:mysql://localhost:3306/base_admin?serverTimezone=GMT%2B8&characterEncoding=utf-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String DRIVER_CLASSNAME = "com.mysql.cj.jdbc.Driver";

    /**
     * 基础路径，需要手动设置
     */
    private String tlfPath = System.getProperty("user.dir") + "\\src\\main\\resources\\tlf\\";//模板文件位置
    private String basePackage = "cn\\huanzi\\qch\\baseadmin\\";//根包位置
    private String filePackage = basePackage + "sys\\";//文件所在包位置

    /**
     * 构造参数，设置表名
     */
    private AutoGeneratorPlus(String tableName) {
        //设置表名
        this.tableName = tableName;

        //文件所在包位置
        filePackage = filePackage + StringUtil.camelCaseName(tableName).toLowerCase() + "\\";

        //拼接完整最终位置 System.getProperty("user.dir") 获取的是项目所在路径，如果我们是子项目，则需要添加一层路径
        filePath = System.getProperty("user.dir") + "\\src\\main\\java\\" + filePackage;
    }

    /**
     * 读取模板，设置内容，生成文件
     * @param templatePath 模板文件路径
     * @param outputFile 文件生成路径
     * @param tableInfos 表字段信息
     * @param customParameter 自定义参数
     */
    private void writer(String templatePath, String outputFile,List<TableInfo> tableInfos,Map<String,String> customParameter){
        //主键
        TableInfo prikey = new TableInfo();

        //for循环标识
        boolean forFlag = false;
        StringBuilder forContent = new StringBuilder(1024);

        //驼峰标识映射后的表名
        String replacement = StringUtil.captureName(StringUtil.camelCaseName(tableName));

        //遍历属性
        for (TableInfo tableInfo : tableInfos) {
            //主键
            if ("PRI".equals(tableInfo.getColumnKey())) {
                prikey = tableInfo;
                break;
            }
        }

        try(FileReader fileReader = new FileReader(templatePath);
            BufferedReader reader = new BufferedReader(fileReader)) {
            //生成文件
            File file = FileUtil.createFile(outputFile);
            StringBuilder stringBuilder = new StringBuilder(1024);

            //读取模板文件，拼接文件内容
            Object[] lines = reader.lines().toArray();
            for (Object o : lines) {
                String line = String.valueOf(o);

                /* 设置值 */

                //${tableName} 表名称，例如：tb_user
                if(line.contains("${tableName}")){
                    line = line.replaceAll("\\$\\{tableName}", tableName);
                }

                //${tableComment} 表注释，例如：tb_user
                if(line.contains("${tableComment}")){
                    line = line.replaceAll("\\$\\{tableComment}", tableComment);
                }

                //${entity} 实体类名称，例如：TbUser
                if(line.contains("${entity}")){
                    line = line.replaceAll("\\$\\{entity}", replacement);
                }

                //${entityFirstToLowerCase} 实体类名称首字母小写，例如：tbUser
                if(line.contains("${entityFirstToLowerCase}")){
                    line = line.replaceAll("\\$\\{entityFirstToLowerCase}", StringUtil.camelCaseName(tableName));
                }

                //${entityToLowerCase} 实体类名称全小写，例如：tbuser
                if(line.contains("${entityToLowerCase}")){
                    line = line.replaceAll("\\$\\{entityToLowerCase}", replacement.toLowerCase());
                }

                //${priDataType} 实体类主键类型，例如：String
                if(line.contains("${priDataType}")){
                    line = line.replaceAll("\\$\\{priDataType}", StringUtil.typeMapping(prikey.getDataType()));
                }

                //处理自定义参数
                line = customParameter(line,customParameter);

                //先取得循环体的内容
                if(forFlag){
                    forContent.append(line).append("\n");
                }

                //是否为for循环遍历表字段
                if(line.contains("#for")){
                    forFlag = true;
                }
                if(line.contains("#end")){
                    forFlag = false;
                    line = line.replaceAll("#end", "");
                }

                //遍历循环体的内容，并设置值
                if(!forFlag && forContent.length() > 0){
                    //遍历表字段
                    for (TableInfo tableInfo : tableInfos) {
                        String tableColumns = forContent.toString()
                                //表字段信息：类型、名称、注释
                                .replaceAll("\\$\\{tableInfo.dataType}", StringUtil.typeMapping(tableInfo.getDataType()))
                                .replaceAll("\\$\\{tableInfo.columnName}", StringUtil.camelCaseName(tableInfo.getColumnName()))
                                .replaceAll("\\$\\{tableInfo.columnComment}", tableInfo.getColumnComment());

                        //清除多余#end，以及换行符
                        tableColumns = tableColumns.replaceAll("#end", "").replaceAll("\n", "");

                        //设置是否主键、是否自增
                        String pri = "",autoIncrement="";
                        //主键
                        if ("PRI".equals(tableInfo.getColumnKey())) {
                            pri = " @Id\n";
                            //自增id
                            if ("auto_increment".equals(tableInfo.getExtra())){
                                autoIncrement = "@GeneratedValue(strategy= GenerationType.IDENTITY)\n";
                            }
                        }
                        tableColumns = tableColumns
                                .replaceAll("#ifPri", pri)
                                .replaceAll("#ifAutoIncrement", autoIncrement);

                        //处理自定义参数
                        tableColumns = customParameter(tableColumns,customParameter);

                        //前补tab，后补换行符
                        stringBuilder.append("    ").append(tableColumns.trim()).append("\n\n");
                    }
                    //置空
                    forContent.setLength(0);
                }

                if(!forFlag){
                    stringBuilder.append(line).append("\n");
                }
            }

            //写入数据到到文件中
            FileUtil.fileWriter(file, stringBuilder);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void writer(String templatePath, String outputFile,List<TableInfo> tableInfos){
        writer(templatePath,outputFile,tableInfos,new HashMap<>(0));
    }

    /**
     * 处理自定义参数
     */
    private String customParameter(String str,Map<String,String> customParameter){
        for (String key : customParameter.keySet()) {
            str = str.replaceAll("\\$\\{"+key+"}",customParameter.get(key));
        }
        return str;
    }

    /**
     * file工具类
     */
    private static class FileUtil {
        /**
         * 创建文件
         *
         * @param pathNameAndFileName 路径跟文件名
         * @return File对象
         */
        private static File createFile(String pathNameAndFileName) {
            File file = new File(pathNameAndFileName);
            try {
                //获取父目录
                File fileParent = file.getParentFile();
                if (!fileParent.exists()) {
                    fileParent.mkdirs();
                }
                //创建文件
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (Exception e) {
                file = null;
                System.err.println("新建文件操作出错");
                e.printStackTrace();
            }
            return file;
        }

        /**
         * 字符流写入文件
         *
         * @param file         file对象
         * @param stringBuilder 要写入的数据
         */
        private static void fileWriter(File file, StringBuilder stringBuilder) {
            //字符流
            try {
                FileWriter resultFile = new FileWriter(file, false);//true,则追加写入 false,则覆盖写入
                PrintWriter myFile = new PrintWriter(resultFile);
                //写入
                myFile.println(stringBuilder.toString());

                myFile.close();
                resultFile.close();
            } catch (Exception e) {
                System.err.println("写入操作出错");
                e.printStackTrace();
            }
        }
    }

    /**
     * 字符串处理工具类
     */
    private static class StringUtil {
        /**
         * 数据库类型->JAVA类型
         *
         * @param dbType 数据库类型
         * @return JAVA类型
         */
        private static String typeMapping(String dbType) {
            String javaType;
            if ("int|integer".contains(dbType)) {
                javaType = "Integer";
            } else if ("float|double|decimal|real".contains(dbType)) {
                javaType = "Double";
            } else if ("date|time|datetime|timestamp".contains(dbType)) {
                javaType = "Date";
            } else {
                javaType = "String";
            }
            return javaType;
        }

        /**
         * 驼峰转换为下划线
         */
        private static String underscoreName(String camelCaseName) {
            StringBuilder result = new StringBuilder(1024);
            if (camelCaseName != null && camelCaseName.length() > 0) {
                result.append(camelCaseName.substring(0, 1).toLowerCase());
                for (int i = 1; i < camelCaseName.length(); i++) {
                    char ch = camelCaseName.charAt(i);
                    if (Character.isUpperCase(ch)) {
                        result.append("_");
                        result.append(Character.toLowerCase(ch));
                    } else {
                        result.append(ch);
                    }
                }
            }
            return result.toString();
        }

        /**
         * 首字母大写
         */
        private static String captureName(String name) {
            char[] cs = name.toCharArray();
            cs[0] -= 32;
            return String.valueOf(cs);

        }

        /**
         * 下划线转换为驼峰
         */
        private static String camelCaseName(String underscoreName) {
            StringBuilder result = new StringBuilder(1024);
            if (underscoreName != null && underscoreName.length() > 0) {
                boolean flag = false;
                for (int i = 0; i < underscoreName.length(); i++) {
                    char ch = underscoreName.charAt(i);
                    if ("_".charAt(0) == ch) {
                        flag = true;
                    } else {
                        if (flag) {
                            result.append(Character.toUpperCase(ch));
                            flag = false;
                        } else {
                            result.append(ch);
                        }
                    }
                }
            }
            return result.toString();
        }
    }

    /**
     * JDBC连接数据库工具类
     */
    private static class DBConnectionUtil {

        static {
            // 1、加载驱动
            try {
                Class.forName(DRIVER_CLASSNAME);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /**
         * 返回一个Connection连接
         */
        static Connection getConnection() {
            Connection conn = null;
            // 2、连接数据库
            try {
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return conn;
        }

        /**
         * 关闭Connection，Statement连接
         */
        public static void close(Connection conn, Statement stmt) {
            try {
                conn.close();
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        /**
         * 关闭Connection，Statement，ResultSet连接
         */
        public static void close(Connection conn, Statement stmt, ResultSet rs) {
            try {
                close(conn, stmt);
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 表结构信息实体类
     */
    private class TableInfo {
        private String columnName;//字段名
        private String dataType;//字段类型
        private String columnComment;//字段注释
        private String columnKey;//主键
        private String extra;//主键类型

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getColumnComment() {
            return columnComment;
        }

        public void setColumnComment(String columnComment) {
            this.columnComment = columnComment;
        }

        public String getColumnKey() {
            return columnKey;
        }

        public void setColumnKey(String columnKey) {
            this.columnKey = columnKey;
        }

        public String getExtra() {
            return extra;
        }

        public void setExtra(String extra) {
            this.extra = extra;
        }
    }

    /**
     * 获取表结构信息
     * 目前仅支持mysql
     */
    private List<TableInfo> getTableInfo() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<TableInfo> list = new ArrayList<>();
        try {
            conn = DBConnectionUtil.getConnection();

            //表字段信息
            String sql = "select column_name,data_type,column_comment,column_key,extra from information_schema.columns where table_schema = (select database()) and table_name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, tableName);
            rs = ps.executeQuery();
            while (rs.next()) {
                TableInfo tableInfo = new TableInfo();
                //列名，全部转为小写
                tableInfo.setColumnName(rs.getString("column_name").toLowerCase());
                //列类型
                tableInfo.setDataType(rs.getString("data_type"));
                //列注释
                tableInfo.setColumnComment(rs.getString("column_comment"));
                //主键
                tableInfo.setColumnKey(rs.getString("column_key"));
                //主键类型
                tableInfo.setExtra(rs.getString("extra"));
                list.add(tableInfo);
            }

            //表注释
            sql = "select table_comment from information_schema.tables where table_schema = (select database()) and table_name=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, tableName);
            rs = ps.executeQuery();
            while (rs.next()) {
                //表注释
                tableComment = rs.getString("table_comment");
            }
        } catch (SQLException e) {

            e.printStackTrace();
        } finally {
            if(rs != null){
                DBConnectionUtil.close(conn, ps, rs);
            }
        }
        return list;
    }

    /**
     * 快速创建，供外部调用，调用之前先设置一下项目的基础路径
     */
    private String create() {
        System.out.println("生成路径位置：" + filePath);

        //获取表信息
        List<TableInfo> tableInfo = getTableInfo();

        //驼峰标识映射后的表名
        String captureName = StringUtil.captureName(StringUtil.camelCaseName(tableName));

        //自定义参数
        HashMap<String, String> customParameter = new HashMap<>(2);
        customParameter.put("author","作者：Auto Generator By 'huanzi-qch'");
        customParameter.put("date","生成日期："+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        //读取模板、生成代码
        writer(tlfPath+"controller.tlf",
                filePath + "controller\\" + captureName + "Controller.java",
                tableInfo,customParameter);
        writer(tlfPath+"entity.tlf",
                filePath + "pojo\\" + captureName + ".java",
                tableInfo,customParameter);
        writer(tlfPath+"entityvo.tlf",
                filePath + "vo\\" + captureName + "Vo.java",
                tableInfo,customParameter);
        writer(tlfPath+"repository.tlf",
                filePath + "repository\\" + captureName + "Repository.java",
                tableInfo,customParameter);
        writer(tlfPath+"service.tlf",
                filePath + "service\\" + captureName + "Service.java",
                tableInfo,customParameter);
        writer(tlfPath+"serviceimpl.tlf",
                filePath + "service\\" + captureName + "ServiceImpl.java",
                tableInfo,customParameter);

        return tableName + " 后台代码生成完毕！";
    }

//    public static void main(String[] args) {
//        String[] tables = {"sys_user","sys_menu","sys_authority","sys_user_menu","sys_user_authority","sys_shortcut_menu","sys_setting","sys_file"};
//        for (String table : tables) {
//            String msg = new AutoGeneratorPlus(table).create();
//            System.out.println(msg);
//        }
//    }
}

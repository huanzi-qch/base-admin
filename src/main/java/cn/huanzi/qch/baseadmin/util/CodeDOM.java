package cn.huanzi.qch.baseadmin.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动生成代码
 */
@Slf4j
public class CodeDOM {

    /**
     * 构造参数，出入表名
     */
    private CodeDOM(String tableName) {
        this.tableName = tableName;
        basePackage_ = "cn\\huanzi\\qch\\baseadmin\\sys\\";
        package_ = basePackage_ + StringUtil.camelCaseName(tableName).toLowerCase() + "\\";
        //System.getProperty("user.dir") 获取的是项目所在路径，如果我们是子项目，则需要添加一层路径
        basePath = System.getProperty("user.dir") + "\\src\\main\\java\\" + package_;
        basePackage_ = "cn\\huanzi\\qch\\baseadmin\\";
    }

    /**
     * 数据连接相关
     */
    private static final String URL = "jdbc:mysql://localhost:3306/test?serverTimezone=GMT%2B8&characterEncoding=utf-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String DRIVER_CLASSNAME = "com.mysql.jdbc.Driver";
    /**
     * 表名
     */
    private String tableName;

    /**
     * 基础路径
     */
    private String basePackage_;
    private String package_;
    private String basePath;

    /**
     * 创建pojo实体类
     */
    private void createPojo(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(basePath + "pojo\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ".java");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "pojo;\n" +
                        "\n" +
                        "import lombok.Data;\n" +
                        "import javax.persistence.*;\n" +
                        "import java.io.Serializable;\n" +
                        "import java.util.Date;\n" +
                        "\n" +
                        "@Entity\n" +
                        "@Table(name = \"" + tableName + "\")\n" +
                        "@Data\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + " implements Serializable {\n"
        );
        //遍历设置属性
        for (TableInfo tableInfo : tableInfos) {
            //主键
            if ("PRI".equals(tableInfo.getColumnKey())) {
                stringBuffer.append("    @Id\n");
            }
            //自增
            if ("auto_increment".equals(tableInfo.getExtra())) {
                stringBuffer.append("    @GeneratedValue(strategy= GenerationType.IDENTITY)\n");
            }
            stringBuffer.append("    private " + StringUtil.typeMapping(tableInfo.getDataType()) + " " + StringUtil.camelCaseName(tableInfo.getColumnName()) + ";//" + tableInfo.getColumnComment() + "\n\n");
        }
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);
    }

    /**
     * 创建vo类
     */
    private void createVo(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(basePath + "vo\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo.java");
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "vo;\n" +
                        "\n" +
                        "import "+ basePackage_.replaceAll("\\\\", ".") +" common.pojo.PageCondition;"+
                        "import lombok.Data;\n" +
                        "import java.io.Serializable;\n" +
                        "import java.util.Date;\n" +
                        "\n" +
                        "@Data\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo extends PageCondition implements Serializable {\n"
        );
        //遍历设置属性
        for (TableInfo tableInfo : tableInfos) {
            stringBuffer.append("    private " + StringUtil.typeMapping(tableInfo.getDataType()) + " " + StringUtil.camelCaseName(tableInfo.getColumnName()) + ";//" + tableInfo.getColumnComment() + "\n\n");
        }
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);
    }

    /**
     * 创建repository类
     */
    private void createRepository(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(basePath + "repository\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository.java");
        StringBuffer stringBuffer = new StringBuffer();
        String t = "String";
        //遍历属性
        for (TableInfo tableInfo : tableInfos) {
            //主键
            if ("PRI".equals(tableInfo.getColumnKey())) {
                t = StringUtil.typeMapping(tableInfo.getDataType());
            }
        }
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "repository;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.repository.*;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import org.springframework.stereotype.Repository;\n" +
                        "\n" +
                        "@Repository\n" +
                        "public interface " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository extends CommonRepository<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuffer.append("\n");
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);
    }

    /**
     * 创建service类
     */
    private void createService(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(basePath + "service\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service.java");
        StringBuffer stringBuffer = new StringBuffer();
        String t = "String";
        //遍历属性
        for (TableInfo tableInfo : tableInfos) {
            //主键
            if ("PRI".equals(tableInfo.getColumnKey())) {
                t = StringUtil.typeMapping(tableInfo.getDataType());
            }
        }
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "service;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.service.*;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "\n" +
                        "public interface " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service extends CommonService<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo, " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuffer.append("\n");
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);

        //Impl
        File file1 = FileUtil.createFile(basePath + "service\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "ServiceImpl.java");
        StringBuffer stringBuffer1 = new StringBuffer();
        stringBuffer1.append(
                "package " + package_.replaceAll("\\\\", ".") + "service;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.service.*;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "repository." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository;\n" +
                        "import org.springframework.beans.factory.annotation.Autowired;\n" +
                        "import org.springframework.stereotype.Service;\n" +
                        "import org.springframework.transaction.annotation.Transactional;\n" +
                        "import javax.persistence.EntityManager;\n" +
                        "import javax.persistence.PersistenceContext;\n" +
                        "\n" +
                        "@Service\n" +
                        "@Transactional\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "ServiceImpl extends CommonServiceImpl<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo, " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> implements " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service{"
        );
        stringBuffer1.append("\n\n");
        stringBuffer1.append(
                "    @PersistenceContext\n" +
                        "    private EntityManager em;\n");

        stringBuffer1.append("" +
                "    @Autowired\n" +
                "    private " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository " + StringUtil.camelCaseName(tableName) + "Repository;\n");
        stringBuffer1.append("}");
        FileUtil.fileWriter(file1, stringBuffer1);
    }

    /**
     * 创建controller类
     */
    private void createController(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(basePath + "controller\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Controller.java");
        StringBuffer stringBuffer = new StringBuffer();
        String t = "String";
        //遍历属性
        for (TableInfo tableInfo : tableInfos) {
            //主键
            if ("PRI".equals(tableInfo.getColumnKey())) {
                t = StringUtil.typeMapping(tableInfo.getDataType());
            }
        }
        stringBuffer.append(
                "package " + package_.replaceAll("\\\\", ".") + "controller;\n" +
                        "\n" +
                        "import " + basePackage_.replaceAll("\\\\", ".") + "common.controller.*;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "import " + package_.replaceAll("\\\\", ".") + "service." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service;\n" +
                        "import org.springframework.beans.factory.annotation.Autowired;\n" +
                        "import org.springframework.web.bind.annotation.*;\n" +
                        "\n" +
                        "@RestController\n" +
                        "@RequestMapping(\"/sys/" + StringUtil.camelCaseName(tableName) + "/\")\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Controller extends CommonController<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo, " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuffer.append("\n");
        stringBuffer.append("" +
                "    @Autowired\n" +
                "    private " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service " + StringUtil.camelCaseName(tableName) + "Service;\n");
        stringBuffer.append("}");
        FileUtil.fileWriter(file, stringBuffer);
    }

    /**
     * 获取表结构信息
     */
    private List<TableInfo> getTableInfo() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ArrayList<TableInfo> list = new ArrayList<>();
        try {
            conn = DBConnectionUtil.getConnection();
            String sql = "select column_name,data_type,column_comment,column_key,extra from information_schema.columns where table_name=?";
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
        } catch (SQLException e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        } finally {
            assert rs != null;
            DBConnectionUtil.close(conn, ps, rs);
        }
        return list;
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
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
            return file;
        }

        /**
         * 字符流写入文件
         *
         * @param file         file对象
         * @param stringBuffer 要写入的数据
         */
        private static void fileWriter(File file, StringBuffer stringBuffer) {
            //字符流
            try {
                FileWriter resultFile = new FileWriter(file, false);//true,则追加写入 false,则覆盖写入
                PrintWriter myFile = new PrintWriter(resultFile);
                //写入
                myFile.println(stringBuffer.toString());

                myFile.close();
                resultFile.close();
            } catch (Exception e) {
                System.err.println("写入操作出错");
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
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
        public static String underscoreName(String camelCaseName) {
            StringBuilder result = new StringBuilder();
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
        static String captureName(String name) {
            char[] cs = name.toCharArray();
            cs[0] -= 32;
            return String.valueOf(cs);

        }

        /**
         * 下划线转换为驼峰
         */
        static String camelCaseName(String underscoreName) {
            StringBuilder result = new StringBuilder();
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
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
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
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
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
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
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
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }

    }

    /**
     * 表结构行信息实体类
     */
    private class TableInfo {
        private String columnName;
        private String dataType;
        private String columnComment;
        private String columnKey;
        private String extra;

        TableInfo() {
        }

        String getColumnName() {
            return columnName;
        }

        void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        String getDataType() {
            return dataType;
        }

        void setDataType(String dataType) {
            this.dataType = dataType;
        }

        String getColumnComment() {
            return columnComment;
        }

        void setColumnComment(String columnComment) {
            this.columnComment = columnComment;
        }

        String getColumnKey() {
            return columnKey;
        }

        void setColumnKey(String columnKey) {
            this.columnKey = columnKey;
        }

        String getExtra() {
            return extra;
        }

        void setExtra(String extra) {
            this.extra = extra;
        }
    }

    /**
     * 快速创建，供外部调用，调用之前先设置一下项目的基础路径
     */
    private String create() {
        List<TableInfo> tableInfo = getTableInfo();
        createPojo(tableInfo);
        createVo(tableInfo);
        createRepository(tableInfo);
        createService(tableInfo);
        createController(tableInfo);
        System.out.println("生成路径位置：" + basePath);
        return tableName + " 后台代码生成完毕！";
    }

//    public static void main(String[] args) {
//        String[] tables = {"sys_user","sys_menu","sys_authority","sys_user_menu","sys_user_authority","sys_shortcut_menu","sys_setting"};
//        for (String table : tables) {
//            String msg = new CodeDOM(table).create();
//            System.out.println(msg);
//        }
//    }
}

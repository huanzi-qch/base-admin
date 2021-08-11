package cn.huanzi.qch.baseadmin.autogenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 代码生成工具 V1.0
 * 详情请阅读博客：https://www.cnblogs.com/huanzi-qch/p/14927738.html
 */
public class AutoGenerator {

    /**
     * 程序自动设置
     */
    private String tableName;//表名
    private String tableComment;//表注释
    private String filePath;//最终文件生成位置

    /**
     * 数据连接相关，需要手动设置
     */
    private static final String URL = "jdbc:mysql://localhost:3306/test?serverTimezone=GMT%2B8&characterEncoding=utf-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";
    private static final String DRIVER_CLASSNAME = "com.mysql.cj.jdbc.Driver";

    /**
     * 基础路径，需要手动设置
     */
    private String basePackage = "cn\\huanzi\\qch\\baseadmin\\";//根包位置
    private String filePackage = basePackage + "sys\\";//文件所在包位置

    /**
     * 构造参数，设置表名
     */
    private AutoGenerator(String tableName) {
        //设置表名
        this.tableName = tableName;

        //文件所在包位置
        filePackage = filePackage + StringUtil.camelCaseName(tableName).toLowerCase() + "\\";

        //拼接完整最终位置 System.getProperty("user.dir") 获取的是项目所在路径，如果我们是子项目，则需要添加一层路径
        filePath = System.getProperty("user.dir") + "\\src\\main\\java\\" + filePackage;
    }

    /**
     * 创建pojo实体类
     */
    private void createPojo(List<TableInfo> tableInfos) {
        //创建文件
        File file = FileUtil.createFile(filePath + "pojo\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ".java");

        //拼接文件内容
        StringBuilder stringBuilder = new StringBuilder(1024);
        stringBuilder.append(
                "package " + filePackage.replaceAll("\\\\", ".") + "pojo;\n" +
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
                stringBuilder.append("    @Id\n");
            }
            //自增
            if ("auto_increment".equals(tableInfo.getExtra())) {
                stringBuilder.append("    @GeneratedValue(strategy= GenerationType.IDENTITY)\n");
            }
            stringBuilder.append("    private ").append(StringUtil.typeMapping(tableInfo.getDataType())).append(" ").append(StringUtil.camelCaseName(tableInfo.getColumnName())).append(";//").append(tableInfo.getColumnComment()).append("\n\n");
        }
        stringBuilder.append("}");

        //写入文件内容
        FileUtil.fileWriter(file, stringBuilder);
    }

    /**
     * 创建vo类
     */
    private void createVo(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(filePath + "vo\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo.java");
        StringBuilder stringBuilder = new StringBuilder(1024);
        stringBuilder.append(
                "package " + filePackage.replaceAll("\\\\", ".") + "vo;\n" +
                        "\n" +
                        "import "+ basePackage.replaceAll("\\\\", ".") +" common.pojo.PageCondition;"+
                        "import lombok.Data;\n" +
                        "import java.io.Serializable;\n" +
                        "import java.util.Date;\n" +
                        "\n" +
                        "@Data\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo extends PageCondition implements Serializable {\n"
        );
        //遍历设置属性
        for (TableInfo tableInfo : tableInfos) {
            stringBuilder.append("    private ").append(StringUtil.typeMapping(tableInfo.getDataType())).append(" ").append(StringUtil.camelCaseName(tableInfo.getColumnName())).append(";//").append(tableInfo.getColumnComment()).append("\n\n");
        }
        stringBuilder.append("}");
        FileUtil.fileWriter(file, stringBuilder);
    }

    /**
     * 创建repository类
     */
    private void createRepository(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(filePath + "repository\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository.java");
        StringBuilder stringBuilder = new StringBuilder(1024);
        String t = "String";
        //遍历属性
        for (TableInfo tableInfo : tableInfos) {
            //主键
            if ("PRI".equals(tableInfo.getColumnKey())) {
                t = StringUtil.typeMapping(tableInfo.getDataType());
            }
        }
        stringBuilder.append(
                "package " + filePackage.replaceAll("\\\\", ".") + "repository;\n" +
                        "\n" +
                        "import " + basePackage.replaceAll("\\\\", ".") + "common.repository.*;\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import org.springframework.stereotype.Repository;\n" +
                        "\n" +
                        "@Repository\n" +
                        "public interface " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository extends CommonRepository<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuilder.append("\n");
        stringBuilder.append("}");
        FileUtil.fileWriter(file, stringBuilder);
    }

    /**
     * 创建service类
     */
    private void createService(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(filePath + "service\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service.java");
        StringBuilder stringBuilder = new StringBuilder(1024);
        String t = "String";
        //遍历属性
        for (TableInfo tableInfo : tableInfos) {
            //主键
            if ("PRI".equals(tableInfo.getColumnKey())) {
                t = StringUtil.typeMapping(tableInfo.getDataType());
            }
        }
        stringBuilder.append(
                "package " + filePackage.replaceAll("\\\\", ".") + "service;\n" +
                        "\n" +
                        "import " + basePackage.replaceAll("\\\\", ".") + "common.service.*;\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "\n" +
                        "public interface " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service extends CommonService<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo, " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuilder.append("\n");
        stringBuilder.append("}");
        FileUtil.fileWriter(file, stringBuilder);

        //Impl
        File file1 = FileUtil.createFile(filePath + "service\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "ServiceImpl.java");
        StringBuilder stringBuilder1 = new StringBuilder(1024);
        stringBuilder1.append(
                "package " + filePackage.replaceAll("\\\\", ".") + "service;\n" +
                        "\n" +
                        "import " + basePackage.replaceAll("\\\\", ".") + "common.service.*;\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "repository." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository;\n" +
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
        stringBuilder1.append("\n\n");
        stringBuilder1.append(
                "    @PersistenceContext\n" +
                        "    private EntityManager em;\n");

        stringBuilder1.append("" +
                "    @Autowired\n" +
                "    private " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Repository " + StringUtil.camelCaseName(tableName) + "Repository;\n");
        stringBuilder1.append("}");
        FileUtil.fileWriter(file1, stringBuilder1);
    }

    /**
     * 创建controller类
     */
    private void createController(List<TableInfo> tableInfos) {
        File file = FileUtil.createFile(filePath + "controller\\" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Controller.java");
        StringBuilder stringBuilder = new StringBuilder(1024);
        String t = "String";
        //遍历属性
        for (TableInfo tableInfo : tableInfos) {
            //主键
            if ("PRI".equals(tableInfo.getColumnKey())) {
                t = StringUtil.typeMapping(tableInfo.getDataType());
            }
        }
        stringBuilder.append(
                "package " + filePackage.replaceAll("\\\\", ".") + "controller;\n" +
                        "\n" +
                        "import " + basePackage.replaceAll("\\\\", ".") + "common.controller.*;\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "pojo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ";\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "vo." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo;\n" +
                        "import " + filePackage.replaceAll("\\\\", ".") + "service." + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service;\n" +
                        "import org.springframework.beans.factory.annotation.Autowired;\n" +
                        "import org.springframework.web.bind.annotation.*;\n" +
                        "\n" +
                        "@RestController\n" +
                        "@RequestMapping(\"/sys/" + StringUtil.camelCaseName(tableName) + "/\")\n" +
                        "public class " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Controller extends CommonController<" + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Vo, " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + ", " + t + "> {"
        );
        stringBuilder.append("\n");
        stringBuilder.append("" +
                "    @Autowired\n" +
                "    private " + StringUtil.captureName(StringUtil.camelCaseName(tableName)) + "Service " + StringUtil.camelCaseName(tableName) + "Service;\n");
        stringBuilder.append("}");
        FileUtil.fileWriter(file, stringBuilder);
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

        //开始生成代码
        createPojo(tableInfo);
        createVo(tableInfo);
        createRepository(tableInfo);
        createService(tableInfo);
        createController(tableInfo);

        return tableName + " 后台代码生成完毕！";
    }

//    public static void main(String[] args) {
////        String[] tables = {"sys_user","sys_menu","sys_authority","sys_user_menu","sys_user_authority","sys_shortcut_menu","sys_setting"};
//        String[] tables = {"tb_user"};
//        for (String table : tables) {
//            String msg = new AutoGenerator(table).create();
//            System.out.println(msg);
//        }
//    }
}

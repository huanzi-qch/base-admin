package cn.huanzi.qch.baseadmin.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * httpServletResponse工具类
 */
public class HttpServletResponseUtil {

    /**
     * PrintWriter输出
     */
    public static void printJson(HttpServletResponse response, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter out = response.getWriter();
        out.print(msg);
        out.flush();
        out.close();
        response.flushBuffer();
    }
    /**
     * PrintWriter输出
     */
    public static void printHtml(HttpServletResponse response, String msg) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print(msg);
        out.flush();
        out.close();
        response.flushBuffer();
    }
}

package cn.huanzi.qch.baseadmin.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    /**
     * 获取当前时间
     */
    public static Date getNowDate(){
        return Calendar.getInstance().getTime();
    }

    /**
     * 获取当前时间 - N个年
     */
    public static Date getNowDateMinusYear(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR,- n);
        return cal.getTime();
    }

    /**
     * 获取当前时间 + N个年
     */
    public static Date getNowDateAddYear(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR,+ n);
        return cal.getTime();
    }

    /**
     * 获取当前时间 - N个月
     */
    public static Date getNowDateMinusMonth(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH,- n);
        return cal.getTime();
    }

    /**
     * 获取当前时间 + N个月
     */
    public static Date getNowDateAddMonth(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH,+ n);
        return cal.getTime();
    }

    /**
     * 获取当前时间 - N个周
     */
    public static Date getNowDateMinusWeek(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR,- n);
        return cal.getTime();
    }

    /**
     * 获取当前时间 + N个周
     */
    public static Date getNowDateAddWeek(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR,+ n);
        return cal.getTime();
    }

    /**
     * 获取当前时间 - N个天
     */
    public static Date getNowDateMinusDay(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,- n);
        return cal.getTime();
    }

    /**
     * 获取当前时间 + N个天
     */
    public static Date getNowDateAddDay(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,+ n);
        return cal.getTime();
    }

    /**
     * 判断该日期是否是周一
     */
    public static boolean isFirstDayOfWeek(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        //java中，一周中的第一天是周日，但我们生活中一周中的第一天指的是周一
        return cal.get(Calendar.DAY_OF_WEEK) == 2;
    }

    /**
     * 判断该日期是否是该月的第一天
     */
    public static boolean isFirstDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH) == 1;
    }

    /**
     * 判断该日期是否是该年的第一天
     */
    public static boolean isFirstDayOfYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_YEAR) == 1;
    }

    /**
     * 判断该日期是否是季度的第一天
     */
    public static boolean isFirstDayOfQuarter(Date date) {
        String mMdd = new SimpleDateFormat("MMdd").format(date);
        return "0101".equals(mMdd) || "0401".equals(mMdd) || "0701".equals(mMdd) || "1001".equals(mMdd);
    }
}

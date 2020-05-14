package cn.huanzi.qch.baseadmin.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    //获取当前时间
    public static Date getNowDate(){
        //处理时间，默认查询半年之内的工单就可以了
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }

    //获取当前时间 - N个年
    public static Date getNowDateMinusYear(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR,- n);
        return cal.getTime();
    }

    //获取当前时间 + N个年
    public static Date getNowDateAddYear(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR,+ n);
        return cal.getTime();
    }

    //获取当前时间 - N个月
    public static Date getNowDateMinusMonth(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH,- n);
        return cal.getTime();
    }

    //获取当前时间 + N个月
    public static Date getNowDateAddMonth(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH,+ n);
        return cal.getTime();
    }

    //获取当前时间 - N个周
    public static Date getNowDateMinusWeek(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR,- n);
        return cal.getTime();
    }

    //获取当前时间 + N个周
    public static Date getNowDateAddWeek(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR,+ n);
        return cal.getTime();
    }

    //获取当前时间 - N个天
    public static Date getNowDateMinusDay(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,- n);
        return cal.getTime();
    }

    //获取当前时间 + N个天
    public static Date getNowDateAddDay(Integer n){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR,+ n);
        return cal.getTime();
    }

    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("当前时间：" + dateFormat.format(DateUtil.getNowDate()));
        System.out.println("减一年：" + dateFormat.format(DateUtil.getNowDateMinusYear(1)));
        System.out.println("减一月：" + dateFormat.format(DateUtil.getNowDateMinusMonth(1)));
        System.out.println("减一周：" + dateFormat.format(DateUtil.getNowDateMinusWeek(1)));
        System.out.println("减一天：" + dateFormat.format(DateUtil.getNowDateMinusDay(1)));
    }
}

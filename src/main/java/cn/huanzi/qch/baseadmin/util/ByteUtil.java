package cn.huanzi.qch.baseadmin.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * byte数组工具类
 */
@Slf4j
public class ByteUtil {

    /**
     * 二进制转十六进制
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexStr = new StringBuilder();
        int num;
        for (byte aByte : bytes) {
            num = aByte;
            if (num < 0) {
                num += 256;
            }
            if (num < 16) {
                hexStr.append("0");
            }
            hexStr.append(Integer.toHexString(num));
        }
        return hexStr.toString().toUpperCase();
    }

    /**
     * Object对象转byte[]
     */
    public static byte[] objectToByte(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bo = null;
        ObjectOutputStream oo = null;
        try {
            bo = new ByteArrayOutputStream();
            oo = new ObjectOutputStream(bo);
            //开始写入输出流
            oo.writeObject(obj);
            //输出流转byte
            bytes = bo.toByteArray();
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        } finally {
            //关闭流
            try {
                assert bo != null;
                bo.close();
                assert oo != null;
                oo.close();
            } catch (IOException e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }
        return bytes;
    }

    /**
     * byte[]转Object对象
     */
    public static Object byteToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream bi = null;
        ObjectInputStream oi = null;
        try {
            bi = new ByteArrayInputStream(bytes);
            oi = new ObjectInputStream(bi);
            //读取输入流
            obj = oi.readObject();
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        } finally {
            //关流
            try {
                assert bi != null;
                bi.close();
                assert oi != null;
                oi.close();
            } catch (IOException e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
        }
        return obj;
    }
}
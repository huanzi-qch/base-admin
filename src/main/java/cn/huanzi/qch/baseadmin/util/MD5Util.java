package cn.huanzi.qch.baseadmin.util;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * MD5 工具类
 */
@Slf4j
public class MD5Util {

    /**
     * 生成MD5加密串
     */
    public static String getMd5(String message) {
        String md5 = "";
        try {
            //创建一个md5算法对象
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageByte = message.getBytes(StandardCharsets.UTF_8);
            //获得MD5字节数组,16*8=128位
            byte[] md5Byte = md.digest(messageByte);
            //转换为16进制字符串
            md5 = ByteUtil.bytesToHex(md5Byte);
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        }
        return md5;
    }

    /**
     * 验证方法
     * @param text 明文
     * @param md5 密文
     * @return 对比结果
     */
    private static boolean verify(String text,String md5){
        return md5.equals(getMd5(text));
    }
}

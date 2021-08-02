package cn.huanzi.qch.baseadmin.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;

/**
 * Json工具类
 */
@Slf4j
public class JsonUtil {
    private static ObjectMapper mapper;

    static{
        //jackson
        mapper = new ObjectMapper();

        //设置日期格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        //禁用空对象转换json
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        //设置null值不参与序列化(字段不被显示)
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * json字符串转对象
     */
    public static <T> T parse(String jsonStr,Class<T> clazz){
        try {
            return mapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        }
        return null;
    }

    /**
     * 对象转json字符串
     */
    public static String stringify(Object obj){
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        }
        return null;
    }
}

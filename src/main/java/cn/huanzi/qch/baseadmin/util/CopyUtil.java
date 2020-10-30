package cn.huanzi.qch.baseadmin.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 实体转换工具
 */
@Slf4j
public class CopyUtil {

    /**
     * 类型转换：实体Vo <->实体  例如：UserVo <-> User
     * 默认支持1层复杂对象复制
     */
    public static <T> T copy(Object src, Class<T> targetType) {
        return CopyUtil.copy(src,targetType,1);
    }

    /**
     * 同上，支持count多层复杂对象复制
     */
    public static <T> T copy(Object src, Class<T> targetType,Integer count) {
        //执行一层，自减1
        count--;

        T target = null;
        try {
            //创建一个空目标对象，并获取一个BeanWrapper代理器，用于属性填充，BeanWrapperImpl在内部使用Spring的BeanUtils工具类对Bean进行反射操作，设置属性。
            target = targetType.newInstance();
            BeanWrapper targetBean = new BeanWrapperImpl(target);

            //获取源对象的BeanMap，属性和属性值直接转换为Map的key-value 形式
            BeanMap srcBean = new BeanMap(src);
            for (Object key : srcBean.keySet()) {
                //源对象属性名称
                String srcPropertyName = key + "";
                //源对象属性值
                Object srcPropertyVal = srcBean.get(key);
                //源对象属性类型
                Class srcPropertyType = srcBean.getType(srcPropertyName);
                //目标对象属性类型
                Class targetPropertyType = targetBean.getPropertyType(srcPropertyName);

                //源对象属性值非空判断、目标对象属性类型非空判断，如果为空跳出，继续操作下一个属性
                if ("class".equals(srcPropertyName) || targetPropertyType == null) {
                    continue;
                }

                //类型相等，可直接设置值，比如：String与String 或者 User与User
                if (srcPropertyType == targetPropertyType) {
                    targetBean.setPropertyValue(srcPropertyName, srcPropertyVal);
                }
                //类型不相等，比如：User与UserVo
                else {
                    //满足条件，跳出递归
                    if(count <= -1){
                        return target;
                    }

                    //如果源复杂对象为null，直接跳过，不需要复制
                    if(srcPropertyVal == null){
                        continue;
                    }

                    //设置目标对象属性值
                    targetBean.setPropertyValue(srcPropertyName, CopyUtil.copy(srcPropertyVal, targetPropertyType, count));
                }
            }
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        }
        return target;
    }

    /**
     * 类型转换：实体Vo <->实体  例如：List<UserVo> <-> List<User>
     */
    public static <T> List<T> copyList(List srcList, Class<T> targetType) {
        List<T> newList = new ArrayList<>();
        for (Object src : srcList) {
            newList.add(CopyUtil.copy(src, targetType));
        }
        return newList;
    }

    /**
     * 类型转换：Object[]转Vo
     * 当使用自定义SQL查询，查询字段跟实体对应不上时，可以使用Object[]接值
     * em.createNativeQuery(sql.toString())，第二个参数不传时，默认就是用Object[]来接值
     * 因为是Object[]转Vo，是按顺序来取值、设置，所有要求两边的字段、属性顺序要一一对应
     */
    public static <T> T copyByObject(Object[] src, Class<T> targetType){
        T targetVo = null;
        try {
            //遍历Object[]转换为Field[]
            targetVo  = targetType.newInstance();
            Field[] fields = targetType.getDeclaredFields();
            int length = src.length < fields.length ? src.length : fields.length;
            for (int i = 0; i < length; i++) {
                Field field = fields[i];
                Object fieldVal = src[i];
                if (fieldVal instanceof Character || fieldVal instanceof BigDecimal) {
                    fieldVal = String.valueOf(fieldVal);
                }

                field.setAccessible(true);//获取授权
                field.set(targetVo, fieldVal);//赋值
            }
        } catch (InstantiationException | IllegalAccessException e) {
            ErrorUtil.errorInfoToString(e);
        }
        return targetVo;
    }

    /**
     * 类型转换：List<Object[]>转List<Vo>
     */
    public static <T> List<T> copyListByObject(List<Object[]> srcList, Class<T> targetType) {
        List<T> newList = new ArrayList<>();
        if (srcList != null) {
            for (Object[] src : srcList) {
                newList.add(CopyUtil.copyByObject(src,targetType));
            }
        }
        return newList;
    }
}
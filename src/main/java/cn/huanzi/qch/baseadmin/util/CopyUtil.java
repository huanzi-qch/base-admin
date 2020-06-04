package cn.huanzi.qch.baseadmin.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 实体转换工具
 */
@Slf4j
public class CopyUtil {

    /**
     * 类型转换：实体Vo <->实体  例如：UserVo <-> User
     * 支持一级复杂对象复制
     */
    public static <T> T copy(Object src, Class<T> targetType) {
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
                    /*     下面的步骤与上面的步骤基本一致      */

                    //如果源复杂对象为null，直接跳过，不需要复制
                    if(srcPropertyVal == null){
                        continue;
                    }

                    Object targetPropertyVal = targetPropertyType.newInstance();
                    BeanWrapper targetPropertyBean = new BeanWrapperImpl(targetPropertyVal);

                    BeanMap srcPropertyBean = new BeanMap(srcPropertyVal);
                    for (Object srcPropertyBeanKey : srcPropertyBean.keySet()) {
                        String srcPropertyBeanPropertyName = srcPropertyBeanKey + "";
                        Object srcPropertyBeanPropertyVal = srcPropertyBean.get(srcPropertyBeanKey);
                        Class srcPropertyBeanPropertyType = srcPropertyBean.getType(srcPropertyBeanPropertyName);
                        Class targetPropertyBeanPropertyType = targetPropertyBean.getPropertyType(srcPropertyBeanPropertyName);

                        if ("class".equals(srcPropertyBeanPropertyName) || targetPropertyBeanPropertyType == null) {
                            continue;
                        }

                        if (srcPropertyBeanPropertyType == targetPropertyBeanPropertyType) {
                            targetPropertyBean.setPropertyValue(srcPropertyBeanPropertyName, srcPropertyBeanPropertyVal);
                        } else {
                            //复杂对象里面的复杂对象不再进行处理
                        }
                    }
                    //设置目标对象属性值
                    targetBean.setPropertyValue(srcPropertyName, targetPropertyBean.getWrappedInstance());
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
        for (Object entity : srcList) {
            newList.add(copy(entity, targetType));
        }
        return newList;
    }

}
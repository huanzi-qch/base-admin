package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.sys.syssetting.vo.SysSettingVo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 系统设置工具类
 * 系统启动时获取数据库数据，设置到公用静态集合sysSettingMap
 * 更新系统设置时同步更新公用静态集合sysSettingMap
 */
public class SysSettingUtil {

    //使用线程安全的ConcurrentHashMap来存储系统设置
    private static ConcurrentHashMap<String,SysSettingVo> sysSettingMap = new ConcurrentHashMap<>(1);

    //从公用静态集合sysSettingMap获取系统设置
    public static SysSettingVo getSysSetting(){
        return sysSettingMap.get("sysSetting");
    }

    //更新公用静态集合sysSettingMap
    public static void setSysSettingMap(SysSettingVo sysSetting){
        if(sysSettingMap.isEmpty()){
            sysSettingMap.put("sysSetting",sysSetting);
        }else{
            sysSettingMap.replace("sysSetting",sysSetting);
        }
    }
}

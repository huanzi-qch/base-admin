package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.common.pojo.MonitorVo;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * 系统环境监控工具类
 */
@Slf4j
public class SystemMonitorUtil {

    private static SystemInfo systemInfo = new SystemInfo();
    private static MonitorVo monitorVo = new MonitorVo();
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static DecimalFormat decimalFormat = new DecimalFormat(".00");

    static {
        monitorVo.setOs(System.getProperties().getProperty("os.name") + "   " +  System.getProperties().getProperty("os.arch"));//操作系统
        monitorVo.setCpuInfo(systemInfo.getHardware().getProcessor().getName());//CPU名称
        monitorVo.setJvmJavaVersion(System.getProperty("java.version"));//java版本
        monitorVo.setRunTime(simpleDateFormat.format(ManagementFactory.getRuntimeMXBean().getStartTime()));//程序启动时间
    }

    public static MonitorVo getSysMonitor(){
        //jvm
        MemoryUsage heapInfo = getHeapInfo();
        monitorVo.setJvmHeapInit(decimalFormat.format(heapInfo.getInit() / 1024 / 1024));
        monitorVo.setJvmHeapMax(decimalFormat.format(heapInfo.getMax() / 1024 / 1024));
        monitorVo.setJvmHeapUsed(decimalFormat.format(heapInfo.getUsed() / 1024 / 1024));
        monitorVo.setJvmHeapCommitted(decimalFormat.format(heapInfo.getCommitted() / 1024 / 1024));
        MemoryUsage noHeapInfo = getNoHeapInfo();
        monitorVo.setJvmNonHeapInit(decimalFormat.format(noHeapInfo.getInit() / 1024 / 1024));
        monitorVo.setJvmNonHeapMax(decimalFormat.format(noHeapInfo.getMax() / 1024 / 1024));
        monitorVo.setJvmNonHeapUsed(decimalFormat.format(noHeapInfo.getUsed() / 1024 / 1024));
        monitorVo.setJvmNonHeapCommitted(decimalFormat.format(noHeapInfo.getCommitted() / 1024 / 1024));

        //系统信息
        monitorVo.setCpuUseRate(decimalFormat.format(getCpuUsage() * 100));
        OperatingSystemMXBean memoryUsage = getMemoryUsage();
        monitorVo.setRamTotal(decimalFormat.format(memoryUsage.getTotalPhysicalMemorySize() / 1024 / 1024 / 1024));
        monitorVo.setRamUsed(decimalFormat.format((memoryUsage.getTotalPhysicalMemorySize() - memoryUsage.getFreePhysicalMemorySize()) / 1024 / 1024 / 1024));
        HashMap<String, Double> diskUsage = getDiskUsage();
        monitorVo.setDiskTotal(decimalFormat.format(diskUsage.get("total")));
        monitorVo.setDiskUsed(decimalFormat.format(diskUsage.get("used")));
        return monitorVo;
    }

    /**
     * 获取jvm中堆内存信息
     */
    private static MemoryUsage getHeapInfo(){
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    }

    /**
     * 获取jvm中非堆内存信息
     */
    private static MemoryUsage getNoHeapInfo(){
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
    }

    /**
     * 获取内存信息
     */
    private static OperatingSystemMXBean getMemoryUsage() {
        return (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    }

    /**
     * 获取CPU信息
     */
    private static double getCpuUsage() {
        //这里会疯狂打印报错：oshi.util.platform.windows.PdhUtil       : Failed to get counter. Error code: 0xC0000BBC
        //有解决方法的同学可以跟我说一下
        return systemInfo.getHardware().getProcessor().getSystemCpuLoadBetweenTicks();
    }

    /**
     * 获取磁盘信息
     */
    private static HashMap<String, Double>  getDiskUsage() {
        HashMap<String, Double> hashMap = new HashMap<>(2);
        File[] files = File.listRoots();
        double total = 0;
        double used = 0;
        for (File file : files) {
            total = total + file.getTotalSpace() / 1024 / 1024 / 1024;
            used = used + file.getFreeSpace() / 1024 / 1024 / 1024;
        }
        hashMap.put("total",total);
        hashMap.put("used",used);

        return hashMap;
    }

    /**
     * 判断系统是否为windows
     *
     * @return 是否
     */
    private static boolean isWindows() {
        return System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS");
    }

    /**
     * 获取linux 磁盘使用率
     *
     * @return 磁盘使用率
     */
    private static HashMap<String, Long> getUnixDiskUsage() {
        HashMap<String, Long> hashMap = new HashMap<>(2);
        String ioCmdStr = "df -h /";
        String resultInfo = runCommand(ioCmdStr);
        log.info(resultInfo);
        String[] data = resultInfo.split(" +");
        double total = Double.parseDouble(data[9].replace("%", ""));
        return hashMap;
    }

    /**
     * 获取Windows 磁盘使用率
     *
     * @return 磁盘使用率
     */
    private static HashMap<String, Long> getWinDiskUsage() {
        HardwareAbstractionLayer hal = systemInfo.getHardware();
        HWDiskStore[] diskStores = hal.getDiskStores();
        HashMap<String, Long> hashMap = new HashMap<>(2);
        long total = 0;
        long used = 0;
        if (diskStores != null && diskStores.length > 0) {
            for (HWDiskStore diskStore : diskStores) {
                long size = diskStore.getSize();
                long writeBytes = diskStore.getWriteBytes();
                total += size;
                used += writeBytes;
            }
        }
        hashMap.put("total",total);
        hashMap.put("used",used);
        return hashMap;
    }

    /**
     * Linux 执行系统命令
     *
     * @param cmd 命令
     * @return 字符串结果
     */
    private static String runCommand(String cmd) {
        StringBuilder info = new StringBuilder(50);
        InputStreamReader isr = null;
        LineNumberReader lnr = null;
        try {
            Process pos = Runtime.getRuntime().exec(cmd);
            pos.waitFor();
            isr = new InputStreamReader(pos.getInputStream());
            lnr = new LineNumberReader(isr);
            String line;
            while ((line = lnr.readLine()) != null) {
                info.append(line).append("\n");
            }
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        }finally {
            try {
                if(isr != null){
                    isr.close();
                }
                if(lnr != null){
                    lnr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return info.toString();
    }
}

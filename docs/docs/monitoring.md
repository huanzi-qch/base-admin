## 实时监控<br/> 
　　实时监控的是系统硬件环境、以及jvm运行时内存，使用websocket，实时将数据输出到web页面，1秒刷新一次<br/>
　　使用ManagementFactory类获取信息，通过WebSocket每隔1秒推送前端页面刷新展示<br/> 

## 效果<br/> 
　　windows环境<br/>
![](https://img2020.cnblogs.com/blog/1353055/202006/1353055-20200610185112519-659512719.gif)<br/>
　　Linux环境 <br/>
![](https://img2020.cnblogs.com/blog/1353055/202007/1353055-20200724141108681-1459007961.png)<br/>

## 代码<br/> 
　　封装了系统环境监控工具类<br/>
```
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

```
　　WebSocket推送<br/>
```

/**
 * WebSocket获取实时系统监控并输出到Web页面
 */
@Slf4j
@Component
@ServerEndpoint(value = "/websocket/monitor", configurator = MyEndpointConfigure.class)
public class MonitorWSServer {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    AsyncTaskExecutor asyncTaskExecutor;

    /**
     * 连接集合
     */
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>(3);

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        //添加到集合中
        sessionMap.put(session.getId(), session);

        //获取系统监控信息
        asyncTaskExecutor.submit(() -> {
            log.info("MonitorWSServer 任务开始");
            while (sessionMap.get(session.getId()) != null) {
                try {
                    //获取系统监控信息 发送
                    send(session,  JsonUtil.stringify(SystemMonitorUtil.getSysMonitor()));

                    //休眠一秒
                    Thread.sleep(1000);
                } catch (Exception e) {
                    //输出到日志文件中
                    log.error(ErrorUtil.errorInfoToString(e));
                }
            }
            log.info("MonitorWSServer 任务结束");
        });
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        //从集合中删除
        sessionMap.remove(session.getId());
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        //输出到日志文件中
        log.error(ErrorUtil.errorInfoToString(error));
    }

    /**
     * 服务器接收到客户端消息时调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {

    }

    /**
     * 封装一个send方法，发送消息到前端
     */
    private void send(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            //输出到日志文件中
            log.error(ErrorUtil.errorInfoToString(e));
        }
    }
}
```
　　前端页面实时展示<br/>
```
<!DOCTYPE>
<!--解决idea thymeleaf 表达式模板报红波浪线-->
<!--suppress ALL -->
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>系统监控</title>
    <!-- 引入公用部分 -->
    <script th:replace="common/head::static"></script>
    <style>
        .layui-card-body {
            height: 100px;
        }
    </style>
</head>
<body>
<!-- 标题 -->
<h1 style="text-align: center;">系统环境监控</h1>
<h6 style="text-align: center;">1秒刷新一次</h6>

<div style="padding: 20px; background-color: #F2F2F2;">
    <div class="layui-row layui-col-space15">
        <div class="layui-col-md6">
            <div class="layui-card">
                <div class="layui-card-header">系统信息</div>
                <div class="layui-card-body">
                    <p>操作系统：<span id="os"></span></p>
                    <p>Java版本：<span id="jvmJavaVersion"></span></p>
                    <p>程序启动时间：<span id="runTime"></span></p>
                </div>
            </div>
        </div>
        <div class="layui-col-md6">
            <div class="layui-card">
                <div class="layui-card-header">CPU</div>
                <div class="layui-card-body">
                    <p>CPU信息：<span id="cpuInfo"></span></p>
                    <p>CPU使用率：<span id="cpuUseRate">0%</span></p>
                </div>
            </div>
        </div>

        <div class="layui-col-md6">
            <div class="layui-card">
                <div class="layui-card-header">内存</div>
                <div class="layui-card-body">
                    <p>内存总量：<span id="ramTotal">0（G）</span></p>
                    <p>已用内存：<span id="ramUsed">0（G）</span></p>
                </div>
            </div>
        </div>
        <div class="layui-col-md6">
            <div class="layui-card">
                <div class="layui-card-header">磁盘</div>
                <div class="layui-card-body">
                    <p>磁盘总量：<span id="diskTotal">0（G）</span></p>
                    <p>已用磁盘：<span id="diskUsed">0（G）</span></p>
                </div>
            </div>
        </div>

        <div class="layui-col-md6">
            <div class="layui-card">
                <div class="layui-card-header">JVM堆内存</div>
                <div class="layui-card-body">
                    <p>初始大小：<span id="jvmHeapInit">0（M）</span></p>
                    <p>最大可用：<span id="jvmHeapMax">0（M）</span></p>
                    <p>已使用：<span id="jvmHeapUsed">0（M）</span></p>
                    <p>已申请：<span id="jvmHeapCommitted">0（M）</span></p>
                </div>
            </div>
        </div>
        <div class="layui-col-md6">
            <div class="layui-card">
                <div class="layui-card-header">JVM非堆内存</div>
                <div class="layui-card-body">
                    <p>初始大小：<span id="jvmNonHeapInit">0（M）</span></p>
                    <p>最大可用：<span id="jvmNonHeapMax">0（M）</span></p>
                    <p>已使用：<span id="jvmNonHeapUsed">0（M）</span></p>
                    <p>已申请：<span id="jvmNonHeapCommitted">0（M）</span></p>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
<script th:inline="javascript">
    let port = [[${port}]];//端口

    //websocket对象
    let websocket = null;

    //判断当前浏览器是否支持WebSocket
    if ('WebSocket' in window) {
        //动态获取域名或ip
        let hostname = window.location.hostname;
        port = window.location.port;
        websocket = new WebSocket("ws://"+hostname+":" + port + ctx + "/websocket/monitor");
    } else {
        console.error("不支持WebSocket");
    }

    //连接发生错误的回调方法
    websocket.onerror = function (e) {
        console.error("WebSocket连接发生错误");
    };

    //连接成功建立的回调方法
    websocket.onopen = function () {
        console.log("WebSocket连接成功")
    };

    //接收到消息的回调方法
    websocket.onmessage = function (event) {
        //追加
        if (event.data) {
            let monitonJson = JSON.parse(event.data);
            $("#os").text(monitonJson.os);
            $("#runTime").text(monitonJson.runTime);
            $("#jvmJavaVersion").text(monitonJson.jvmJavaVersion);

            $("#jvmHeapInit").text(monitonJson.jvmHeapInit  + "（M）");
            $("#jvmHeapMax").text(monitonJson.jvmHeapMax  + "（M）");
            $("#jvmHeapUsed").text(monitonJson.jvmHeapUsed  + "（M）");
            $("#jvmHeapCommitted").text(monitonJson.jvmHeapCommitted  + "（M）");

            $("#jvmNonHeapInit").text(monitonJson.jvmNonHeapInit  + "（M）");
            $("#jvmNonHeapMax").text(monitonJson.jvmNonHeapMax  + "（M）");
            $("#jvmNonHeapUsed").text(monitonJson.jvmNonHeapUsed  + "（M）");
            $("#jvmNonHeapCommitted").text(monitonJson.jvmNonHeapCommitted  + "（M）");


            $("#cpuUseRate").text(monitonJson.cpuUseRate + "%");
            $("#cpuInfo").text(monitonJson.cpuInfo);

            $("#ramTotal").text(monitonJson.ramTotal + "（G）");
            $("#ramUsed").text(monitonJson.ramUsed + "（G）");

            $("#diskTotal").text(monitonJson.diskTotal + "（G）");
            $("#diskUsed").text(monitonJson.diskUsed + "（G）");
        }
    }

    //连接关闭的回调方法
    websocket.onclose = function () {
        console.log("WebSocket连接关闭")
    };
</script>
</html>
```
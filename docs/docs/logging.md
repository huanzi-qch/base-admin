## 实时日志<br/> 
　　使用websocket，实时将日志输出到web页面，1秒刷新一次<br/> 
　　详情请看我之前的博客：[SpringBoot系列——Logback日志](https://www.cnblogs.com/huanzi-qch/p/11041300.html)，输出到文件以及实时输出到web页面<br/> 
　　注意：这里的日志配置只配置了dev环境，prod环境尚未为空，发布生产环境前记得先配置，否则生成的日志文件将不会输入日志内容！<br/> 

　　实时日志中，主要是用到正则去匹配读取出来的日志，达到对日志进行换行、着色等操作<br/> 
## 效果<br/> 
![](https://img2018.cnblogs.com/blog/1353055/201909/1353055-20190917143841270-1600976561.png)<br/>

## 代码<br/> 
　　WebSocket推送<br/>
```

/**
 * WebSocket获取实时日志并输出到Web页面
 */
@Slf4j
@Component
@ServerEndpoint(value = "/websocket/logging", configurator = MyEndpointConfigure.class)
public class LoggingWSServer {

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    AsyncTaskExecutor asyncTaskExecutor;

    /**
     * 连接集合
     */
    private static Map<String, Session> sessionMap = new ConcurrentHashMap<>(3);
    private static Map<String, Integer> lengthMap = new ConcurrentHashMap<>(3);

    /**
     * 匹配日期开头加换行，2019-08-12 14:15:04
     */
    private Pattern datePattern = Pattern.compile("[\\d+][\\d+][\\d+][\\d+]-[\\d+][\\d+]-[\\d+][\\d+] [\\d+][\\d+]:[\\d+][\\d+]:[\\d+][\\d+]");

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        //添加到集合中
        sessionMap.put(session.getId(), session);
        lengthMap.put(session.getId(), 1);//默认从第一行开始

        //获取日志信息
        asyncTaskExecutor.submit(() -> {
            log.info("LoggingWebSocketServer 任务开始");
            boolean first = true;
            BufferedReader reader = null;
            FileReader fileReader = null;
            while (sessionMap.get(session.getId()) != null) {
                try {
                    //日志文件，获取最新的
                    fileReader = new FileReader(System.getProperty("user.home") + "/log/" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + applicationName + ".log");

                    //字符流
                    reader = new BufferedReader(fileReader);
                    Object[] lines = reader.lines().toArray();

                    //只取从上次之后产生的日志
                    Object[] copyOfRange = Arrays.copyOfRange(lines, lengthMap.get(session.getId()), lines.length);

                    //对日志进行着色，更加美观  PS：注意，这里要根据日志生成规则来操作
                    for (int i = 0; i < copyOfRange.length; i++) {
                        String line = String.valueOf(copyOfRange[i]);
                        //先转义
                        line = line.replaceAll("&", "&amp;")
                                .replaceAll("<", "&lt;")
                                .replaceAll(">", "&gt;")
                                .replaceAll("\"", "&quot;");

                        //处理等级
                        line = line.replace("DEBUG", "<span style='color: blue;'>DEBUG</span>");
                        line = line.replace("INFO", "<span style='color: green;'>INFO</span>");
                        line = line.replace("WARN", "<span style='color: orange;'>WARN</span>");
                        line = line.replace("ERROR", "<span style='color: red;'>ERROR</span>");

                        //处理类名
                        String[] split = line.split("]");
                        if (split.length >= 2) {
                            String[] split1 = split[1].split("-");
                            if (split1.length >= 2) {
                                line = split[0] + "]" + "<span style='color: #298a8a;'>" + split1[0] + "</span>" + "-" + split1[1];
                            }
                        }

                        // 匹配日期开头加换行，2019-08-12 14:15:04
                        Matcher m = datePattern.matcher(line);
                        if (m.find( )) {
                            //找到下标
                            int start = m.start();
                            //插入
                            StringBuilder  sb = new StringBuilder (line);
                            sb.insert(start,"<br/><br/>");
                            line = sb.toString();
                        }

                        copyOfRange[i] = line;
                    }

                    //存储最新一行开始
                    lengthMap.replace(session.getId(), lines.length);

                    //第一次如果太大，截取最新的200行就够了，避免传输的数据太大
                    if(first && copyOfRange.length > 200){
                        copyOfRange = Arrays.copyOfRange(copyOfRange, copyOfRange.length - 200, copyOfRange.length);
                        first = false;
                    }

                    String result = StringUtils.join(copyOfRange, "<br/>");

                    //发送
                    send(session, result);

                    //休眠一秒
                    Thread.sleep(1000);
                } catch (Exception e) {
                    //输出到日志文件中
                    log.error(ErrorUtil.errorInfoToString(e));
                }
            }
            try {
                reader.close();
                fileReader.close();
            } catch (IOException e) {
                //输出到日志文件中
                log.error(ErrorUtil.errorInfoToString(e));
            }
            log.info("LoggingWebSocketServer 任务结束");
        });
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        //从集合中删除
        sessionMap.remove(session.getId());
        lengthMap.remove(session.getId());
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
    <title>实时日志</title>
    <!-- 引入公用部分 -->
    <script th:replace="common/head::static"></script>
</head>
<body>
<!-- 标题 -->
<h1 style="text-align: center;">实时日志</h1>
<h6 style="text-align: center;">1秒刷新一次</h6>

<!-- 显示区 -->
<div id="loggingText" contenteditable="true"
     style="width:100%;height: 500px;background-color: ghostwhite; overflow: auto;"></div>

<!-- 操作栏 -->
<div style="text-align: center;">
    <button onclick="$('#loggingText').text('')" style="color: green; height: 35px;">清屏</button>
    <button onclick="$('#loggingText').animate({scrollTop:$('#loggingText')[0].scrollHeight});"
            style="color: green; height: 35px;">滚动至底部
    </button>
    <button onclick="if(window.loggingAutoBottom){$(this).text('开启自动滚动');}else{$(this).text('关闭自动滚动');};window.loggingAutoBottom = !window.loggingAutoBottom"
            style="color: green; height: 35px; ">开启自动滚动
    </button>
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
        websocket = new WebSocket("ws://"+hostname+":" + port + ctx + "/websocket/logging");
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

            //日志内容
            let $loggingText = $("#loggingText");
            $loggingText.append(event.data);

            //是否开启自动底部
            if (window.loggingAutoBottom) {
                //滚动条自动到最底部
                $loggingText.scrollTop($loggingText[0].scrollHeight);
            }
        }
    }

    //连接关闭的回调方法
    websocket.onclose = function () {
        console.log("WebSocket连接关闭")
    };
</script>
</html>
```
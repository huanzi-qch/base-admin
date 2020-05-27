package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.common.pojo.IpVo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

/**
 * IP工具类
 */
@Slf4j
public class IpUtil {
    /**
     * 获取访问者的ip地址
     * 注：要外网访问才能获取到外网地址，如果你在局域网甚至本机上访问，获得的是内网或者本机的ip
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress = null;
        try {
            //X-Forwarded-For：Squid 服务代理
            String ipAddresses = request.getHeader("X-Forwarded-For");

            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                //Proxy-Client-IP：apache 服务代理
                ipAddresses = request.getHeader("Proxy-Client-IP");
            }

            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                //WL-Proxy-Client-IP：weblogic 服务代理
                ipAddresses = request.getHeader("WL-Proxy-Client-IP");
            }

            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                //HTTP_CLIENT_IP：有些代理服务器
                ipAddresses = request.getHeader("HTTP_CLIENT_IP");
            }

            if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                //X-Real-IP：nginx服务代理
                ipAddresses = request.getHeader("X-Real-IP");
            }

            //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
            if (ipAddresses != null && ipAddresses.length() != 0) {
                ipAddress = ipAddresses.split(",")[0];
            }

            //还是不能获取到，最后再通过request.getRemoteAddr();获取
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * 调用太平洋网络IP地址查询Web接口（http://whois.pconline.com.cn/），返回ip、地理位置
     */
    public static IpVo getIpVo(String ip){
        //查本机
        String url = "http://whois.pconline.com.cn/ipJson.jsp?json=true";

        //查指定ip
        if(!StringUtils.isEmpty(ip)){
            url = "http://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip;
        }

        StringBuilder inputLine = new StringBuilder();
        String read;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestProperty("Charset", "GBK");
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "GBK"));
            while ((read = in.readLine()) != null) {
                inputLine.append(read);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //返回格式
        /*
        {
            ip: "58.63.47.115",
            pro: "广东省",
            proCode: "440000",
            city: "广州市",
            cityCode: "440100",
            region: "天河区",
            regionCode: "440106",
            addr: "广东省广州市天河区 电信",
            regionNames: "",
            err: ""
        }
         */

        IpVo ipVo = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            //当属性的值为空（null或者""）时，不进行序列化，可以减少数据传输
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            //设置日期格式
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            //转换成IpVo
            ipVo = mapper.readValue(new String(inputLine.toString().getBytes("GBK"), "GBK"), IpVo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ipVo;
    }

    /**
     * 直接根据访问者的Request，返回ip、地理位置
     */
    public static IpVo getIpVoByRequest(HttpServletRequest request){
        return IpUtil.getIpVo(IpUtil.getIpAddr(request));
    }

    /*
        终极大法：java获取不了，就用js来获取
        <!-- js获取客户ip -->
        <script src="http://whois.pconline.com.cn/ipJson.jsp"></script>
     */

    /*//测试
    public static void main(String[] args) {
        //获取本机ip
        System.out.println(getIpVo(null));

        //获取指定ip
        System.out.println(getIpVo("115.48.58.106"));
    }*/
}

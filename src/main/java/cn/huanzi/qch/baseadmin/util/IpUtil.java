package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.common.pojo.IpVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * IP工具类
 */
@Slf4j
public class IpUtil {
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 获取访问者的内网ip地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if (ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        log.error("IP获取失败了！", e);
                    }
                    assert inet != null;
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) {
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }

    /**
     * 调用接口，返回外网ip、地理位置
     */
    public static IpVo getIpVo(){
        String chinaz = "http://pv.sohu.com/cityjson?ie=utf-8";

        String inputLine = "";
        String read = "";
        try {
            URL url = new URL(chinaz);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            while ((read = in.readLine()) != null) {
                inputLine += read;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String reg = "\"";
        String [] ss = inputLine.split(reg);
        // 3 ip 7 城市编码 11 城市名
        IpVo ipVo = new IpVo();
        ipVo.setIp(ss[3]);
        ipVo.setCityCode(ss[7]);
        try {
            //ASCII、ISO-8859-1、GB2312、GBK、UTF-8、UTF-16
            ipVo.setCity(new String(ss[11].getBytes("UTF-8"), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ipVo;
    }
}

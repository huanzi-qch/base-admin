package cn.huanzi.qch.baseadmin.util;

import cn.huanzi.qch.baseadmin.common.pojo.HolidayVo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 节假日工具类
 */
public class HolidayUtil {

    /**
     * 发送get请求
     */
    private static String get(String url){
        StringBuilder inputLine = new StringBuilder();
        String read;
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setReadTimeout(30 * 1000);
            urlConnection.setConnectTimeout(30 * 1000);
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36)");
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));
            while ((read = in.readLine()) != null) {
                inputLine.append(read);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputLine.toString();
    }

    /**
     * 调用免费API查询全年工作日、周末、法定节假日、节假日调休补班数据
     * 1、调用 https://api.apihubs.cn/holiday/get?size=500&year=2021 查询全年日历（含周末）
     * 2、调用 https://timor.tech/api/holiday/year/2021 查询全年节假日、调休
     */
    public static ArrayList<HolidayVo> getAllHolidayByYear(String year) throws IOException {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        ArrayList<HolidayVo> holidayVoList = new ArrayList<>();
        HashMap<String,HolidayVo> hashMap = new HashMap<>();


        //查询全年日历包含周末
        String allDayJson = HolidayUtil.get("https://api.apihubs.cn/holiday/get?size=500&year="+year);
        Map allDayMap = JsonUtil.parse(allDayJson,Map.class);
        Map allDayData = (Map)allDayMap.get("data");
        List allDayDataList = (List)allDayData.get("list");
        allDayDataList.forEach((value) -> {
            HolidayVo holidayVo = new HolidayVo();

            Map value1 = (Map) value;
            String YEAR = value1.get("year").toString();
            String MONTH = value1.get("month").toString().replace(YEAR,"");
            String DAY = value1.get("date").toString().replace(YEAR+MONTH,"");

            holidayVo.setData(YEAR + "-" + MONTH + "-" + DAY);
            String STATUS = "0";
            String msg = "工作日";
            if("1".equals(value1.get("weekend").toString())){
                STATUS = "1";
                msg = "周末";
            }
            holidayVo.setStatus(STATUS);
            holidayVo.setMsg(msg);

            hashMap.put(holidayVo.getData(),holidayVo);
        });

        //查询全年节假日、调休
        String holidayJson = HolidayUtil.get("https://timor.tech/api/holiday/year/"+year + "/");
        Map holidayMap = JsonUtil.parse(holidayJson,Map.class);
        LinkedHashMap holidayList = (LinkedHashMap)holidayMap.get("holiday");
        holidayList.forEach((key,value) -> {
            HolidayVo holidayVo = new HolidayVo();

            Map value1 = (Map) value;
            String dateTime = value1.get("date").toString();

            holidayVo.setData(dateTime);
            String STATUS = "2";
            String msg = "法定节假日("+value1.get("name").toString()+")";
            if(value.toString().contains("调休")){
                STATUS = "3";
                msg = "节假日调休补班("+value1.get("target").toString()+")";
            }
            holidayVo.setStatus(STATUS);
            holidayVo.setMsg(msg);

            hashMap.replace(holidayVo.getData(),holidayVo);
        });

        for (String key : hashMap.keySet()) {
            holidayVoList.add(hashMap.get(key));

        }

        //排序
        holidayVoList.sort((a,b)->{
            try {
                return sf.parse(a.getData()).compareTo(sf.parse(b.getData()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 1;
        });

        return holidayVoList;
    }

//    public static void main(String[] args) {
//        try {
//            ArrayList<HolidayVo> HolidayVoList = HolidayUtil.getAllHolidayByYear("2021");
//            System.err.println("全年完整数据：");
//            for (HolidayVo HolidayVo : HolidayVoList) {
//                System.err.println(HolidayVo);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}

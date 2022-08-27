package com.lin.demo.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lin.demo.dto.PromData;
import com.lin.demo.prometheus.constants.PromConstants;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PromUtils {

    /**
     * 获取Prometheus中聚会运算后的value
     * @param format
     * @return
     */
    public static Object getPromSingleValue(String format){
        JSONArray promResult = getPromResult(format);
        if(promResult.isEmpty() || Objects.isNull(promResult)) return "";
        String value = promResult.getJSONObject(0).getJSONArray("value").getString(1);
        return value;
    }

    /**
     * 获取Prometheus返回对象的result
     * @param format：PromSql查询语句
     * @return
     */
    public static JSONArray getPromResult(String format){
        Map<String, Object> params = new HashMap<>();
        params.put(PromConstants.QUERY,format);
        JSONObject object = HttpUtils.get(PromConstants.PROM_URL, params);
        String status = (String) object.get("status");
        if(!"success".equals(status)) {
            log.error("prometheus请求失败");
            throw new RuntimeException("prometheus请求失败");
        }
        JSONObject data = object.getJSONObject("data");
        JSONArray result = data.getJSONArray("result");
        return result;
    }

    /**
     * 查询服务对于的value
     * @param jsonArray
     * @return
     */
    public static Map<String ,String> getPromResultMap(JSONArray jsonArray){
        Map<String,String> map = new HashMap<>();
        for(int i = 0;i<jsonArray.size();i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String service = (String) jsonObject.getJSONObject("metric").get("service");
            String values = (String) jsonObject.getJSONArray("value").get(1);
            map.put(service,values);
        }
        return map;
    }

    /**
     * 获取web巡检列表
     * @param jsonArray
     * @return
     */
    public static List<PromData> getPromResultDate(JSONArray jsonArray){
        List<PromData> promDataList = new ArrayList<>();
        for (int i = 0;i<jsonArray.size();i++){
            PromData promData = new PromData();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONObject metric = jsonObject.getJSONObject("metric");
            String instance = (String) metric.get("instance");
            Pattern pattern = Pattern.compile("(\\w+://\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(\\d+)");
            String port = "";
            String url = "";
            Matcher matcher = pattern.matcher(instance);
            while (matcher.find()){
                url = matcher.group(1);
                port = matcher.group(2);
            }
            String system = (String) metric.get("project");
            String service = (String) metric.get("service");
            BigDecimal bigDecimal = (BigDecimal) jsonObject.getJSONArray("value").get(0);
            long longValue = bigDecimal.longValue();
            LocalDateTime promTime = LocalDateTime.ofEpochSecond(longValue,0,ZoneOffset.ofHours(8));
            String values = (String) jsonObject.getJSONArray("value").get(1);

            promData.setPromTime(promTime)
                    .setResult(values)
                    .setPort(port)
                    .setUrl(url)
                    .setService(service)
                    .setSystem(system);
            promDataList.add(promData);
        }
        return promDataList;
    }

}

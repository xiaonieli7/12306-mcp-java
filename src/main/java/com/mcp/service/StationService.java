package com.mcp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mcp.constants.RailwayConstants;
import com.mcp.model.StationData;
import com.mcp.utils.HttpUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 车站服务类
 * 提供车站信息查询功能
 */
@Slf4j
@Service
public class StationService {
    /**
     * 存储所有车站数据，以车站代码为键
     */
    private Map<String, StationData> stationMap = new HashMap<>();
    
    /**
     * 存储城市下的所有车站信息，以城市名为键
     */
    private Map<String, List<Map<String, String>>> cityStationsMap = new HashMap<>();
    
    /**
     * 存储车站名称对应的车站代码，以车站名为键
     */
    private Map<String, Map<String, String>> nameStationsMap = new HashMap<>();
    
    /**
     * 初始化方法，启动时加载所有车站数据
     */
    @PostConstruct
    public void init() {
        loadStations();
    }
    
    /**
     * 加载车站数据
     */
    private void loadStations() {
        log.info("开始加载车站数据...");
        try {
            // 获取12306官网的车站数据
            String url = RailwayConstants.API_BASE + "/otn/resources/js/framework/station_name.js";
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
            
            String response = HttpUtils.get(url, null, headers);
            if (response != null) {
                // 解析车站数据
                parseStations(response);
                log.info("车站数据加载完成，共 {} 个车站", stationMap.size());
            } else {
                log.error("获取车站数据失败");
            }
        } catch (Exception e) {
            log.error("加载车站数据异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 解析车站数据
     *
     * @param rawData 原始数据
     */
    private void parseStations(String rawData) {
        if (!StringUtils.hasLength(rawData)) {
            return;
        }
        
        // 提取车站数据字符串
        Pattern pattern = Pattern.compile("var station_names ='([^']+)'");
        Matcher matcher = pattern.matcher(rawData);
        
        if (matcher.find()) {
            String stationData = matcher.group(1);
            String[] stations = stationData.split("@");
            
            for (String station : stations) {
                if (!StringUtils.hasLength(station)) {
                    continue;
                }
                
                String[] parts = station.split("\\|");
                if (parts.length >= 6) {
                    StationData stationInfo = new StationData();
                    stationInfo.setStationId(parts[0]);
                    stationInfo.setStationName(parts[1]);
                    stationInfo.setStationCode(parts[2]);
                    stationInfo.setStationPinyin(parts[3]);
                    stationInfo.setStationShort(parts[4]);
                    stationInfo.setStationIndex(parts[5]);
                    stationInfo.setCityIndex(parts[6]);
                    stationInfo.setCity(parts[7]);

                    // 存储到stationMap
                    stationMap.put(parts[2], stationInfo);
                    
                    // 存储到nameStationsMap
                    Map<String, String> stationCodeMap = new HashMap<>();
                    stationCodeMap.put("station_code", parts[2]);
                    stationCodeMap.put("station_name", parts[1]);
                    nameStationsMap.put(parts[1], stationCodeMap);
                    
                    // 存储到cityStationsMap
                    List<Map<String, String>> cityStations = cityStationsMap.getOrDefault(parts[7], new ArrayList<>());
                    Map<String, String> stationMap = new HashMap<>();
                    stationMap.put("station_code", parts[2]);
                    stationMap.put("station_name", parts[1]);
                    cityStations.add(stationMap);
                    cityStationsMap.put(parts[7], cityStations);
                }
            }
        }
    }
    
    /**
     * 获取指定城市的所有车站信息
     *
     * @param cityName 城市名称
     * @return 城市车站信息的JSON字符串
     */
    @Tool(description = "根据城市名称获取该城市的所有车站信息")
    public String getStationsCodeInCity(@ToolParam(description = "城市名称") String cityName) {
        log.debug("查询城市车站信息: {}", cityName);
        if (!StringUtils.hasLength(cityName)) {
            log.warn("请求中提供的城市名称为空");
            return "请提供有效的城市名称";
        }
        
        List<Map<String, String>> stations = cityStationsMap.get(cityName);
        if (stations == null || stations.isEmpty()) {
            log.info("未找到城市 {} 的车站信息", cityName);
            return "未找到城市 " + cityName + " 的车站信息";
        }
        
        log.info("找到城市 {} 的车站信息，共 {} 个站点", cityName, stations.size());
        JSONObject result = new JSONObject();
        result.put(cityName, stations);
        return result.toJSONString();
    }
    
    /**
     * 根据车站名称获取车站代码
     *
     * @param stationName 车站名称
     * @return 车站代码信息的JSON字符串
     */
    @Tool(description = "根据车站名称获取车站代码")
    public String getStationCodeByName(@ToolParam(description = "车站名称") String stationName) {
        log.debug("查询车站代码: {}", stationName);
        if (!StringUtils.hasLength(stationName)) {
            log.warn("请求中提供的车站名称为空");
            return "请提供有效的车站名称";
        }
        
        Map<String, String> station = nameStationsMap.get(stationName);
        if (station == null) {
            log.info("未找到名为 {} 的车站", stationName);
            return "未找到名为 " + stationName + " 的车站";
        }
        
        log.info("找到车站 {} 的代码信息: {}", stationName, station.get("station_code"));
        JSONObject result = new JSONObject();
        result.put(stationName, station);
        return result.toJSONString();
    }
    
    /**
     * 根据多个车站名称获取车站代码
     *
     * @param stationNames 多个车站名称，以逗号分隔
     * @return 多个车站代码信息的JSON字符串
     */
    @Tool(description = "根据多个车站名称（用逗号分隔）获取车站代码")
    public String getStationCodeByNames(@ToolParam(description = "多个车站名称，用逗号分隔") String stationNames) {
        log.debug("批量查询车站代码: {}", stationNames);
        if (!StringUtils.hasLength(stationNames)) {
            log.warn("请求中提供的车站名称列表为空");
            return "请提供有效的车站名称列表";
        }
        
        String[] names = stationNames.split(",");
        JSONObject result = new JSONObject();
        int foundCount = 0;
        
        for (String name : names) {
            String trimmedName = name.trim();
            Map<String, String> station = nameStationsMap.get(trimmedName);
            if (station != null) {
                result.put(trimmedName, station);
                foundCount++;
            } else {
                log.info("未找到名为 {} 的车站", trimmedName);
            }
        }
        
        log.info("批量查询车站代码完成，共查询 {} 个站点，找到 {} 个", names.length, foundCount);
        return result.toJSONString();
    }
    
    /**
     * 根据车站代码获取车站名称
     *
     * @param stationCode 车站代码
     * @return 车站名称
     */
    @Tool(description = "根据车站代码获取车站名称")
    public String getStationNameByCode(@ToolParam(description = "车站代码") String stationCode) {
        log.debug("根据代码查询车站名称: {}", stationCode);
        if (!StringUtils.hasLength(stationCode)) {
            log.warn("请求中提供的车站代码为空");
            return "请提供有效的车站代码";
        }
        
        StationData stationData = stationMap.get(stationCode);
        if (stationData == null) {
            log.info("未找到代码为 {} 的车站", stationCode);
            return "未找到代码为 " + stationCode + " 的车站";
        }
        
        log.info("找到车站代码 {} 对应的站点: {}", stationCode, stationData.getStationName());
        return stationData.getStationName();
    }
    
    /**
     * 根据车站名称或拼音前缀模糊查询车站
     *
     * @param keyword 关键词
     * @return 匹配的车站列表JSON字符串
     */
    @Tool(description = "根据关键词（车站名称或拼音前缀）模糊查询车站")
    public String searchStations(@ToolParam(description = "查询关键词") String keyword) {
        log.debug("模糊查询车站: {}", keyword);
        if (!StringUtils.hasLength(keyword)) {
            log.warn("请求中提供的查询关键词为空");
            return "请提供有效的查询关键词";
        }
        
        try {
            // 转换为小写以进行不区分大小写的搜索
            final String lowerKeyword = keyword.toLowerCase();
            
            // 根据关键词进行模糊匹配
            List<Map<String, String>> matchedStations = stationMap.values().stream()
                    .filter(station -> 
                            station.getStationName().contains(keyword) ||
                            station.getStationPinyin().toLowerCase().startsWith(lowerKeyword) ||
                            station.getStationShort().toLowerCase().startsWith(lowerKeyword))
                    .map(station -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("station_name", station.getStationName());
                        map.put("station_code", station.getStationCode());
                        map.put("city", station.getCity());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            log.info("模糊查询 {} 结果: 找到 {} 个匹配的车站", keyword, matchedStations.size());
            return JSON.toJSONString(matchedStations);
        } catch (Exception e) {
            log.error("模糊查询车站异常: {}", e.getMessage(), e);
            return "查询车站异常: " + e.getMessage();
        }
    }
    
    /**
     * 获取所有车站的总数
     *
     * @return 车站总数
     */
    @Tool(description = "获取系统中所有车站的总数")
    public int getTotalStationCount() {
        log.debug("获取车站总数");
        int count = stationMap.size();
        log.info("当前系统中共有 {} 个车站", count);
        return count;
    }
} 

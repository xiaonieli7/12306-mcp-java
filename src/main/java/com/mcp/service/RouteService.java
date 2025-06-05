package com.mcp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mcp.constants.RailwayConstants;
import com.mcp.model.RouteStationData;
import com.mcp.model.RouteStationInfo;
import com.mcp.utils.DateUtils;
import com.mcp.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路线服务类
 * 提供列车路线查询功能
 */
@Slf4j
@Service
public class RouteService {
    
    /**
     * 查询列车路线信息
     *
     * @param date        日期，格式为 yyyy-MM-dd
     * @param trainCode   车次代码，如G1、D1等
     * @param fromStation 出发站代码
     * @param toStation   到达站代码
     * @return 列车路线信息的JSON字符串
     */
    @Tool(description = "查询列车的完整路线信息，包括所有停靠站")
    public String getTrainRoute(
            @ToolParam(description = "查询日期，格式为yyyy-MM-dd") String date,
            @ToolParam(description = "列车编号，如G1, K1080等") String trainCode,
            @ToolParam(description = "出发站代码") String fromStation,
            @ToolParam(description = "到达站代码") String toStation) {
        
        log.debug("开始查询列车路线: 日期={}, 车次={}, 出发站={}, 到达站={}", date, trainCode, fromStation, toStation);
        
        // 参数验证
        if (!DateUtils.isValidFutureDate(date)) {
            log.warn("无效的查询日期: {}", date);
            return "请提供有效的查询日期（今天或未来日期）";
        }
        
        if (!StringUtils.hasLength(trainCode)) {
            log.warn("车次编号为空");
            return "请提供有效的列车编号";
        }
        
        if (!StringUtils.hasLength(fromStation) || !StringUtils.hasLength(toStation)) {
            log.warn("站点代码无效: 出发站={}, 到达站={}", fromStation, toStation);
            return "请提供有效的出发站和到达站代码";
        }
        
        try {
            log.info("查询列车 {} 的路线信息: {} 从 {} 到 {}", trainCode, date, fromStation, toStation);
            
            // 首先获取列车编号
            String trainNo = getTrainNo(date, trainCode, fromStation, toStation);
            if (trainNo == null) {
                log.warn("未找到列车编号: {}", trainCode);
                return "未找到列车 " + trainCode + " 的信息";
            }
            
            log.debug("获取到列车内部编号: {}", trainNo);
            
            // 查询12306 API获取列车路线信息
            String url = RailwayConstants.API_BASE + "/otn/czxx/queryByTrainNo";
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("train_no", trainNo);
            params.put("from_station_telecode", fromStation);
            params.put("to_station_telecode", toStation);
            params.put("depart_date", date);
            
            log.debug("构建路线查询参数: {}", params);
            
            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
            
            // 获取Cookie
            Map<String, String> cookies = HttpUtils.getCookie(RailwayConstants.WEB_URL);
            if (!cookies.isEmpty()) {
                String cookieString = HttpUtils.formatCookies(cookies);
                headers.put("Cookie", cookieString);
                log.debug("使用Cookie: {}", cookieString);
            } else {
                log.debug("未获取到Cookie");
            }
            
            // 发送请求
            log.info("发送路线查询API请求: {}", url);
            String response = HttpUtils.get(url, params, headers);
            if (response == null) {
                log.error("API返回为空");
                return "查询列车路线失败，请稍后重试";
            }
            
            // 解析响应数据
            log.debug("解析路线API响应");
            JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse.getIntValue("httpstatus") != 200) {
                String errorMsg = jsonResponse.getString("messages");
                log.error("API请求失败: {}", errorMsg);
                return "请求失败：" + errorMsg;
            }
            
            JSONObject data = jsonResponse.getJSONObject("data");
            if (data == null || !data.containsKey("data")) {
                log.warn("API响应中无路线数据");
                return "没有找到列车 " + trainCode + " 的路线信息";
            }
            
            JSONArray stationsArray = data.getJSONArray("data");
            if (stationsArray == null || stationsArray.isEmpty()) {
                log.info("列车 {} 的路线数据为空", trainCode);
                return "没有找到列车 " + trainCode + " 的路线信息";
            }
            
            log.info("获取到列车 {} 的 {} 个站点信息", trainCode, stationsArray.size());
            
            // 解析路线数据
            List<RouteStationData> routeStations = parseRouteStationsData(stationsArray);
            log.debug("解析路线数据完成，共 {} 个站点", routeStations.size());
            
            // 格式化路线信息
            List<RouteStationInfo> routeStationInfos = parseRouteStationsInfo(routeStations);
            log.debug("格式化路线信息完成，共 {} 个站点信息", routeStationInfos.size());
            
            // 返回结果
            String result = formatRouteStationsInfo(routeStationInfos);
            log.info("查询完成，返回列车 {} 的 {} 个站点信息", trainCode, routeStationInfos.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询列车路线信息异常: {}", e.getMessage(), e);
            return "查询列车路线出错：" + e.getMessage();
        }
    }
    
    /**
     * 获取列车编号
     *
     * @param date        日期
     * @param trainCode   车次代码
     * @param fromStation 出发站代码
     * @param toStation   到达站代码
     * @return 列车编号
     */
    private String getTrainNo(String date, String trainCode, String fromStation, String toStation) {
        log.debug("开始获取列车内部编号: {}", trainCode);
        try {
            // 请求车票查询接口获取列车编号
            String url = RailwayConstants.API_BASE + "/otn/leftTicket/query";
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("leftTicketDTO.train_date", date);
            params.put("leftTicketDTO.from_station", fromStation);
            params.put("leftTicketDTO.to_station", toStation);
            params.put("purpose_codes", "ADULT");
            
            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.111 Safari/537.36");
            
            // 获取Cookie
            Map<String, String> cookies = HttpUtils.getCookie(RailwayConstants.WEB_URL);
            if (!cookies.isEmpty()) {
                headers.put("Cookie", HttpUtils.formatCookies(cookies));
            }
            
            log.debug("发送车票查询请求获取列车编号");
            // 发送请求
            String response = HttpUtils.get(url, params, headers);
            if (response == null) {
                log.warn("获取列车编号的API返回为空");
                return null;
            }
            
            // 解析响应数据
            JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse.getIntValue("httpstatus") != 200) {
                log.warn("获取列车编号的API请求失败: {}", jsonResponse.getString("messages"));
                return null;
            }
            
            JSONObject data = jsonResponse.getJSONObject("data");
            if (data == null || !data.containsKey("result")) {
                log.warn("获取列车编号的API响应中无车票数据");
                return null;
            }
            
            JSONArray resultArray = data.getJSONArray("result");
            if (resultArray == null || resultArray.isEmpty()) {
                log.warn("获取列车编号的API查询结果为空");
                return null;
            }
            
            // 遍历结果，查找匹配的列车
            for (int i = 0; i < resultArray.size(); i++) {
                String ticketString = resultArray.getString(i);
                String[] ticketParts = ticketString.split("\\|");
                
                if (ticketParts.length < 3) {
                    continue;
                }
                
                // 匹配列车编号
                if (trainCode.equalsIgnoreCase(ticketParts[3])) {
                    log.info("找到列车 {} 的内部编号: {}", trainCode, ticketParts[2]);
                    return ticketParts[2];  // 返回列车编号
                }
            }
            
            log.warn("未找到匹配的列车: {}", trainCode);
            return null;
            
        } catch (Exception e) {
            log.error("获取列车编号异常: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 解析路线站点数据
     *
     * @param stationsArray 站点数组
     * @return 路线站点数据列表
     */
    private List<RouteStationData> parseRouteStationsData(JSONArray stationsArray) {
        log.debug("开始解析路线站点数据，共 {} 条", stationsArray.size());
        List<RouteStationData> routeStations = new ArrayList<>();
        
        for (int i = 0; i < stationsArray.size(); i++) {
            JSONObject stationObject = stationsArray.getJSONObject(i);
            
            RouteStationData stationData = new RouteStationData();
            stationData.setStationName(stationObject.getString("station_name"));
            stationData.setArriveTime(stationObject.getString("arrive_time"));
            stationData.setStartTime(stationObject.getString("start_time"));
            stationData.setStopoverTime(stationObject.getString("stopover_time"));
            stationData.setStationNo(stationObject.getIntValue("station_no"));
            stationData.setIsEnabled(stationObject.getBooleanValue("isEnabled"));
            
            routeStations.add(stationData);
            log.trace("解析站点: {}. {}, 到达时间: {}, 发车时间: {}", 
                    stationData.getStationNo(), stationData.getStationName(), 
                    stationData.getArriveTime(), stationData.getStartTime());
        }
        
        log.debug("路线站点数据解析完成，共 {} 个站点", routeStations.size());
        return routeStations;
    }
    
    /**
     * 解析为格式化后的路线站点信息
     *
     * @param routeStationDataList 路线站点数据列表
     * @return 格式化后的路线站点信息列表
     */
    private List<RouteStationInfo> parseRouteStationsInfo(List<RouteStationData> routeStationDataList) {
        log.debug("开始格式化路线站点信息");
        List<RouteStationInfo> routeStationInfoList = new ArrayList<>();
        
        for (RouteStationData stationData : routeStationDataList) {
            RouteStationInfo stationInfo = new RouteStationInfo();
            stationInfo.setArriveTime(stationData.getArriveTime());
            stationInfo.setStationName(stationData.getStationName());
            stationInfo.setStopoverTime(stationData.getStopoverTime());
            stationInfo.setStationNo(Integer.parseInt(String.valueOf(stationData.getStationNo())));
            
            routeStationInfoList.add(stationInfo);
        }
        
        log.debug("路线站点信息格式化完成，共 {} 个站点信息", routeStationInfoList.size());
        return routeStationInfoList;
    }
    
    /**
     * 格式化路线站点信息
     *
     * @param routeStationInfoList 路线站点信息列表
     * @return 格式化后的JSON字符串
     */
    private String formatRouteStationsInfo(List<RouteStationInfo> routeStationInfoList) {
        if (routeStationInfoList == null || routeStationInfoList.isEmpty()) {
            return "没有找到符合条件的路线站点信息";
        }
        
        // 转为JSON字符串
        return JSON.toJSONString(routeStationInfoList);
    }
} 
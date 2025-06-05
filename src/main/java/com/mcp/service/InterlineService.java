package com.mcp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mcp.constants.RailwayConstants;
import com.mcp.model.InterlineInfo;
import com.mcp.model.Price;
import com.mcp.model.TicketInfo;
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
import java.util.stream.Collectors;

/**
 * 中转路线服务类
 * 提供列车中转查询功能
 */
@Slf4j
@Service
public class InterlineService {
    
    /**
     * 查询两站之间的中转路线方案
     *
     * @param date       日期，格式为yyyy-MM-dd
     * @param fromStation 出发站代码
     * @param toStation  到达站代码
     * @param trainTypes 列车类型过滤，如G,D,K等，多种类型用逗号分隔
     * @return 中转路线信息的JSON字符串
     */
    @Tool(description = "查询两站之间的中转乘车方案，支持按车型筛选")
    public String getInterlineRoutes(
            @ToolParam(description = "查询日期，格式为yyyy-MM-dd") String date,
            @ToolParam(description = "出发站代码") String fromStation,
            @ToolParam(description = "到达站代码") String toStation,
            @ToolParam(description = "列车类型过滤，如G,D,K等，多种类型用逗号分隔，可为空") String trainTypes) {
        
        log.debug("开始查询中转路线: 日期={}, 出发站={}, 到达站={}, 车型过滤={}", date, fromStation, toStation, trainTypes);
        
        // 参数验证
        if (!DateUtils.isValidFutureDate(date)) {
            log.warn("无效的查询日期: {}", date);
            return "请提供有效的查询日期（今天或未来日期）";
        }
        
        if (!StringUtils.hasLength(fromStation) || !StringUtils.hasLength(toStation)) {
            log.warn("站点代码无效: 出发站={}, 到达站={}", fromStation, toStation);
            return "请提供有效的出发站和到达站代码";
        }
        
        try {
            log.info("查询中转路线: {} 从 {} 到 {}", date, fromStation, toStation);
            
            // 请求12306 API获取中转路线信息
            String url = RailwayConstants.API_BASE + "/otn/lcxx/query";
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("to_station", toStation);
            params.put("from_station", fromStation);
            params.put("depart_date", date);
            params.put("purpose_codes", "ADULT");
            
            log.debug("构建中转查询参数: {}", params);
            
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
            log.info("发送中转查询API请求: {}", url);
            String response = HttpUtils.get(url, params, headers);
            if (response == null) {
                log.error("API返回为空");
                return "查询中转路线失败，请稍后重试";
            }
            
            // 解析响应数据
            log.debug("解析中转查询API响应");
            JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse.getIntValue("httpstatus") != 200) {
                String errorMsg = jsonResponse.getString("messages");
                log.error("API请求失败: {}", errorMsg);
                return "请求失败：" + errorMsg;
            }
            
            JSONObject data = jsonResponse.getJSONObject("data");
            if (data == null || !data.containsKey("interlist") || !data.containsKey("timeSpan")) {
                log.warn("API响应中无中转路线数据");
                return "没有找到中转路线信息";
            }
            
            JSONArray interlineArray = data.getJSONArray("interlist");
            if (interlineArray == null || interlineArray.isEmpty()) {
                log.info("查询结果为空: {} 从 {} 到 {}", date, fromStation, toStation);
                return "没有找到符合条件的中转路线信息";
            }
            
            log.info("查询到 {} 条中转路线信息", interlineArray.size());
            
            // 解析中转路线数据
            List<InterlineInfo> interlineInfoList = parseInterlineData(interlineArray);
            log.debug("解析中转路线数据完成，共 {} 条", interlineInfoList.size());
            
            // 根据列车类型过滤
            if (StringUtils.hasLength(trainTypes)) {
                log.debug("按车型过滤: {}", trainTypes);
                int beforeCount = interlineInfoList.size();
                interlineInfoList = filterInterlineInfoByTrainTypes(interlineInfoList, trainTypes);
                log.info("车型过滤后剩余 {} 条中转路线信息（过滤前 {} 条）", interlineInfoList.size(), beforeCount);
            }
            
            // 格式化结果
            String result = formatInterlineInfo(interlineInfoList);
            log.info("查询完成，返回 {} 条中转路线信息", interlineInfoList.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询中转路线信息异常: {}", e.getMessage(), e);
            return "查询中转路线出错：" + e.getMessage();
        }
    }
    
    /**
     * 解析中转路线数据
     *
     * @param interlineArray 中转路线数据数组
     * @return 中转路线信息列表
     */
    private List<InterlineInfo> parseInterlineData(JSONArray interlineArray) {
        log.debug("开始解析中转路线数据，共 {} 条", interlineArray.size());
        List<InterlineInfo> interlineInfoList = new ArrayList<>();
        
        for (int i = 0; i < interlineArray.size(); i++) {
            try {
                JSONObject interlineObject = interlineArray.getJSONObject(i);
                
                InterlineInfo interlineInfo = new InterlineInfo();
                interlineInfo.setFromStation(interlineObject.getString("from_station"));
                interlineInfo.setToStation(interlineObject.getString("to_station"));
                interlineInfo.setMiddleStation(interlineObject.getString("middle_station"));
                interlineInfo.setFirstTrain(interlineObject.getString("first_train"));
                interlineInfo.setSecondTrain(interlineObject.getString("second_train"));
                interlineInfo.setStartTime(interlineObject.getString("start_time"));
                interlineInfo.setArriveTime(interlineObject.getString("arrive_time"));
                interlineInfo.setTotalTime(interlineObject.getString("total_time"));
                interlineInfo.setWaitTime(interlineObject.getString("wait_time"));
                interlineInfo.setSameStation(interlineObject.getBooleanValue("same_station"));
                
                // 提取车票信息
                interlineInfo.setTicketsInfo(parseTicketInfoFromInterline(interlineObject));
                
                // 提取DW标识
                interlineInfo.setDwFlags(extractDWFlags(interlineObject));
                
                interlineInfoList.add(interlineInfo);
                log.trace("解析中转路线: {} -> {} -> {}, 第一程: {}, 第二程: {}", 
                        interlineInfo.getFromStation(), interlineInfo.getMiddleStation(), interlineInfo.getToStation(),
                        interlineInfo.getFirstTrain(), interlineInfo.getSecondTrain());
            } catch (Exception e) {
                log.warn("解析中转路线数据异常: {}", e.getMessage());
            }
        }
        
        log.debug("中转路线数据解析完成，共 {} 条", interlineInfoList.size());
        return interlineInfoList;
    }
    
    /**
     * 从中转数据中解析车票信息
     *
     * @param interlineObject 中转路线对象
     * @return 车票信息列表
     */
    private List<TicketInfo> parseTicketInfoFromInterline(JSONObject interlineObject) {
        List<TicketInfo> ticketInfoList = new ArrayList<>();
        
        JSONArray fullList = interlineObject.getJSONArray("fullList");
        if (fullList != null && !fullList.isEmpty()) {
            JSONObject firstSegment = fullList.getJSONObject(0);
            String trainCode = firstSegment.getString("station_train_code");
            
            TicketInfo ticketInfo = new TicketInfo();
            ticketInfo.setTrainNo(firstSegment.getString("train_no"));
            ticketInfo.setStartTrainCode(trainCode);
            ticketInfo.setStartTime(firstSegment.getString("start_time"));
            ticketInfo.setArriveTime(firstSegment.getString("arrive_time"));
            ticketInfo.setLishi(firstSegment.getString("lishi"));
            ticketInfo.setFromStation(firstSegment.getString("from_station_name"));
            ticketInfo.setToStation(firstSegment.getString("to_station_name"));
            ticketInfo.setFromStationTelecode(firstSegment.getString("from_station_telecode"));
            ticketInfo.setToStationTelecode(firstSegment.getString("to_station_telecode"));
            
            // 提取服务标识
            List<String> dwFlags = extractDWFlags(firstSegment.getString("dw_flag"));
            ticketInfo.setDwFlag(dwFlags);
            
            // 提取座位价格信息，这里简化处理，实际可能需要从接口获取
            List<Price> prices = new ArrayList<>();
            
            // 高级软卧
            if (!"无".equals(firstSegment.getString("gr_num"))) {
                Price price = new Price();
                price.setSeatName("高级软卧");
                price.setShortName("gr");
                price.setSeatTypeCode("6");
                price.setNum(firstSegment.getString("gr_num"));
                price.setPrice(null);
                price.setDiscount(null);
                prices.add(price);
            }
            
            // 其他座位类型类似处理...
            
            ticketInfo.setPrices(prices);
            ticketInfoList.add(ticketInfo);
        }
        
        return ticketInfoList;
    }
    
    /**
     * 提取服务标识
     *
     * @param dwFlagStr 服务标识字符串
     * @return 服务标识列表
     */
    private List<String> extractDWFlags(String dwFlagStr) {
        List<String> flags = new ArrayList<>();
        
        if (!StringUtils.hasLength(dwFlagStr)) {
            return flags;
        }
        
        String[] flagsArr = dwFlagStr.split("#");
        for (String flag : flagsArr) {
            if (StringUtils.hasLength(flag)) {
                for (String dwFlag : RailwayConstants.DW_FLAGS) {
                    if (flag.contains(dwFlag)) {
                        flags.add(dwFlag);
                        break;
                    }
                }
            }
        }
        
        return flags;
    }
    
    /**
     * 从中转对象中提取服务标识
     *
     * @param interlineObject 中转路线对象
     * @return 服务标识列表
     */
    private List<String> extractDWFlags(JSONObject interlineObject) {
        List<String> flags = new ArrayList<>();
        
        JSONArray fullList = interlineObject.getJSONArray("fullList");
        if (fullList != null && !fullList.isEmpty()) {
            JSONObject firstSegment = fullList.getJSONObject(0);
            String dwFlagStr = firstSegment.getString("dw_flag");
            
            // 使用字符串版本的方法提取标识
            return extractDWFlags(dwFlagStr);
        }
        
        return flags;
    }
    
    /**
     * 格式化中转路线信息
     *
     * @param interlineInfoList 中转路线信息列表
     * @return 格式化后的JSON字符串
     */
    private String formatInterlineInfo(List<InterlineInfo> interlineInfoList) {
        if (interlineInfoList == null || interlineInfoList.isEmpty()) {
            return "没有找到符合条件的中转路线";
        }
        
        // 格式化为易读的数据
        JSONArray formattedList = new JSONArray();
        
        for (InterlineInfo info : interlineInfoList) {
            JSONObject obj = new JSONObject();
            obj.put("from_station", info.getFromStation());
            obj.put("middle_station", info.getMiddleStation());
            obj.put("to_station", info.getToStation());
            obj.put("first_train", info.getFirstTrain());
            obj.put("second_train", info.getSecondTrain());
            obj.put("start_time", info.getStartTime());
            obj.put("arrive_time", info.getArriveTime());
            obj.put("total_time", info.getTotalTime());
            obj.put("wait_time", info.getWaitTime());
            obj.put("same_station", info.getSameStation());
            
            formattedList.add(obj);
        }
        
        return JSON.toJSONString(formattedList);
    }
    
    /**
     * 根据列车类型过滤中转路线信息
     *
     * @param interlineInfoList 中转路线信息列表
     * @param trainTypes        列车类型，用逗号分隔
     * @return 过滤后的中转路线信息列表
     */
    private List<InterlineInfo> filterInterlineInfoByTrainTypes(List<InterlineInfo> interlineInfoList, String trainTypes) {
        if (!StringUtils.hasLength(trainTypes)) {
            return interlineInfoList;
        }
        
        log.debug("按照列车类型过滤中转路线: {}", trainTypes);
        String[] types = trainTypes.split(",");
        
        return interlineInfoList.stream()
                .filter(interline -> {
                    boolean matchFirst = false;
                    boolean matchSecond = false;
                    
                    for (String type : types) {
                        if (matchTrainType(interline.getFirstTrain(), type.trim(), interline.getDwFlags())) {
                            matchFirst = true;
                        }
                        
                        if (matchTrainType(interline.getSecondTrain(), type.trim(), interline.getDwFlags())) {
                            matchSecond = true;
                        }
                    }
                    
                    // 两程车都匹配返回true
                    boolean match = matchFirst && matchSecond;
                    if (match) {
                        log.debug("中转路线 {} -> {} 匹配车型过滤条件", interline.getFirstTrain(), interline.getSecondTrain());
                    }
                    return match;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 判断车次是否匹配指定的列车类型
     *
     * @param trainCode  车次代码
     * @param trainType  列车类型
     * @param dwFlags    服务标识列表
     * @return 是否匹配
     */
    private boolean matchTrainType(String trainCode, String trainType, List<String> dwFlags) {
        if (!StringUtils.hasLength(trainType)) {
            return true;
        }
        
        // 按列车编号前缀匹配
        if ("G".equalsIgnoreCase(trainType) && (trainCode.startsWith("G") || trainCode.startsWith("C"))) {
            log.trace("车次 {} 匹配高铁/城际类型", trainCode);
            return true;
        } else if ("D".equalsIgnoreCase(trainType) && trainCode.startsWith("D")) {
            log.trace("车次 {} 匹配动车类型", trainCode);
            return true;
        } else if ("Z".equalsIgnoreCase(trainType) && trainCode.startsWith("Z")) {
            log.trace("车次 {} 匹配直达特快类型", trainCode);
            return true;
        } else if ("T".equalsIgnoreCase(trainType) && trainCode.startsWith("T")) {
            log.trace("车次 {} 匹配特快类型", trainCode);
            return true;
        } else if ("K".equalsIgnoreCase(trainType) && trainCode.startsWith("K")) {
            log.trace("车次 {} a匹配快速类型", trainCode);
            return true;
        } else if ("O".equalsIgnoreCase(trainType) && 
                !(trainCode.startsWith("G") || trainCode.startsWith("D") || 
                  trainCode.startsWith("C") || trainCode.startsWith("Z") || 
                  trainCode.startsWith("T") || trainCode.startsWith("K"))) {
            log.trace("车次 {} 匹配其他类型", trainCode);
            return true;
        }
        
        // 按服务标识匹配
        if ("F".equalsIgnoreCase(trainType) && dwFlags != null && 
                dwFlags.stream().anyMatch(flag -> flag.contains("复兴号"))) {
            log.trace("车次 {} 匹配复兴号类型", trainCode);
            return true;
        } else if ("S".equalsIgnoreCase(trainType) && dwFlags != null && 
                dwFlags.stream().anyMatch(flag -> flag.contains("智能动车组"))) {
            log.trace("车次 {} 匹配智能动车组类型", trainCode);
            return true;
        }
        
        return false;
    }
} 
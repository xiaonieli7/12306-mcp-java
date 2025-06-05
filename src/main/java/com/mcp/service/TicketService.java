package com.mcp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mcp.utils.DateUtils;
import com.mcp.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 车票服务类
 * 提供列车票务查询功能
 */
@Slf4j
@Service
public class TicketService {
    
    /**
     * 查询车票信息
     *
     * @param date          日期，格式为 yyyy-MM-dd
     * @param fromStation   出发站代码
     * @param toStation     到达站代码
     * @param trainTypes    列车类型过滤，如G,D,K等，多种类型用逗号分隔
     * @return 车票信息的JSON字符串
     */
    @Tool(description = "查询两站之间的车票信息，支持按车型过滤")
    public String getTickets(
            @ToolParam(description = "查询日期，格式为yyyy-MM-dd") String date,
            @ToolParam(description = "出发站代码") String fromStation,
            @ToolParam(description = "到达站代码") String toStation,
            @ToolParam(description = "列车类型过滤，如G,D,K等，多种类型用逗号分隔，可为空") String trainTypes) {
        
        log.debug("开始查询车票信息: 日期={}, 出发站={}, 到达站={}, 车型过滤={}", date, fromStation, toStation, trainTypes);
        
        // 参数验证
        if (!DateUtils.isValidFutureDate(date)) {
            log.warn("无效的查询日期: {}", date);
            return "请提供有效的查询日期（今天或未来日期）";
        }
        
        if (!StringUtils.hasLength(fromStation) || !StringUtils.hasLength(toStation)) {
            log.warn("车站代码无效: 出发站={}, 到达站={}", fromStation, toStation);
            return "请提供有效的出发站和到达站代码";
        }
        
        try {
            log.info("通过前端服务查询车票信息: {} 从 {} 到 {}", date, fromStation, toStation);
            
            // 构建前端服务URL
            String frontendUrl = "http://localhost:3001/api/tickets";
            
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("date", date);
            params.put("fromStation", fromStation);
            params.put("toStation", toStation);
            if (StringUtils.hasLength(trainTypes)) {
                // 将逗号分隔的车型转换为字符串（如"G,D" -> "GD"）
                String trainFilterFlags = trainTypes.replaceAll(",", "").replaceAll(" ", "");
                params.put("trainFilterFlags", trainFilterFlags);
            }
            
            // 设置请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Accept", "application/json");
            
            // 发送请求到前端服务
            log.info("发送请求到前端服务: {}", frontendUrl);
            String response = HttpUtils.get(frontendUrl, params, headers);
            
            if (response == null) {
                log.error("前端服务返回为空");
                return "查询车票信息失败，前端服务无响应";
            }
            
            // 解析前端服务响应
            log.debug("解析前端服务响应");
            JSONObject jsonResponse = JSON.parseObject(response);
            if (jsonResponse == null) {
                log.error("前端服务响应解析失败: {}", response);
                return "查询车票信息失败，前端服务响应格式错误";
            }
            
            // 检查是否有错误
            if (jsonResponse.containsKey("error")) {
                String errorMsg = jsonResponse.getString("error");
                String message = jsonResponse.getString("message");
                log.error("前端服务返回错误: {} - {}", errorMsg, message);
                return "查询失败：" + (message != null ? message : errorMsg);
            }
            
            // 获取车票数据
            JSONArray ticketsArray = jsonResponse.getJSONArray("tickets");
            if (ticketsArray == null || ticketsArray.isEmpty()) {
                String message = jsonResponse.getString("message");
                log.info("查询结果为空: {} 从 {} 到 {}", date, fromStation, toStation);
                return message != null ? message : "没有找到符合条件的车票信息";
            }
            
            log.info("查询到 {} 条车票信息", ticketsArray.size());
            
            // 格式化车票信息为用户友好的格式
            String result = formatTicketsFromJson(ticketsArray);
            log.info("查询完成，返回 {} 条车票信息", ticketsArray.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询车票信息异常: {}", e.getMessage(), e);
            return "查询车票信息出错：" + e.getMessage();
        }
    }
    
    /**
     * 格式化从前端服务返回的车票JSON数据
     *
     * @param ticketsArray 车票JSON数组
     * @return 格式化后的车票信息字符串
     */
    private String formatTicketsFromJson(JSONArray ticketsArray) {
        if (ticketsArray == null || ticketsArray.isEmpty()) {
            return "没有找到符合条件的车票信息";
        }
        
        StringBuilder result = new StringBuilder();
        result.append("车次信息查询结果：\n\n");
        
        for (int i = 0; i < ticketsArray.size(); i++) {
            JSONObject ticket = ticketsArray.getJSONObject(i);
            
            result.append(String.format("车次：%s\n", ticket.getString("startTrainCode")));
            result.append(String.format("出发站：%s -> 到达站：%s\n", 
                ticket.getString("fromStation"), ticket.getString("toStation")));
            result.append(String.format("出发时间：%s -> 到达时间：%s\n", 
                ticket.getString("startTime"), ticket.getString("arriveTime")));
            result.append(String.format("历时：%s\n", ticket.getString("lishi")));
            
            // 座位信息
            JSONArray prices = ticket.getJSONArray("prices");
            if (prices != null && !prices.isEmpty()) {
                result.append("座位信息：\n");
                for (int j = 0; j < prices.size(); j++) {
                    JSONObject price = prices.getJSONObject(j);
                    String seatName = price.getString("seatName");
                    String num = price.getString("num");
                    String numDisplay = num.matches("\\d+") ? num + "张" : num;
                    result.append(String.format("  - %s：%s剩余\n", seatName, numDisplay));
                }
            }
            
            // 服务标识
            JSONArray dwFlags = ticket.getJSONArray("dwFlag");
            if (dwFlags != null && !dwFlags.isEmpty()) {
                result.append("服务标识：");
                for (int j = 0; j < dwFlags.size(); j++) {
                    if (j > 0) result.append("、");
                    result.append(dwFlags.getString(j));
                }
                result.append("\n");
            }
            
            result.append("\n");
        }
        
        return result.toString();
    }
}
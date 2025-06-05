package com.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 格式化后的车票信息
 * 用于向客户端展示的车票数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfo {
    /**
     * 车次号
     */
    private String trainNo;
    
    /**
     * 车次代码
     */
    private String startTrainCode;
    
    /**
     * 发车时间
     */
    private String startTime;
    
    /**
     * 到达时间
     */
    private String arriveTime;
    
    /**
     * 历时
     */
    private String lishi;
    
    /**
     * 出发站
     */
    private String fromStation;
    
    /**
     * 到达站
     */
    private String toStation;
    
    /**
     * 出发站电报码
     */
    private String fromStationTelecode;
    
    /**
     * 到达站电报码
     */
    private String toStationTelecode;
    
    /**
     * 票价信息
     */
    private List<Price> prices;
    
    /**
     * 服务标识列表
     */
    private List<String> dwFlag;
} 
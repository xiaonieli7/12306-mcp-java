package com.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 中转路线信息模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterlineInfo {
    /**
     * 总历时
     */
    private String allLishi;
    
    /**
     * 出发时间
     */
    private String startTime;
    
    /**
     * 出发日期
     */
    private String startDate;
    
    /**
     * 中转日期
     */
    private String middleDate;
    
    /**
     * 到达日期
     */
    private String arriveDate;
    
    /**
     * 到达时间
     */
    private String arriveTime;
    
    /**
     * 出发站代码
     */
    private String fromStationCode;
    
    /**
     * 出发站名称
     */
    private String fromStationName;
    
    /**
     * 中转站代码
     */
    private String middleStationCode;
    
    /**
     * 中转站名称
     */
    private String middleStationName;
    
    /**
     * 终点站代码
     */
    private String endStationCode;
    
    /**
     * 终点站名称
     */
    private String endStationName;
    
    /**
     * 第一段车次代码（用于过滤）
     */
    private String startTrainCode;
    
    /**
     * 第一段车次号
     */
    private String firstTrainNo;
    
    /**
     * 第二段车次号
     */
    private String secondTrainNo;
    
    /**
     * 车次数量
     */
    private Integer trainCount;
    
    /**
     * 票务信息列表
     */
    private List<TicketInfo> ticketList;
    
    /**
     * 是否同站台换乘
     */
    private Boolean sameStation;
    
    /**
     * 等待时间
     */
    private String waitTime;
    
    /**
     * 出发站（简化名称）
     */
    private String fromStation;
    
    /**
     * 到达站（简化名称）
     */
    private String toStation;
    
    /**
     * a中转站（简化名称）
     */
    private String middleStation;
    
    /**
     * 第一程车次
     */
    private String firstTrain;
    
    /**
     * 第二程车次
     */
    private String secondTrain;
    
    /**
     * 总用时
     */
    private String totalTime;
    
    /**
     * 车票信息
     */
    private List<TicketInfo> ticketsInfo;
    
    /**
     * 服务标识列表
     */
    private List<String> dwFlags;
} 
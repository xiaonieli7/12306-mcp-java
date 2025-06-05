package com.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 路线站点数据模型
 * 表示列车经过的站点信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStationData {
    /**
     * 到达时间
     */
    private String arriveTime;
    
    /**
     * 站点名称
     */
    private String stationName;
    
    /**
     * 是否在中国境内
     */
    private String isChina;
    
    /**
     * 发车时间
     */
    private String startTime;
    
    /**
     * 停留时间
     */
    private String stopoverTime;
    
    /**
     * 站点序号
     */
    private Integer stationNo;
    
    /**
     * 国家代码
     */
    private String countryCode;
    
    /**
     * 国家名称
     */
    private String countryName;
    
    /**
     * 是否启用
     */
    private Boolean isEnabled;
    
    /**
     * 列车类型名称
     */
    private String trainClassName;
    
    /**
     * 服务类型
     */
    private String serviceType;
    
    /**
     * 终点站名称
     */
    private String endStationName;
    
    /**
     * 始发站名称
     */
    private String startStationName;
    
    /**
     * 车次代码
     */
    private String stationTrainCode;
} 
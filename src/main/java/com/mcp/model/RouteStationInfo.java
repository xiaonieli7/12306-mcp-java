package com.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 格式化后的路线站点信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteStationInfo {
    /**
     * 到达时间
     */
    private String arriveTime;
    
    /**
     * 站点名称
     */
    private String stationName;
    
    /**
     * 停留时间
     */
    private String stopoverTime;
    
    /**
     * 站点序号
     */
    private Integer stationNo;
} 
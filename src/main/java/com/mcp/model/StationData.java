package com.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车站数据模型
 * 对应12306的车站信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StationData {
    /**
     * 车站ID
     */
    private String stationId;

    /**
     * 车站名称
     */
    private String stationName;

    /**
     * 车站代码
     */
    private String stationCode;

    /**
     * 车站拼音
     */
    private String stationPinyin;

    /**
     * 车站拼音简写
     */
    private String stationShort;

    /**
     * 车站索引
     */
    private String stationIndex;

    /**
     * 车站编码
     */
    private String code;

    /**
     * 所在城市索引
     */
    private String cityIndex;

    /**
     * 所在城市
     */
    private String city;

    /**
     * 预留字段1
     */
    private String r1;

    /**
     * 预留字段2
     */
    private String r2;
} 
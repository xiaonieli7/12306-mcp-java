package com.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 票价信息模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {
    /**
     * 座位名称
     */
    private String seatName;
    
    /**
     * 简称
     */
    private String shortName;
    
    /**
     * 座位类型代码
     */
    private String seatTypeCode;
    
    /**
     * 余票数量
     */
    private String num;
    
    /**
     * 价格
     */
    private Double price;
    
    /**
     * 折扣
     */
    private Double discount;
} 
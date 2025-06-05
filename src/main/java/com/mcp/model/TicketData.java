package com.mcp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 车票数据模型
 * 对应12306查询结果的车票信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketData {
    /**
     * 密钥字符串
     */
    private String secretStr;
    
    /**
     * 按钮文本信息
     */
    private String buttonTextInfo;
    
    /**
     * 车次号
     */
    private String trainNo;
    
    /**
     * 车次代码
     */
    private String stationTrainCode;
    
    /**
     * 始发站电报码
     */
    private String startStationTelecode;
    
    /**
     * 终点站电报码
     */
    private String endStationTelecode;
    
    /**
     * 出发站电报码
     */
    private String fromStationTelecode;
    
    /**
     * 到达站电报码
     */
    private String toStationTelecode;
    
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
     * 能否网上购买
     */
    private String canWebBuy;
    
    /**
     * 余票信息
     */
    private String ypInfo;
    
    /**
     * 发车日期
     */
    private String startTrainDate;
    
    /**
     * 座位特性
     */
    private String trainSeatFeature;
    
    /**
     * 位置代码
     */
    private String locationCode;
    
    /**
     * 出发站序号
     */
    private String fromStationNo;
    
    /**
     * 到达站序号
     */
    private String toStationNo;
    
    /**
     * 是否支持卡
     */
    private String isSupportCard;
    
    /**
     * 受控列车标志
     */
    private String controlledTrainFlag;
    
    // 各席别余票数量
    private String ggNum;    // 其他
    private String grNum;    // 高级软卧
    private String qtNum;    // 其他
    private String rwNum;    // 软卧
    private String rzNum;    // 软座
    private String tzNum;    // 特等座
    private String wzNum;    // 无座
    private String ybNum;    // 硬板
    private String ywNum;    // 硬卧
    private String yzNum;    // 硬座
    private String zeNum;    // 二等座
    private String zyNum;    // 一等座
    private String swzNum;   // 商务座
    private String srrbNum;  // 动卧
    
    /**
     * 余票扩展信息
     */
    private String ypEx;
    
    /**
     * 座位类型
     */
    private String seatTypes;
    
    /**
     * 交换列车标志
     */
    private String exchangeTrainFlag;
    
    /**
     * 候补列车标志
     */
    private String houbuTrainFlag;
    
    /**
     * 候补座位限制
     */
    private String houbuSeatLimit;
    
    /**
     * 新的余票信息
     */
    private String ypInfoNew;
    
    /**
     * 服务标识
     */
    private String dwFlag;
    
    /**
     * 停靠检查时间
     */
    private String stopcheckTime;
    
    /**
     * 国家标志
     */
    private String countryFlag;
    
    /**
     * 本地到达时间
     */
    private String localArriveTime;
    
    /**
     * 本地出发时间
     */
    private String localStartTime;
    
    /**
     * 卧铺等级信息
     */
    private String bedLevelInfo;
    
    /**
     * 座位折扣信息
     */
    private String seatDiscountInfo;
    
    /**
     * 销售时间
     */
    private String saleTime;
} 
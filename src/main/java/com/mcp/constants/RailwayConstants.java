package com.mcp.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 铁路相关常量
 */
public class RailwayConstants {
    /**
     * 12306 API 基础地址
     */
    public static final String API_BASE = "https://kyfw.12306.cn";
    
    /**
     * 12306 网站地址
     */
    public static final String WEB_URL = "https://www.12306.cn/index/";
    
    /**
     * 座位类型简称映射
     */
    public static final Map<String, String> SEAT_SHORT_TYPES = new HashMap<>();
    
    /**
     * 座位类型代码映射
     */
    public static final Map<String, Map<String, String>> SEAT_TYPES = new HashMap<>();
    
    /**
     * 列车服务标识
     */
    public static final String[] DW_FLAGS = {
        "智能动车组", "复兴号", "静音车厢", "温馨动卧", "动感号", "支持选铺", "老年优惠"
    };
    
    // 初始化座位类型简称映射
    static {
        SEAT_SHORT_TYPES.put("swz", "商务座");
        SEAT_SHORT_TYPES.put("tz", "特等座");
        SEAT_SHORT_TYPES.put("zy", "一等座");
        SEAT_SHORT_TYPES.put("ze", "二等座");
        SEAT_SHORT_TYPES.put("gr", "高软卧");
        SEAT_SHORT_TYPES.put("srrb", "动卧");
        SEAT_SHORT_TYPES.put("rw", "软卧");
        SEAT_SHORT_TYPES.put("yw", "硬卧");
        SEAT_SHORT_TYPES.put("rz", "软座");
        SEAT_SHORT_TYPES.put("yz", "硬座");
        SEAT_SHORT_TYPES.put("wz", "无座");
        SEAT_SHORT_TYPES.put("qt", "其他");
        SEAT_SHORT_TYPES.put("gg", "");
        SEAT_SHORT_TYPES.put("yb", "");
    }
    
    // 初始化座位类型代码映射
    static {
        // 商务座
        Map<String, String> swz = new HashMap<>();
        swz.put("name", "商务座");
        swz.put("short", "swz");
        SEAT_TYPES.put("9", swz);
        
        // 特等座
        Map<String, String> tz = new HashMap<>();
        tz.put("name", "特等座");
        tz.put("short", "tz");
        SEAT_TYPES.put("P", tz);
        
        // 一等座
        Map<String, String> zy = new HashMap<>();
        zy.put("name", "一等座");
        zy.put("short", "zy");
        SEAT_TYPES.put("M", zy);
        
        // 优选一等座
        Map<String, String> zyy = new HashMap<>();
        zyy.put("name", "优选一等座");
        zyy.put("short", "zy");
        SEAT_TYPES.put("D", zyy);
        
        // 二等座
        Map<String, String> ze = new HashMap<>();
        ze.put("name", "二等座");
        ze.put("short", "ze");
        SEAT_TYPES.put("O", ze);
        
        // 二等包座
        Map<String, String> zeb = new HashMap<>();
        zeb.put("name", "二等包座");
        zeb.put("short", "ze");
        SEAT_TYPES.put("S", zeb);
        
        // 高级软卧
        Map<String, String> gr = new HashMap<>();
        gr.put("name", "高级软卧");
        gr.put("short", "gr");
        SEAT_TYPES.put("6", gr);
        
        // 高级动卧
        Map<String, String> grd = new HashMap<>();
        grd.put("name", "高级动卧");
        grd.put("short", "gr");
        SEAT_TYPES.put("A", grd);
        
        // 软卧
        Map<String, String> rw = new HashMap<>();
        rw.put("name", "软卧");
        rw.put("short", "rw");
        SEAT_TYPES.put("4", rw);
        
        // 一等卧
        Map<String, String> rwyd = new HashMap<>();
        rwyd.put("name", "一等卧");
        rwyd.put("short", "rw");
        SEAT_TYPES.put("I", rwyd);
        
        // 动卧
        Map<String, String> dw = new HashMap<>();
        dw.put("name", "动卧");
        dw.put("short", "rw");
        SEAT_TYPES.put("F", dw);
        
        // 硬卧
        Map<String, String> yw = new HashMap<>();
        yw.put("name", "硬卧");
        yw.put("short", "yw");
        SEAT_TYPES.put("3", yw);
        
        // 二等卧
        Map<String, String> ywed = new HashMap<>();
        ywed.put("name", "二等卧");
        ywed.put("short", "yw");
        SEAT_TYPES.put("J", ywed);
        
        // 软座
        Map<String, String> rz = new HashMap<>();
        rz.put("name", "软座");
        rz.put("short", "rz");
        SEAT_TYPES.put("2", rz);
        
        // 硬座
        Map<String, String> yz = new HashMap<>();
        yz.put("name", "硬座");
        yz.put("short", "yz");
        SEAT_TYPES.put("1", yz);
        
        // 无座
        Map<String, String> wz = new HashMap<>();
        wz.put("name", "无座");
        wz.put("short", "wz");
        SEAT_TYPES.put("W", wz);
        SEAT_TYPES.put("WZ", wz);
        
        // 其他
        Map<String, String> qt = new HashMap<>();
        qt.put("name", "其他");
        qt.put("short", "qt");
        SEAT_TYPES.put("H", qt);
    }
} 
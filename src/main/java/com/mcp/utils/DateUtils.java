package com.mcp.utils;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtils {
    /**
     * 日期格式：yyyy-MM-dd
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    
    /**
     * 时间格式：HH:mm
     */
    private static final String TIME_PATTERN = "HH:mm";
    
    /**
     * 获取当前日期的字符串表示
     *
     * @return 格式为 yyyy-MM-dd 的日期字符串
     */
    public static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        return dateFormat.format(new Date());
    }
    
    /**
     * 获取当前时间的字符串表示
     *
     * @return 格式为 HH:mm 的时间字符串
     */
    public static String getCurrentTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_PATTERN);
        return timeFormat.format(new Date());
    }
    
    /**
     * 检查日期格式是否有效
     *
     * @param dateStr 日期字符串
     * @return 是否有效
     */
    public static boolean isValidDate(String dateStr) {
        if (!StringUtils.hasLength(dateStr)) {
            return false;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * 验证日期字符串是否在今天及之后
     *
     * @param dateStr 日期字符串，格式为 yyyy-MM-dd
     * @return 是否有效
     */
    public static boolean isValidFutureDate(String dateStr) {
        if (!isValidDate(dateStr)) {
            return false;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            LocalDate date = LocalDate.parse(dateStr, formatter);
            LocalDate today = LocalDate.now();
            
            // 检查日期是否是今天或之后的日期
            return !date.isBefore(today);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 格式化日期
     *
     * @param dateStr 日期字符串
     * @return 格式化后的日期字符串
     */
    public static String formatDate(String dateStr) {
        if (!StringUtils.hasLength(dateStr)) {
            return "";
        }
        
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            LocalDate date = LocalDate.parse(dateStr, inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            return date.format(outputFormatter);
        } catch (DateTimeParseException e) {
            return dateStr;
        }
    }
} 
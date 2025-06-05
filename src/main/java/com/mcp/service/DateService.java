package com.mcp.service;

import com.mcp.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * 日期服务
 * 提供日期相关的工具方法
 */
@Slf4j
@Service
public class DateService {
    
    /**
     * 获取当前日期
     *
     * @return 当前日期，格式为 yyyy-MM-dd
     */
    @Tool(description = "获取当前日期，返回格式为 yyyy-MM-dd 的日期字符串")
    public String getCurrentDate() {
        log.debug("获取当前日期");
        String currentDate = DateUtils.getCurrentDate();
        log.info("当前日期: {}", currentDate);
        return currentDate;
    }
    
    /**
     * 验证日期字符串是否有效
     *
     * @param dateStr 日期字符串，格式为 yyyy-MM-dd
     * @return 日期是否有效
     */
    @Tool(description = "验证日期字符串是否有效，返回布尔值")
    public boolean isValidDate(@ToolParam(description = "日期字符串，格式为 yyyy-MM-dd") String dateStr) {
        log.debug("验证日期是否有效: {}", dateStr);
        boolean isValid = DateUtils.isValidDate(dateStr);
        if (!isValid) {
            log.warn("无效的日期格式: {}", dateStr);
        }
        return isValid;
    }
    
    /**
     * 验证日期字符串是否在今天及之后
     *
     * @param dateStr 日期字符串，格式为 yyyy-MM-dd
     * @return 日期是否有效且是今天或之后
     */
    @Tool(description = "验证日期字符串是否在今天及之后，返回布尔值")
    public boolean isValidFutureDate(@ToolParam(description = "日期字符串，格式为 yyyy-MM-dd") String dateStr) {
        log.debug("验证日期是否为今天或未来日期: {}", dateStr);
        boolean isValidFuture = DateUtils.isValidFutureDate(dateStr);
        if (!isValidFuture) {
            log.warn("日期不是今天或未来日期: {}", dateStr);
        }
        return isValidFuture;
    }
    
    /**
     * 格式化日期
     *
     * @param dateStr 日期字符串
     * @return 格式化后的日期字符串
     */
    @Tool(description = "格式化日期字符串为 yyyy-MM-dd 格式")
    public String formatDate(@ToolParam(description = "待格式化的日期字符串") String dateStr) {
        log.debug("格式化日期: {}", dateStr);
        try {
            String formattedDate = DateUtils.formatDate(dateStr);
            log.info("日期格式化结果: {} -> {}", dateStr, formattedDate);
            return formattedDate;
        } catch (Exception e) {
            log.error("日期格式化异常: {}", e.getMessage(), e);
            return null;
        }
    }
} 
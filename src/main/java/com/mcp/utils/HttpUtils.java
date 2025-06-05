package com.mcp.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求工具类
 */
@Slf4j
public class HttpUtils {
    /**
     * 发送GET请求
     *
     * @param url     请求URL
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String get(String url, Map<String, Object> params, Map<String, String> headers) {
        try {
            HttpRequest request = HttpRequest.get(url);
            
            // 设置请求参数
            if (params != null && !params.isEmpty()) {
                request.form(params);
            }
            
            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::header);
            }
            
            // 发送请求并获取响应
            HttpResponse response = request.execute();
            return response.body();
        } catch (Exception e) {
            log.error("发送GET请求失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 发送POST请求
     *
     * @param url     请求URL
     * @param params  请求参数
     * @param headers 请求头
     * @return 响应内容
     */
    public static String post(String url, Map<String, Object> params, Map<String, String> headers) {
        try {
            HttpRequest request = HttpRequest.post(url);
            
            // 设置请求参数
            if (params != null && !params.isEmpty()) {
                request.form(params);
            }
            
            // 设置请求头
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(request::header);
            }
            
            // 发送请求并获取响应
            HttpResponse response = request.execute();
            return response.body();
        } catch (Exception e) {
            log.error("发送POST请求失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 解析Cookie字符串为Map
     *
     * @param cookies Cookie字符串列表
     * @return Cookie键值对
     */
    public static Map<String, String> parseCookies(List<String> cookies) {
        Map<String, String> cookieRecord = new HashMap<>();
        if (cookies == null || cookies.isEmpty()) {
            return cookieRecord;
        }
        
        for (String cookie : cookies) {
            if (!StringUtils.hasLength(cookie)) {
                continue;
            }
            
            // 提取键值对部分（去掉 Path、HttpOnly 等属性）
            String keyValuePart = cookie.split(";")[0];
            // 分割键和值
            String[] parts = keyValuePart.split("=");
            if (parts.length >= 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();
                cookieRecord.put(key, value);
            }
        }
        
        return cookieRecord;
    }
    
    /**
     * 将Cookie Map格式化为Cookie请求头字符串
     *
     * @param cookies Cookie键值对
     * @return Cookie请求头字符串
     */
    public static String formatCookies(Map<String, String> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return "";
        }
        
        StringBuilder cookieBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            cookieBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue())
                    .append("; ");
        }
        
        // 移除最后的分号和空格
        if (cookieBuilder.length() > 0) {
            cookieBuilder.setLength(cookieBuilder.length() - 2);
        }
        
        return cookieBuilder.toString();
    }
    
    /**
     * 解析响应的JSON为泛型对象
     *
     * @param response  响应字符串
     * @param classType 目标类型
     * @param <T>       泛型类型
     * @return 解析后的对象
     */
    public static <T> T parseJsonResponse(String response, Class<T> classType) {
        if (!StringUtils.hasLength(response)) {
            return null;
        }
        
        try {
            return JSON.parseObject(response, classType);
        } catch (Exception e) {
            log.error("解析JSON响应失败: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 获取初始Cookie
     *
     * @param url 请求URL
     * @return Cookie键值对
     */
    public static Map<String, String> getCookie(String url) {
        try {
            log.info("开始获取Cookie，URL: {}", url);
            
            // 使用API基础URL而不是WEB_URL
            String apiUrl = "https://kyfw.12306.cn";
            log.info("使用API URL获取Cookie: {}", apiUrl);
            
            HttpRequest request = HttpRequest.get(apiUrl);
            
            // 设置更完整的请求头，模拟真实浏览器
            request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            request.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            request.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            request.header("Accept-Encoding", "gzip, deflate, br");
            request.header("Connection", "keep-alive");
            request.header("Upgrade-Insecure-Requests", "1");
            request.timeout(15000); // 增加超时时间到15秒
            
            HttpResponse response = request.execute();
            log.info("HTTP响应状态码: {}", response.getStatus());
            
            if (response.getStatus() != 200) {
                log.error("HTTP请求失败，状态码: {}", response.getStatus());
                return new HashMap<>();
            }
            
            List<String> cookies = response.headerList("Set-Cookie");
            log.info("获取到的Set-Cookie头数量: {}", cookies != null ? cookies.size() : 0);
            
            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    log.debug("Set-Cookie: {}", cookie);
                }
            } else {
                log.warn("未获取到任何Cookie");
                return new HashMap<>();
            }
            
            Map<String, String> cookieMap = parseCookies(cookies);
            log.info("解析后的Cookie数量: {}", cookieMap.size());
            
            // 如果没有获取到Cookie，尝试备用方法
            if (cookieMap.isEmpty()) {
                log.warn("使用API URL未获取到Cookie，尝试使用WEB URL");
                return getCookieFromWebUrl(url);
            }
            
            return cookieMap;
        } catch (Exception e) {
            log.error("获取Cookie失败，URL: {}, 错误: {}", url, e.getMessage(), e);
            // 尝试备用方法
            return getCookieFromWebUrl(url);
        }
    }
    
    /**
     * 从WEB URL获取Cookie的备用方法
     */
    private static Map<String, String> getCookieFromWebUrl(String url) {
        try {
            log.info("尝试从WEB URL获取Cookie: {}", url);
            HttpRequest request = HttpRequest.get(url);
            
            request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            request.timeout(10000);
            
            HttpResponse response = request.execute();
            log.info("WEB URL HTTP响应状态码: {}", response.getStatus());
            
            List<String> cookies = response.headerList("Set-Cookie");
            Map<String, String> cookieMap = parseCookies(cookies);
            log.info("从WEB URL解析后的Cookie数量: {}", cookieMap.size());
            
            return cookieMap;
        } catch (Exception e) {
            log.error("从WEB URL获取Cookie也失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
} 
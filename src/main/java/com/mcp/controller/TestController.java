package com.mcp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于验证服务器状态
 */
@RestController
public class TestController {
    
    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "12306-MCP Server");
        response.put("status", "running");
        response.put("mcp_endpoint", "/see");
        response.put("description", "这是一个MCP服务器，需要通过MCP协议访问，不是普通的Web API");
        response.put("usage", "请使用支持MCP协议的客户端连接到 http://localhost:8080/see");
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "12306-MCP");
        return response;
    }
} 
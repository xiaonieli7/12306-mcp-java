package com.mcp.config;

import com.mcp.service.*;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP服务器配置类
 * 显式注册所有工具服务
 */
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(
            DateService dateService,
            StationService stationService,
            TicketService ticketService,
            RouteService routeService,
            InterlineService interlineService) {
        
        // 使用MethodToolCallbackProvider来注册@Tool方法
        return MethodToolCallbackProvider.builder()
                .toolObjects(dateService, stationService, ticketService, routeService, interlineService)
                .build();
    }
} 
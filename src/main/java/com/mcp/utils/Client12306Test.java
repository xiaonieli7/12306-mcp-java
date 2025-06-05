//package com.jcai.utils;
//import io.modelcontextprotocol.client.McpClient;
//import io.modelcontextprotocol.client.McpSchema;
//import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.Map;
//
///**
// * 12306-MCP客户端测试
// * @author jcai
// */
//public class Client12306Test {
//
//    public static void main(String[] args) {
//        // 注意这里使用的是8081端口，与您修改后的配置一致
//        var transport = new WebFluxSseClientTransport(WebClient.builder().baseUrl("http://localhost:8081/see"));
//        var client = McpClient.sync(transport).build();
//
//        try {
//            // 初始化连接
//            client.initialize();
//
//            // 发送ping测试连接
//            client.ping();
//            System.out.println("连接成功！");
//
//            // 列出并展示可用的工具
//            McpSchema.ListToolsResult toolsList = client.listTools();
//            System.out.println("可用工具列表 = " + toolsList);
//
//            // 获取车站信息测试
//            System.out.println("\n--- 测试车站信息查询 ---");
//            McpSchema.CallToolResult stationResult = client.callTool(new McpSchema.CallToolRequest(
//                    "getStationCodeByName",
//                    Map.of("stationName", "北京南")));
//            System.out.println("车站代码查询结果: " + stationResult.content());
//
//            // 获取当前日期
//            System.out.println("\n--- 测试日期服务 ---");
//            McpSchema.CallToolResult dateResult = client.callTool(new McpSchema.CallToolRequest(
//                    "getCurrentDate",
//                    Map.of()));
//            System.out.println("当前日期: " + dateResult.content());
//
//            // 查询车次信息
//            System.out.println("\n--- 测试车票查询 ---");
//            String currentDate = dateResult.content().toString().replace("\"", "");
//            McpSchema.CallToolResult ticketsResult = client.callTool(new McpSchema.CallToolRequest(
//                    "getTickets",
//                    Map.of(
//                            "date", currentDate,
//                            "fromStation", "VNP", // 北京南
//                            "toStation", "AOH",   // 上海虹桥
//                            "trainTypes", "G"     // 高铁
//                    )));
//            System.out.println("车票查询结果: " + ticketsResult.content());
//
//        } catch (Exception e) {
//            System.err.println("发生错误: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            // 优雅关闭连接
//            client.closeGracefully();
//            System.out.println("连接已关闭");
//        }
//    }
//}
# 12306-MCP

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0--M6-green.svg)](https://spring.io/projects/spring-ai)
[![MCP](https://img.shields.io/badge/MCP-Protocol-purple.svg)](https://modelcontextprotocol.io/)

基于 Spring Boot 和 Spring AI 实现的 12306 列车信息查询服务，通过 MCP (Model Context Protocol) 协议提供与大语言模型交互的能力。

## 功能特点

- **车站信息查询**: 查询城市车站、站点代码等
- **车票信息查询**: 查询直达车票信息，支持按车型筛选
- **车次路线查询**: 查询车次完整路线和停靠站信息
- **中转查询**: 查询两站之间的中转乘车方案
- **车次筛选**: 支持按车型筛选（高铁、动车、特快等）
- **MCP 协议支持**: 允许大语言模型通过标准协议调用服务功能

## 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本
- Spring Boot 3.4.3 或更高版本
- Spring AI 1.0.0-M6 或更高版本
- 网络连接（用于访问12306 API）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/xiaonieli7/12306-mcp-java.git
cd 12306-mcp
```

### 2. 配置参数

项目默认配置已在 `src/main/resources/application.yml` 中设置：

```yaml
server:
  port: 8080

spring:
  application:
    name: 12306-mcp-java
  ai:
    mcp-server:
      endpoint: /see
      # 可选配置，默认情况下所有@Tool注解的方法都会作为工具被自动注册
      tools:
        include:
          packages: com.mcp

logging:
  level:
    org.springframework.ai: INFO
    com.mcp: DEBUG
```

### 3. 构建项目

```bash
mvn clean package
```

### 4. 运行应用

```bash
java -jar target/jcai-0.0.1-SNAPSHOT.jar
```

服务启动后将在控制台显示启动信息和注册的工具列表。

## MCP 服务配置

本项目支持 MCP (Model Context Protocol) 协议，使大语言模型能够直接调用服务功能。

大模型可以通过以下配置访问：

```json
{
  "mcpServers": {
    "12306-mcp": {
      "url": "http://localhost:8080/see"
    }
  }
}
```

## 项目结构

```
src/main/java/com/jcai/
├── Application.java           # 应用程序入口类
├── constants/
│   └── RailwayConstants.java  # 铁路相关常量
├── model/
│   ├── InterlineInfo.java     # 中转路线信息模型
│   ├── Price.java             # 票价信息模型
│   ├── RouteStationData.java  # 路线站点数据模型
│   ├── RouteStationInfo.java  # 格式化后的路线站点信息
│   ├── StationData.java       # 车站数据模型
│   ├── TicketData.java        # 车票数据模型
│   └── TicketInfo.java        # 格式化后的车票信息
├── service/
│   ├── DateService.java       # 日期服务
│   ├── InterlineService.java  # 中转查询服务
│   ├── RouteService.java      # 路线查询服务
│   ├── StationService.java    # 车站查询服务
│   └── TicketService.java     # 车票查询服务
└── utils/
    ├── DateUtils.java         # 日期工具类
    └── HttpUtils.java         # HTTP请求工具类
```

## API 接口说明

本项目通过 MCP 协议暴露以下工具函数：

### 1. 日期服务 (DateService)

| 接口名称 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| getCurrentDate | 获取当前日期 | 无 | 当前日期字符串(yyyy-MM-dd) |
| isValidDate | 验证日期字符串是否有效 | dateStr: 日期字符串 | 布尔值 |
| isValidFutureDate | 验证日期是否是当前或未来日期 | dateStr: 日期字符串 | 布尔值 |
| formatDate | 格式化日期字符串 | dateStr: 日期字符串 | 格式化后的日期字符串 |

### 2. 车站服务 (StationService)

| 接口名称 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| getStationsCodeInCity | 获取指定城市的所有车站信息 | cityName: 城市名称 | 城市车站信息的JSON字符串 |
| getStationCodeByName | 根据车站名称获取车站代码 | stationName: 车站名称 | 车站代码信息的JSON字符串 |
| getStationCodeByNames | 根据多个车站名称获取车站代码 | stationNames: 多个车站名称(逗号分隔) | 多个车站代码信息的JSON字符串 |
| getStationNameByCode | 根据车站代码获取车站名称 | stationCode: 车站代码 | 车站名称 |
| searchStations | 根据关键词模糊查询车站 | keyword: 关键词 | 匹配的车站列表JSON字符串 |
| getTotalStationCount | 获取系统中所有车站的总数 | 无 | 车站总数 |

### 3. 车票服务 (TicketService)

| 接口名称 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| getTickets | 查询两站之间的车票信息 | date: 日期<br>fromStation: 出发站代码<br>toStation: 到达站代码<br>trainTypes: 车型过滤(可选) | 车票信息的JSON字符串 |

### 4. 路线服务 (RouteService)

| 接口名称 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| getTrainRoute | 查询列车的完整路线信息 | date: 日期<br>trainCode: 车次代码<br>fromStation: 出发站代码<br>toStation: 到达站代码 | 列车路线信息的JSON字符串 |

### 5. 中转服务 (InterlineService)

| 接口名称 | 描述 | 参数 | 返回值 |
|---------|------|------|--------|
| getInterlineRoutes | 查询两站之间的中转路线方案 | date: 日期<br>fromStation: 出发站代码<br>toStation: 到达站代码<br>trainTypes: 车型过滤(可选) | 中转路线信息的JSON字符串 |

## 示例

### 查询车站代码

```json
// 请求参数
{
  "stationName": "北京南"
}

// 返回结果
{
  "北京南": {
    "station_code": "VNP",
    "station_name": "北京南"
  }
}
```

### 查询车票信息

```json
// 请求参数
{
  "date": "2023-05-01",
  "fromStation": "VNP",
  "toStation": "AOH",
  "trainTypes": "G"
}

// 返回结果（简化版）
[
  {
    "trainNo": "24000000G10Q",
    "startTrainCode": "G1",
    "startTime": "06:36",
    "arriveTime": "12:38",
    "lishi": "06:02",
    "fromStation": "北京南",
    "toStation": "上海虹桥",
    "fromStationTelecode": "VNP",
    "toStationTelecode": "AOH",
    "prices": [
      {
        "seatName": "商务座",
        "shortName": "swz",
        "seatTypeCode": "9",
        "num": "有"
      },
      {
        "seatName": "一等座",
        "shortName": "zy",
        "seatTypeCode": "M",
        "num": "有"
      }
    ],
    "dwFlag": ["复兴号"]
  }
]
```

### 查询车次路线

```json
// 请求参数
{
  "date": "2023-05-01",
  "trainCode": "G1",
  "fromStation": "VNP",
  "toStation": "AOH"
}

// 返回结果（简化版）
[
  {
    "arriveTime": "06:36",
    "stationName": "北京南",
    "stopoverTime": "3分钟",
    "stationNo": 1
  },
  {
    "arriveTime": "08:15",
    "stationName": "济南西",
    "stopoverTime": "2分钟",
    "stationNo": 2
  },
  {
    "arriveTime": "12:38",
    "stationName": "上海虹桥",
    "stopoverTime": "终点站",
    "stationNo": 3
  }
]
```

### 查询中转路线

```json
// 请求参数
{
  "date": "2023-05-01",
  "fromStation": "ZDW",
  "toStation": "ZYN",
  "trainTypes": "G"
}

// 返回结果（简化版）
[
  {
    "from_station": "成都东",
    "middle_station": "西安北",
    "to_station": "郑州",
    "first_train": "G89",
    "second_train": "G507",
    "start_time": "08:45",
    "arrive_time": "16:58",
    "total_time": "08:13",
    "wait_time": "00:45",
    "same_station": true
  }
]
```

## 与大语言模型集成

本项目基于Spring AI的MCP协议与大语言模型集成，使模型可以调用服务提供的功能：

1. 模型可通过MCP协议发现所有可用工具
2. 根据用户自然语言请求选择合适的工具
3. 调用工具并获取结果
4. 将结果格式化返回给用户

示例对话：
- 用户：明天从北京到上海的高铁票价是多少？
- 模型：（通过MCP调用工具后）明天从北京到上海的高铁票价为二等座553元起，一等座933元起。最早一班G1次列车06:36从北京南站出发，12:38到达上海虹桥站。

## 支持的列车类型筛选

以下列车类型可用于车票查询和中转查询的筛选：

- `G` - 高铁/城际（G/C开头车次）
- `D` - 动车（D开头车次）
- `Z` - 直达特快（Z开头车次）
- `T` - 特快（T开头车次）
- `K` - 快速（K开头车次）
- `F` - 复兴号（含"复兴号"标识的车次）
- `S` - 智能动车组（含"智能动车组"标识的车次）
- `O` - 其他（非上述类型的车次）

## 技术实现

- 使用 Hutool HTTP 和 OkHttp 客户端访问12306 API
- 使用 FastJSON 进行JSON数据解析和生成
- 基于 Spring AI 的 MCP 协议实现工具暴露
- 采用 Spring Boot 作为应用框架
- 使用 Lombok 简化代码




## 贡献指南

欢迎贡献代码、报告问题或提出新功能建议。提交PR前，请确保遵循以下准则：

1. 代码风格与项目保持一致
2. 添加适当的测试
3. 更新文档，说明新增功能

## 致谢

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring AI](https://spring.io/projects/spring-ai)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Hutool](https://hutool.cn/)
- [FastJSON](https://github.com/alibaba/fastjson)
- [Lombok](https://projectlombok.org/)

## 日志管理

本项目使用SLF4J和Logback实现日志管理，提供了多级别的日志记录：

- **ERROR**：记录严重错误，如API请求失败、数据解析异常等
- **WARN**：记录警告信息，如参数验证失败、未找到匹配数据等
- **INFO**：记录关键业务流程，如查询开始、完成、结果统计等
- **DEBUG**：记录详细操作步骤，如参数构建、API请求、数据解析等
- **TRACE**：记录最详细的调试信息，通常用于开发环境

### 日志配置

日志级别可在`application.yml`中配置：

```yaml
logging:
  level:
    org.springframework.ai: INFO
    com.mcp: DEBUG  # 可修改为INFO, WARN, ERROR等
```

### 日志示例

```
2023-05-01 12:34:56.789 INFO  [main] c.j.s.StationService - 车站数据加载完成，共 2574 个车站
2023-05-01 12:35:01.234 INFO  [http-nio-8080-exec-1] c.j.s.TicketService - 查询车票信息: 2023-05-03 从 VNP 到 AOH
2023-05-01 12:35:03.456 INFO  [http-nio-8080-exec-1] c.j.s.TicketService - 查询到 35 条车票信息
2023-05-01 12:35:03.567 INFO  [http-nio-8080-exec-1] c.j.s.TicketService - 查询完成，返回 25 条车票信息
```

### 日志文件

日志文件默认保存在应用运行目录下的`logs/`目录中：

- `12306-mcp.log` - 当前日志文件
- `12306-mcp.log.yyyy-MM-dd` - 历史日志文件

可以通过在`application.yml`中添加以下配置修改日志文件位置和滚动策略：

```yaml
logging:
  file:
    name: /path/to/your/logs/12306-mcp.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30
```

本项目仅用于学习

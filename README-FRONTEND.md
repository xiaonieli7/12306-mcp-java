# 12306 MCP 前后端分离架构

## 架构说明

为了解决12306 API的复杂认证和Cookie管理问题，我们采用了前后端分离的架构：

- **前端服务 (Node.js)**: 负责直接与12306 API通信，处理Cookie获取、重定向等复杂逻辑
- **后端服务 (Java Spring Boot)**: 提供MCP工具接口，通过HTTP调用前端服务获取数据

## 目录结构

```
jcai/
├── frontend/                 # Node.js前端服务
│   ├── package.json         # 前端依赖配置
│   └── server.js           # Express服务器
├── src/                     # Java后端源码
├── start-services.bat       # 一键启动脚本
└── README-FRONTEND.md       # 本文档
```

## 快速启动

### 方法一：使用一键启动脚本（推荐）

```bash
# Windows
start-services.bat
```

### 方法二：手动启动

1. **启动前端服务**
```bash
cd frontend
npm install
npm start
```
前端服务将在 http://localhost:3001 启动

2. **启动后端服务**
```bash
cd ..
mvn clean compile
mvn spring-boot:run
```
后端服务将在 http://localhost:8080 启动

## API接口

### 前端服务 (端口3001)

- `GET /health` - 健康检查
- `GET /api/current-date` - 获取当前日期
- `GET /api/tickets` - 查询车票信息
  - 参数：
    - `date`: 查询日期 (YYYY-MM-DD)
    - `fromStation`: 出发站代码
    - `toStation`: 到达站代码
    - `trainFilterFlags`: 车型过滤 (可选，如"G"表示高铁)

### 后端服务 (端口8080)

- Spring AI MCP工具接口
- 自动注册的工具：
  - `getTickets`: 查询车票信息
  - `getStations`: 查询车站信息
  - `getRoutes`: 查询路线信息
  - 等等...

## 测试

### 测试前端服务

```bash
# 获取当前日期
curl http://localhost:3001/api/current-date

# 查询车票（北京到上海的高铁）
curl "http://localhost:3001/api/tickets?date=2025-06-04&fromStation=BJP&toStation=SHH&trainFilterFlags=G"
```

### 测试后端服务

后端服务启动后，MCP工具会自动注册，可以通过支持MCP的客户端进行调用。

## 优势

1. **分离关注点**: 前端专注于12306 API通信，后端专注于MCP工具逻辑
2. **更好的错误处理**: Node.js对HTTP请求和Cookie处理更加灵活
3. **易于调试**: 可以独立测试前端API
4. **可扩展性**: 可以轻松添加更多的12306 API功能

## 故障排除

### 前端服务无法启动
- 检查Node.js是否已安装
- 检查端口3001是否被占用
- 运行 `npm install` 确保依赖已安装

### 后端服务无法连接前端
- 确保前端服务已启动并运行在端口3001
- 检查防火墙设置
- 查看后端日志中的错误信息

### 12306 API请求失败
- 12306可能有反爬虫机制，这是正常现象
- 可以尝试重启前端服务
- 检查网络连接

## 日志

- 前端服务日志：在启动前端服务的控制台窗口中查看
- 后端服务日志：在启动后端服务的控制台窗口中查看

## 开发

如果需要修改代码：

1. 修改前端代码后，重启前端服务
2. 修改后端代码后，重新编译并重启后端服务

```bash
# 重启前端
cd frontend
npm start

# 重启后端
mvn clean compile
mvn spring-boot:run
``` 
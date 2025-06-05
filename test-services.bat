@echo off
echo ========================================
echo 测试 12306 MCP 服务
echo ========================================

echo.
echo 1. 测试前端服务健康检查...
curl -s http://localhost:3001/health
if %errorlevel% neq 0 (
    echo [错误] 前端服务未启动或无法访问
    echo 请先运行 start-services.bat 启动服务
    pause
    exit /b 1
)

echo.
echo 2. 测试获取当前日期...
curl -s http://localhost:3001/api/current-date

echo.
echo 3. 测试车票查询（北京到上海）...
curl -s "http://localhost:3001/api/tickets?date=2025-06-04&fromStation=BJP&toStation=SHH&trainFilterFlags=G"

echo.
echo 4. 测试后端服务健康检查...
curl -s http://localhost:8080/actuator/health
if %errorlevel% neq 0 (
    echo [警告] 后端服务可能未启动或无法访问
)

echo.
echo ========================================
echo 测试完成
echo ========================================
echo.
echo 如果看到正常的JSON响应，说明服务运行正常
echo 如果出现错误，请检查：
echo 1. 服务是否已启动 (运行 start-services.bat)
echo 2. 端口是否被占用
echo 3. 网络连接是否正常
echo.
pause 
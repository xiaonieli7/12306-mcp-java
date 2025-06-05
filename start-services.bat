@echo off
echo 启动12306 MCP服务...

echo.
echo 1. 安装前端依赖...
cd frontend
call npm install
if %errorlevel% neq 0 (
    echo 前端依赖安装失败！
    pause
    exit /b 1
)

echo.
echo 2. 启动前端服务（端口3001）...
start "12306前端服务" cmd /k "npm start"

echo.
echo 3. 等待前端服务启动...
timeout /t 5 /nobreak

echo.
echo 4. 编译Java后端...
cd ..
call mvn clean compile
if %errorlevel% neq 0 (
    echo Java后端编译失败！
    pause
    exit /b 1
)

echo.
echo 5. 启动Java后端服务（端口8080）...
start "12306后端服务" cmd /k "mvn spring-boot:run"

echo.
echo 服务启动完成！
echo 前端服务: http://localhost:3001
echo 后端服务: http://localhost:8080
echo MCP工具已注册，可以开始使用了。
echo.
echo 按任意键退出...
pause 
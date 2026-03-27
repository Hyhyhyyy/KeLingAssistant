@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║                 课灵 KeLing 多端启动器                       ║
echo ╠════════════════════════════════════════════════════════════╣
echo ║  [1] 启动后端服务器 (Server)                                 ║
echo ║  [2] 启动网页前端 (Web)                                      ║
echo ║  [3] 同时启动服务器和网页                                     ║
echo ║  [4] 查看Android项目说明                                     ║
echo ║  [5] 安装所有依赖                                            ║
echo ║  [6] 构建生产版本                                            ║
echo ║  [7] 查看项目状态                                            ║
echo ║  [0] 退出                                                    ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

set /p choice="请选择操作 [0-7]: "

if "%choice%"=="1" goto server
if "%choice%"=="2" goto web
if "%choice%"=="3" goto both
if "%choice%"=="4" goto android
if "%choice%"=="5" goto install
if "%choice%"=="6" goto build
if "%choice%"=="7" goto status
if "%choice%"=="0" goto end

:server
echo.
echo 🚀 启动后端服务器...
cd /d "%~dp0apps\server"
call npm run dev
goto end

:web
echo.
echo 🌐 启动网页前端...
cd /d "%~dp0apps\web"
call npm run dev
goto end

:both
echo.
echo 🚀 同时启动服务器和网页...
start "KeLing Server" cmd /k "cd /d %~dp0apps\server && npm run dev"
timeout /t 3 >nul
start "KeLing Web" cmd /k "cd /d %~dp0apps\web && npm run dev"
echo.
echo ✅ 已在新窗口启动服务
echo    后端: http://localhost:3001
echo    前端: http://localhost:5173
goto end

:android
echo.
echo 📱 Android 项目说明:
echo.
echo    项目位置: %~dp0apps\android
echo.
echo    启动步骤:
echo    1. 打开 Android Studio
echo    2. 选择 Open an Existing Project
echo    3. 选择 apps\android 目录
echo    4. 等待 Gradle 同步完成
echo    5. 点击 Run 按钮启动模拟器或真机
echo.
echo    注意: 确保后端服务器已在 http://localhost:3001 运行
echo    Android 模拟器会自动通过 10.0.2.2 访问本机
echo.
pause
goto menu

:install
echo.
echo 📦 安装所有依赖...
echo.
echo [1/3] 安装共享包依赖...
cd /d "%~dp0packages\shared"
call npm install
echo.
echo [2/3] 安装后端依赖...
cd /d "%~dp0apps\server"
call npm install
echo.
echo [3/3] 安装前端依赖...
cd /d "%~dp0apps\web"
call npm install
echo.
echo ✅ 所有依赖安装完成!
pause
goto menu

:build
echo.
echo 🔨 构建生产版本...
echo.
echo [1/3] 构建共享类型包...
cd /d "%~dp0packages\shared"
call npm run build
echo.
echo [2/3] 构建后端...
cd /d "%~dp0apps\server"
call npm run build
echo.
echo [3/3] 构建前端...
cd /d "%~dp0apps\web"
call npm run build
echo.
echo ✅ 生产版本构建完成!
echo    后端输出: apps\server\dist
echo    前端输出: apps\web\dist
echo    Android APK: apps\android\app\build\outputs\apk\release
pause
goto menu

:status
echo.
echo 📊 项目状态检查...
echo.
echo [后端服务]
curl -s http://localhost:3001/api/health 2>nul
if errorlevel 1 (
    echo    状态: 未运行
) else (
    echo    状态: 运行中
)
echo.
echo [前端服务]
curl -s http://localhost:5173 2>nul | findstr "html" >nul
if errorlevel 1 (
    echo    状态: 未运行
) else (
    echo    状态: 运行中
)
echo.
echo [数据库]
if exist "%~dp0apps\server\prisma\keling.db" (
    echo    状态: 已创建
) else (
    echo    状态: 未创建
)
echo.
pause
goto menu

:end
exit /b

:menu
cls
goto :eof
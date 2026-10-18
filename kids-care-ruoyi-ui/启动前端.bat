@echo off
chcp 936 >nul
title KidsCare Frontend :81
cd /d "%~dp0"

rem 已经有前端在 81 端口运行时，不要再起第二个实例，直接打开浏览器
netstat -ano | findstr /C:":81 " | findstr "LISTENING" >nul 2>nul
if not errorlevel 1 (
    echo 前端已在运行，直接为你打开 http://localhost:81
    start "" "http://localhost:81"
    timeout /t 2 >nul
    exit /b 0
)

if not exist node_modules (
    echo 首次运行，正在安装前端依赖，约 1-3 分钟...
    call npm install
    if errorlevel 1 (
        echo 依赖安装失败，请检查网络后重试。
        pause
        exit /b 1
    )
)

echo 正在启动前端: http://localhost:81  （浏览器会自动弹出）
call npm run dev
pause

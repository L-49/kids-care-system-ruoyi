@echo off
chcp 936 >nul
title KidsCare Backend :8081
set JAVA_HOME=C:/Users/HP/.jdks/ms-17.0.20.1
set PATH=%JAVA_HOME%\bin;%PATH%
cd /d "%~dp0"

rem 后端已在 8081 端口运行时，直接打开系统页面即可
netstat -ano | findstr /C:":8081 " | findstr "LISTENING" >nul 2>nul
if not errorlevel 1 (
    echo 后端已在运行，直接为你打开 http://localhost:81
    start "" "http://localhost:81"
    timeout /t 2 >nul
    exit /b 0
)

echo [1/2] 正在编译模块（离线模式）...
call "D:/Jetbrains/Maven/apache-maven-3.9.10/bin/mvn.cmd" -o -q -DskipTests install
if errorlevel 1 goto :fail

echo [2/2] 正在启动后端 http://localhost:8081
call "D:/Jetbrains/Maven/apache-maven-3.9.10/bin/mvn.cmd" -o -pl kids-admin spring-boot:run
goto :end

:fail
echo 编译失败，请看上方报错信息。

:end
pause

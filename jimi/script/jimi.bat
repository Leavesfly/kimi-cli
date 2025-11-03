@echo off
REM Jimi - Java Implementation of Moonshot Intelligence
REM Windows 启动脚本

setlocal enabledelayedexpansion

REM 获取脚本所在目录
set SCRIPT_DIR=%~dp0

REM JAR 文件路径
set JAR_FILE=%SCRIPT_DIR%target\jimi-0.1.0.jar

REM 检查 JAR 文件是否存在
if not exist "%JAR_FILE%" (
    echo 错误: 找不到 JAR 文件: %JAR_FILE%
    echo 请先运行: mvn clean package
    exit /b 1
)

REM 检查 JAVA_HOME
if "%JAVA_HOME%"=="" (
    echo 警告: JAVA_HOME 未设置，使用系统默认 Java
    set JAVA_CMD=java
) else (
    set JAVA_CMD=%JAVA_HOME%\bin\java
)

REM 检查 Java 是否可用
"%JAVA_CMD%" -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 找不到 Java 命令
    exit /b 1
)

REM JVM 参数
if "%JVM_OPTS%"=="" (
    set JVM_OPTS=-Xms256m -Xmx2g
)

REM 运行 JAR
"%JAVA_CMD%" %JVM_OPTS% -jar "%JAR_FILE%" %*

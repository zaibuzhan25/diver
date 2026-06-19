@echo off
chcp 65001 >nul
set MAVEN_HOME=..\..\tools\maven\apache-maven-3.9.6
set PATH=%MAVEN_HOME%\bin;%PATH%
echo 正在下载依赖并编译...
call mvn clean compile
echo 编译完成!
pause

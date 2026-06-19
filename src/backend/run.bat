@echo off
chcp 65001 >nul
set MAVEN_HOME=..\..\tools\maven\apache-maven-3.9.6
set PATH=%MAVEN_HOME%\bin;%PATH%
echo ========================================
echo  旅游方案管理系统 - 后端启动
echo  数据库: MySQL (localhost:3306/diver)
echo ========================================
call mvn spring-boot:run
pause

@echo off
setlocal
set "GRADLE_USER_HOME=%~dp0gradle-home"
call "%~dp0gradlew.bat" runClient %*
exit /b %ERRORLEVEL%

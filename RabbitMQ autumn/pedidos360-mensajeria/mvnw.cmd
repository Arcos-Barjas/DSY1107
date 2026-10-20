@echo off
setlocal
REM DSY1107 - Maven Wrapper para Windows.
REM No requiere Maven instalado globalmente. La primera ejecucion descarga Maven 3.9.11.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0.mvn\wrapper\maven-wrapper.ps1" %*
exit /b %ERRORLEVEL%

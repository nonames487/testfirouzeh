@echo off
setlocal

set APP_HOME=%~dp0
set GRADLE_VERSION=8.0
set GRADLE_BIN=%APP_HOME%\.gradle\bootstrap\gradle-%GRADLE_VERSION%\bin\gradle.bat

where gradle >nul 2>nul
if exist "%GRADLE_BIN%" goto run_bootstrap
if %ERRORLEVEL%==0 (
  gradle %*
  exit /b %ERRORLEVEL%
)

where java >nul 2>nul
if not %ERRORLEVEL%==0 (
  echo ERROR: Java/JDK is required to run Gradle, but java was not found on PATH. 1>&2
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command "New-Item -ItemType Directory -Force '%APP_HOME%\.gradle\bootstrap' | Out-Null; Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%APP_HOME%\.gradle\bootstrap\gradle-%GRADLE_VERSION%-bin.zip'; Expand-Archive -Force '%APP_HOME%\.gradle\bootstrap\gradle-%GRADLE_VERSION%-bin.zip' '%APP_HOME%\.gradle\bootstrap'"
if not %ERRORLEVEL%==0 exit /b %ERRORLEVEL%

:run_bootstrap
"%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%

@echo off
setlocal EnableExtensions EnableDelayedExpansion

where mvn >nul 2>nul
if %ERRORLEVEL%==0 (
  mvn %*
  exit /b %ERRORLEVEL%
)

set "BASE_DIR=%~dp0"
set "PROPS=%BASE_DIR%.mvn\wrapper\maven-wrapper.properties"
if not exist "%PROPS%" (
  echo Missing %PROPS% 1>&2
  exit /b 1
)

for /f "tokens=1,* delims==" %%A in (%PROPS%) do (
  if "%%A"=="distributionUrl" set "DIST_URL=%%B"
)
if not defined DIST_URL (
  echo distributionUrl is not configured in %PROPS% 1>&2
  exit /b 1
)

for %%F in (!DIST_URL!) do set "ARCHIVE_NAME=%%~nxF"
set "MVN_DIR_NAME=!ARCHIVE_NAME:-bin.zip=!"
if defined MAVEN_USER_HOME (
  set "CACHE_BASE=%MAVEN_USER_HOME%"
) else (
  set "CACHE_BASE=%USERPROFILE%\.m2"
)
set "CACHE_ROOT=!CACHE_BASE!\wrapper\dists\!MVN_DIR_NAME!"
set "MVN_BIN=!CACHE_ROOT!\!MVN_DIR_NAME!\bin\mvn.cmd"
set "ARCHIVE=!CACHE_ROOT!\!ARCHIVE_NAME!"

if not exist "!MVN_BIN!" (
  if not exist "!CACHE_ROOT!" mkdir "!CACHE_ROOT!"
  if not exist "!ARCHIVE!" (
    echo Downloading Maven from !DIST_URL! 1>&2
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
      "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest -UseBasicParsing -Uri '%DIST_URL%' -OutFile '%ARCHIVE%'"
    if errorlevel 1 exit /b 1
  )
  if exist "!CACHE_ROOT!\!MVN_DIR_NAME!" rmdir /s /q "!CACHE_ROOT!\!MVN_DIR_NAME!"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%CACHE_ROOT%' -Force"
  if errorlevel 1 exit /b 1
)

call "!MVN_BIN!" %*
exit /b %ERRORLEVEL%

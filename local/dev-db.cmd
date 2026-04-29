@echo off
setlocal

set "SCRIPT_DIR=%~dp0"

for /f "usebackq eol=# tokens=1,* delims==" %%a in ("%SCRIPT_DIR%..\backend\.env") do (
    if not "%%a"=="" set "%%a=%%b"
)

docker compose -f "%SCRIPT_DIR%docker-compose.yml" %*
@echo off
REM Stage 1 Pipeline Local Test Script
REM Tests all 7 services build locally before running Jenkins

setlocal enabledelayedexpansion
cd /d "%~dp0"

echo.
echo ╔════════════════════════════════════════════════════════════════════╗
echo ║                 STAGE 1 PIPELINE - LOCAL TEST                    ║
echo ║         Testing all 7 services Maven build locally                ║
echo ╚════════════════════════════════════════════════════════════════════╝
echo.

set COUNT=0
set SUCCESS=0
set FAILED=0

REM Define services array
set "services[0]=auth-service"
set "services[1]=user-service"
set "services[2]=ticket-service"
set "services[3]=assignment-service"
set "services[4]=notifications-service"
set "services[5]=analytics-service"
set "services[6]=eureka-server"

REM Build each service
for /L %%i in (0,1,6) do (
    set service=!services[%%i]!
    set /a COUNT+=1
    
    echo.
    echo ┌─────────────────────────────────────────────────────────────────
    echo │ [%%i/7] Building !service!...
    echo └─────────────────────────────────────────────────────────────────
    
    cd /d "!service!"
    
    REM Run Maven build
    mvn clean package -DskipTests -U -q > nul 2>&1
    
    REM Check if JAR was created
    if exist "target\!service!-0.0.1-SNAPSHOT.jar" (
        echo  ✅ SUCCESS: !service! built
        set /a SUCCESS+=1
    ) else (
        echo  ❌ FAILED: !service! build failed
        set /a FAILED+=1
    )
    
    cd ..
)

echo.
echo ════════════════════════════════════════════════════════════════════
echo BUILD SUMMARY
echo ════════════════════════════════════════════════════════════════════
echo  ✅ Successful: !SUCCESS!/7
echo  ❌ Failed: !FAILED!/7
echo.

if %FAILED% equ 0 (
    echo ✅ ALL SERVICES BUILT SUCCESSFULLY!
    echo.
    echo Next: Run Jenkins pipeline at:
    echo   http://localhost:8080/job/ITSM-Build/
    echo.
    exit /b 0
) else (
    echo ❌ Some services failed. Check output above.
    exit /b 1
)

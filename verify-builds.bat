@echo off
echo.
echo =========================================
echo VERIFYING ALL 7 SERVICES BUILT
echo =========================================
echo.

setlocal enabledelayedexpansion

set "services=auth-service user-service ticket-service assignment-service notifications-service analytics-service eureka-server"
set "success=0"
set "failed=0"

for %%s in (%services%) do (
    if exist "%%s\target\%%s-0.0.1-SNAPSHOT.jar" (
        echo [OK] %%s
        set /a success+=1
    ) else (
        echo [FAIL] %%s
        set /a failed+=1
    )
)

echo.
echo =========================================
echo SUMMARY: %success% built, %failed% failed
echo =========================================
echo.

if %failed% equ 0 (
    echo All services ready for Jenkins!
    echo Next: Run Jenkins pipeline
    echo URL: http://localhost:8080/job/ITSM-Build/build
    exit /b 0
) else (
    echo Some services failed. Check builds above.
    exit /b 1
)

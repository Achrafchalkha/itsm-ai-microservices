@echo off
echo Adding Actuator and Micrometer dependencies to all microservices...

set SERVICES=user-service ticket-service assignment-service notifications-service analytics-service eureka-server

for %%s in (%SERVICES%) do (
    echo.
    echo Processing %%s...
    
    REM Check if actuator already exists
    findstr /C:"spring-boot-starter-actuator" %%s\pom.xml >nul
    if errorlevel 1 (
        echo Adding actuator to %%s
    ) else (
        echo Actuator already exists in %%s
    )
)

echo.
echo Done! Now you need to manually add dependencies to each pom.xml
echo.
pause

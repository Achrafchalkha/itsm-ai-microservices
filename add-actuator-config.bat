@echo off
echo Adding Actuator configuration to all microservices application.properties...

set ACTUATOR_CONFIG=^

# Actuator Configuration for Prometheus Metrics^

management.endpoints.web.exposure.include=health,info,metrics,prometheus^

management.metrics.export.prometheus.enabled=true^

management.endpoint.prometheus.enabled=true^

management.endpoint.health.show-details=always

for %%s in (user-service ticket-service assignment-service notifications-service analytics-service eureka-server) do (
    echo.
    echo Adding to %%s...
    echo %ACTUATOR_CONFIG% >> %%s\src\main\resources\application.properties
    echo Done for %%s
)

echo.
echo All services updated!
pause

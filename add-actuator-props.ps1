$services = @("user-service", "ticket-service", "assignment-service", "notifications-service", "analytics-service", "eureka-server")

$actuatorConfig = @"

# Actuator Configuration for Prometheus Metrics
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true
management.endpoint.prometheus.enabled=true
management.endpoint.health.show-details=always
"@

foreach ($service in $services) {
    $propertiesPath = ".\$service\src\main\resources\application.properties"
    
    if (Test-Path $propertiesPath) {
        $content = Get-Content $propertiesPath -Raw
        
        if ($content -notmatch "management.endpoints.web.exposure.include") {
            Write-Host "Adding Actuator config to $service..." -ForegroundColor Green
            Add-Content -Path $propertiesPath -Value $actuatorConfig
            Write-Host "Done!" -ForegroundColor Cyan
        } else {
            Write-Host "Actuator config already exists in $service" -ForegroundColor Yellow
        }
    } else {
        Write-Host "ERROR: $propertiesPath not found!" -ForegroundColor Red
    }
}

Write-Host "All services updated!" -ForegroundColor Green

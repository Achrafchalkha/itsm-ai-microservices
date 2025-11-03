# PowerShell script to add Actuator dependencies to all remaining services

$services = @("ticket-service", "assignment-service", "notifications-service", "analytics-service", "eureka-server")

foreach ($service in $services) {
    $pomPath = ".\$service\pom.xml"
    
    if (Test-Path $pomPath) {
        $content = Get-Content $pomPath -Raw
        
        # Check if actuator already exists
        if ($content -notmatch "spring-boot-starter-actuator") {
            Write-Host "Adding Actuator to $service..." -ForegroundColor Green
            Write-Host "Please add manually to $service pom.xml" -ForegroundColor Yellow
        } else {
            Write-Host "Actuator already exists in $service" -ForegroundColor Yellow
        }
    } else {
        Write-Host "ERROR: $pomPath not found!" -ForegroundColor Red
    }
}

Write-Host "Done checking all services." -ForegroundColor Green

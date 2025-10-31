#!/usr/bin/env pwsh
# Stage 1 Pipeline - Local Test
# Tests all 7 services build locally before running Jenkins

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════════╗"
Write-Host "║                 STAGE 1 PIPELINE - LOCAL TEST                    ║"
Write-Host "║         Testing all 7 services Maven build locally                ║"
Write-Host "╚════════════════════════════════════════════════════════════════════╝"
Write-Host ""

$services = @(
    "auth-service",
    "user-service",
    "ticket-service",
    "assignment-service",
    "notifications-service",
    "analytics-service",
    "eureka-server"
)

$success = 0
$failed = 0

foreach ($service in $services) {
    $index = $services.IndexOf($service) + 1
    
    Write-Host ""
    Write-Host "┌─────────────────────────────────────────────────────────────────"
    Write-Host "│ [$index/7] Building $service..."
    Write-Host "└─────────────────────────────────────────────────────────────────"
    
    Push-Location $service
    
    # Run Maven build silently
    $output = & mvn clean package -DskipTests -U -q 2>&1
    
    # Check if JAR was created
    $jarPath = "target/$service-0.0.1-SNAPSHOT.jar"
    if (Test-Path $jarPath) {
        Write-Host "  ✅ SUCCESS: $service built"
        $success++
    } else {
        Write-Host "  ❌ FAILED: $service build failed"
        Write-Host "     Error: $output"
        $failed++
    }
    
    Pop-Location
}

Write-Host ""
Write-Host "════════════════════════════════════════════════════════════════════"
Write-Host "BUILD SUMMARY"
Write-Host "════════════════════════════════════════════════════════════════════"
Write-Host "  ✅ Successful: $success/7"
Write-Host "  ❌ Failed: $failed/7"
Write-Host ""

if ($failed -eq 0) {
    Write-Host "✅ ALL SERVICES BUILT SUCCESSFULLY!"
    Write-Host ""
    Write-Host "Next: Run Jenkins pipeline at:"
    Write-Host "   http://localhost:8080/job/ITSM-Build/build"
    Write-Host ""
    exit 0
} else {
    Write-Host "❌ Some services failed. Check output above."
    exit 1
}

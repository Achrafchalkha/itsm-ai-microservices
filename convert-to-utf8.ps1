$jenkinsfile = "c:\Users\LENOVO\Downloads\ITSM\Jenkinsfile"
$content = [System.IO.File]::ReadAllText($jenkinsfile)
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($jenkinsfile, $content, $utf8NoBom)
Write-Host "Converted to UTF-8 without BOM"

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$UserServiceUrl = "http://localhost:8081",
    [string]$AuthServiceUrl = "http://localhost:8082",
    [string]$MessageServiceUrl = "http://localhost:8083",
    [string]$BotServiceUrl = "http://localhost:8084"
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host "[PulseHub smoke] $Message"
}

function Invoke-Json {
    param(
        [string]$Uri,
        [string]$Method = "GET",
        [object]$Body = $null,
        [hashtable]$Headers = @{}
    )

    if ($null -eq $Body) {
        return Invoke-RestMethod -Uri $Uri -Method $Method -Headers $Headers
    }

    $json = $Body | ConvertTo-Json
    return Invoke-RestMethod `
        -Uri $Uri `
        -Method $Method `
        -ContentType "application/json; charset=utf-8" `
        -Headers $Headers `
        -Body ([System.Text.Encoding]::UTF8.GetBytes($json))
}

function Assert-Equal {
    param(
        [object]$Actual,
        [object]$Expected,
        [string]$Message
    )

    if ($Actual -ne $Expected) {
        throw "$Message Expected '$Expected', got '$Actual'."
    }
}

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$username = "smoke$suffix"
$displayName = "Smoke $suffix"
$password = "password123"

Write-Step "Checking health endpoints"
Assert-Equal (Invoke-Json "$BaseUrl/actuator/health").status "UP" "BFF health failed."
Assert-Equal (Invoke-Json "$UserServiceUrl/actuator/health").status "UP" "user-service health failed."
Assert-Equal (Invoke-Json "$AuthServiceUrl/actuator/health").status "UP" "auth-service health failed."
Assert-Equal (Invoke-Json "$MessageServiceUrl/actuator/health").status "UP" "message-service health failed."
Assert-Equal (Invoke-Json "$BotServiceUrl/actuator/health").status "UP" "bot-service health failed."

Write-Step "Registering user '$username' through BFF"
$register = Invoke-Json "$BaseUrl/api/auth/register" "POST" @{
    username = $username
    displayName = $displayName
    password = $password
}

Assert-True ($null -ne $register.userId) "Register response did not contain userId."
Assert-Equal $register.username $username "Register response username mismatch."
Assert-Equal $register.displayName $displayName "Register response displayName mismatch."

Write-Step "Checking user profile was created in user-service"
$profile = Invoke-Json "$UserServiceUrl/users/$($register.userId)"
Assert-Equal $profile.id $register.userId "User profile id does not match auth userId."
Assert-Equal $profile.username $username "User profile username mismatch."
Assert-Equal $profile.displayName $displayName "User profile displayName mismatch."

Write-Step "Logging in through BFF"
$login = Invoke-Json "$BaseUrl/api/auth/login" "POST" @{
    username = $username
    password = $password
}

Assert-True ($null -ne $login.token -and $login.token.Length -gt 0) "Login response did not contain token."
$authHeaders = @{ Authorization = "Bearer $($login.token)" }

Write-Step "Checking /api/me"
$me = Invoke-Json "$BaseUrl/api/me" "GET" $null $authHeaders
Assert-Equal $me.userId $register.userId "/api/me userId mismatch."
Assert-Equal $me.username $username "/api/me username mismatch."

Write-Step "Posting normal message through BFF"
$normalMessage = Invoke-Json "$BaseUrl/api/messages" "POST" @{
    channel = "general"
    content = "Smoke test message"
} $authHeaders

Assert-Equal $normalMessage.username $username "Message username should come from user-service profile."
Assert-Equal $normalMessage.content "Smoke test message" "Message content mismatch."

Write-Step "Posting bot trigger through BFF"
$botTrigger = Invoke-Json "$BaseUrl/api/messages" "POST" @{
    channel = "general"
    content = "hej bot"
} $authHeaders

Assert-Equal $botTrigger.username $username "Bot trigger username should come from user-service profile."

Write-Step "Waiting for PulseBot response"
$botResponse = $null
for ($attempt = 1; $attempt -le 10; $attempt++) {
    Start-Sleep -Milliseconds 700
    $messages = Invoke-Json "$BaseUrl/api/messages?channel=general" "GET" $null $authHeaders
    $botResponse = $messages | Where-Object {
        $_.username -eq "PulseBot" -and $_.content -like "*Testa att skriva /help*"
    } | Select-Object -First 1

    if ($null -ne $botResponse) {
        break
    }
}

Assert-True ($null -ne $botResponse) "PulseBot response was not found in /api/messages?channel=general."

Write-Step "OK health checks"
Write-Step "OK register"
Write-Step "OK user profile"
Write-Step "OK login"
Write-Step "OK /api/me"
Write-Step "OK message"
Write-Step "OK PulseBot response"
Write-Host "PulseHub smoke test passed for user '$username'."

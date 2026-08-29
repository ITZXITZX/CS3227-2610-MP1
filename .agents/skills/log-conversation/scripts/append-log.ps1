[CmdletBinding()]
param(
    [string] $InputPath,

    [Parameter(Mandatory = $true)]
    [string] $Prompt,

    [Parameter(Mandatory = $true)]
    [string] $Reply,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string[]] $Action,

    [string] $LogPath
)

$ErrorActionPreference = 'Stop'

if ($InputPath) {
    $payloadJson = [System.IO.File]::ReadAllText($InputPath, [System.Text.UTF8Encoding]::new($false))
    $payload = $payloadJson | ConvertFrom-Json
    if ($payload.PSObject.Properties.Name -contains 'Prompt') { $Prompt = [string] $payload.Prompt }
    if ($payload.PSObject.Properties.Name -contains 'Reply') { $Reply = [string] $payload.Reply }
    if ($payload.PSObject.Properties.Name -contains 'Action') { $Action = @($payload.Action | ForEach-Object { [string] $_ }) }
    if ($payload.PSObject.Properties.Name -contains 'LogPath') { $LogPath = [string] $payload.LogPath }
}

if (-not $LogPath) {
    $repositoryRoot = Split-Path -Parent (Split-Path -Parent (Split-Path -Parent (Split-Path -Parent $PSScriptRoot)))
    $LogPath = Join-Path $repositoryRoot 'logs\transcript.md'
}

function Get-MarkdownFence {
    param([string] $Text)

    $longestRun = 0
    foreach ($match in [regex]::Matches($Text, '`+')) {
        $longestRun = [Math]::Max($longestRun, $match.Length)
    }
    return ('`' * [Math]::Max(3, $longestRun + 1))
}

$resolvedLogPath = [System.IO.Path]::GetFullPath($LogPath)
$logDirectory = Split-Path -Parent $resolvedLogPath
[System.IO.Directory]::CreateDirectory($logDirectory) | Out-Null

$promptFence = Get-MarkdownFence -Text $Prompt
$replyFence = Get-MarkdownFence -Text $Reply
$timestamp = [DateTimeOffset]::Now.ToString('yyyy-MM-dd HH:mm:ss zzz')
$actionLines = ($Action | ForEach-Object { "- $_" }) -join [Environment]::NewLine

$entry = @"
## Conversation - $timestamp

### Conversation history

${promptFence}text
User: $Prompt

Assistant: $Reply
$promptFence

### Summary of Actions taken

$actionLines

"@

if (Test-Path -LiteralPath $resolvedLogPath) {
    [System.IO.File]::AppendAllText($resolvedLogPath, [Environment]::NewLine + $entry, [System.Text.UTF8Encoding]::new($false))
} else {
    [System.IO.File]::WriteAllText($resolvedLogPath, "# Transcript`r`n`r`n$entry", [System.Text.UTF8Encoding]::new($false))
}

$writtenText = [System.IO.File]::ReadAllText($resolvedLogPath, [System.Text.UTF8Encoding]::new($false))
if ($writtenText -match '[\u00C3\u00E2\u00C2\u0192\uFFFD]') {
    throw "Encoding validation failed: possible UTF-8 mojibake was detected in $resolvedLogPath"
}

Write-Output $resolvedLogPath

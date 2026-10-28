[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$ModJar,
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference = 'Stop'
$targetJar = (Resolve-Path -LiteralPath $ModJar).Path
$java = Join-Path $JavaHome 'bin\java.exe'
$javac = Join-Path $JavaHome 'bin\javac.exe'
if (-not (Test-Path -LiteralPath $javac)) { throw 'JavaHome must be a JDK 8 directory.' }
$projectPath = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$stamp = [DateTimeOffset]::UtcNow.ToString('yyyyMMdd-HHmmss-fff')
$classes = Join-Path $projectPath ('build\autofish-regression\' + $stamp)
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$sources = @(Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'stubs') -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
$sources += Join-Path $PSScriptRoot 'StableFishingSuite.java'
& $javac -encoding UTF-8 -source 8 -target 8 -proc:none -classpath $targetJar -d $classes @sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Output ('TARGET_SHA256 ' + (Get-FileHash -LiteralPath $targetJar -Algorithm SHA256).Hash)
& $java '-Xverify:all' '-Dfile.encoding=UTF-8' -cp ($classes + ';' + $targetJar) autofish.tests.StableFishingSuite $targetJar
exit $LASTEXITCODE

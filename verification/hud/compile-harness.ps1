param(
    [Parameter(Mandatory=$true)][string]$FixtureRoot,
    [Parameter(Mandatory=$true)][string]$JavaHome,
    [Parameter(Mandatory=$true)][string]$MappedForgeJar,
    [Parameter(Mandatory=$true)][string]$RuntimeClasspathFile,
    [Parameter(Mandatory=$true)][string]$Mappings,
    [Parameter(Mandatory=$true)][string]$SpecialSourceJar,
    [string]$DevelopmentClasses=(Join-Path $PSScriptRoot '..\..\build\classes\java\main')
)
$ErrorActionPreference='Stop'
$taskFixture=[IO.Path]::GetFullPath($FixtureRoot)
$taskClasses=Join-Path $taskFixture 'classes'
New-Item -ItemType Directory -Path $taskClasses,(Join-Path $taskFixture 'game\mods') -Force | Out-Null
$taskCp=$DevelopmentClasses+';'+$MappedForgeJar+';'+(Get-Content -LiteralPath $RuntimeClasspathFile -Raw).Trim()
& (Join-Path $JavaHome 'bin\javac.exe') -encoding UTF-8 -cp $taskCp -d $taskClasses (Join-Path $PSScriptRoot 'TooltipSmoke.java')
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
$taskDevJar=Join-Path $taskFixture 'harness-dev.jar'
& (Join-Path $JavaHome 'bin\jar.exe') cf $taskDevJar -C $taskClasses .
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
& (Join-Path $JavaHome 'bin\java.exe') -cp ($SpecialSourceJar+';'+$taskCp) net.md_5.specialsource.SpecialSource --in-jar $taskDevJar --out-jar (Join-Path $taskFixture 'game\mods\tooltipsmoke-test.jar') --srg-in $Mappings --live
exit $LASTEXITCODE

$Root = Split-Path -Parent $PSScriptRoot
if (-not $env:DBT_JAVA_HOME) {
    $mc = Join-Path $env:LOCALAPPDATA "Packages\Microsoft.4297127D64EC6_8wekyb3d8bbwe\LocalCache\Local\runtime\java-runtime-epsilon\windows-x64\java-runtime-epsilon"
    if (Test-Path (Join-Path $mc "bin\javac.exe")) { $env:DBT_JAVA_HOME = $mc }
}
if ($env:DBT_JAVA_HOME) { $env:JAVA_HOME = $env:DBT_JAVA_HOME }
$trust = "-Djavax.net.ssl.trustStoreType=Windows-ROOT"
$env:GRADLE_OPTS = "$env:GRADLE_OPTS $trust"
Push-Location $Root
try {
    & .\gradlew.bat "-Dorg.gradle.jvmargs=-Xmx4G -Dfile.encoding=UTF-8 $trust" @args
    exit $LASTEXITCODE
} finally {
    Pop-Location
}

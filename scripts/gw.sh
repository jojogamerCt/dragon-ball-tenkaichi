#!/usr/bin/env bash
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if [ -z "$DBT_JAVA_HOME" ]; then
  MC_RT="$LOCALAPPDATA/Packages/Microsoft.4297127D64EC6_8wekyb3d8bbwe/LocalCache/Local/runtime/java-runtime-epsilon/windows-x64/java-runtime-epsilon"
  if [ -x "$MC_RT/bin/javac.exe" ]; then DBT_JAVA_HOME="$MC_RT"; fi
fi
if [ -n "$DBT_JAVA_HOME" ]; then export JAVA_HOME="$DBT_JAVA_HOME"; fi
EXTRA=""
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) EXTRA="-Djavax.net.ssl.trustStoreType=Windows-ROOT" ;;
esac
export GRADLE_OPTS="$GRADLE_OPTS $EXTRA"
cd "$ROOT"
exec ./gradlew "-Dorg.gradle.jvmargs=-Xmx4G -Dfile.encoding=UTF-8 $EXTRA" "$@"

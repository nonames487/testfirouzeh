#!/usr/bin/env sh
set -e

cd "$(dirname "$0")/../android"
if command -v gradle >/dev/null 2>&1; then
  gradle wrapper --gradle-version 8.0 --distribution-type bin
else
  echo "Gradle is not installed. Use ./gradlew after installing a JDK, or install Gradle and rerun this script."
fi

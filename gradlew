#!/bin/sh
# Gradle wrapper launcher (simplified; downloads Gradle 8.2 if needed).
# See gradle/wrapper/gradle-wrapper.properties for the distribution URL.
set -e
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_URL="https://services.gradle.org/distributions/gradle-8.2-bin.zip"
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-8.2-bin"
if [ ! -x "$DIST_DIR/gradle-8.2/bin/gradle" ]; then
  mkdir -p "$DIST_DIR"
  TMP_ZIP="$DIST_DIR/gradle-8.2-bin.zip"
  if [ ! -f "$TMP_ZIP" ]; then
    echo "Downloading Gradle 8.2..."
    curl -L -o "$TMP_ZIP" "$DIST_URL" || wget -O "$TMP_ZIP" "$DIST_URL"
  fi
  cd "$DIST_DIR" && unzip -o -q "gradle-8.2-bin.zip" && cd "$APP_HOME"
fi
exec "$DIST_DIR/gradle-8.2/bin/gradle" "$@"

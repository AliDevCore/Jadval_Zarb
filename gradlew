#!/bin/sh
set -e
PRG="$0"
while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
APP_DIR=`dirname "$PRG"`
WRAPPER_JAR="$APP_DIR/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$WRAPPER_JAR" ]; then
    exec java -jar "$WRAPPER_JAR" "$@"
else
    if command -v gradle >/dev/null 2>&1; then
        exec gradle "$@"
    else
        echo "Error: gradle-wrapper.jar not found and 'gradle' command is not in PATH."
        exit 1
    fi
fi

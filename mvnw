#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Start Up Batch script
# ----------------------------------------------------------------------------

# Resolve all symlinks to get the real script path
SCRIPT="$0"
while [ -h "$SCRIPT" ] ; do
  ls=`ls -ld "$SCRIPT"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    SCRIPT="$link"
  else
    SCRIPT=`dirname "$SCRIPT"`/"$link"
  fi
done

DIRNAME=`dirname "$SCRIPT"`
WRAPPER_JAR="$DIRNAME/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER="org.apache.maven.wrapper.MavenWrapperMain"

if [ ! -f "$WRAPPER_JAR" ]; then
    mkdir -p "$DIRNAME/.mvn/wrapper"
    echo "Downloading maven-wrapper.jar..."
    curl -sH "User-Agent: Mozilla/5.0" -o "$WRAPPER_JAR" https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
fi

exec java -classpath "$WRAPPER_JAR" "$WRAPPER_LAUNCHER" "$@"

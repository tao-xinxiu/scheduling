#!/bin/bash

JAVA_HOME=${JAVA_HOME-NULL};
if [ "$JAVA_HOME" = "NULL" ]; then
    echo "The environment variable JAVA_HOME must be set to the current jdk distribution"
    exit 127
fi

BASEDIR=$(dirname $0)

LIB=$BASEDIR/../lib
CONF=$BASEDIR/../conf
DIST=$BASEDIR/../dist

CLASSPATH=$(find "$LIB" -name '*.jar' -printf '%p:' | sed 's/:$//')
CLASSPATH=$CLASSPATH:$(find "$DIST" -name '*.jar' -printf '%p:' | sed 's/:$//')

"$JAVA_HOME"/bin/java -classpath $CLASSPATH \
-Djava.security.manager \
-Djava.security.policy="$CONF/java.policy" \
org.ow2.proactive_grid_cloud_portal.cli.Main "$@"


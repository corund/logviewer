#!/bin/sh
BASEDIR=$(dirname $0)
JARS=$(echo $(ls -1 ${BASEDIR}/lib/*) | sed 's/ /:/g')
exec java \
    -classpath $JARS \
    net.corund.logviewer.LogViewer

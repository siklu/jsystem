#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
mvn -f jsystem-parent/ clean install && mvn -f jsystem-assembly/jsystem-runner clean install
if [ "$?" = "0" ]
then
    if [ "x$1" = "xdeploy" ]
    then
        mvn -f jsystem-assembly/jsystem-runner deploy
    fi
fi
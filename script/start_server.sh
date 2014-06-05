#!/bin/sh
cd `dirname $0`/..
mkdir -p ./logs

JAVA_OPTS="-Djava.ext.dirs=./libs"
JAVA_VM_OPTS="-Xms4g -Xmx4g -Xmn1g -Xss256k -XX:PermSize=128m -XX:MaxPermSize=128m -XX:+UseStringCache -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:+CMSClassUnloadingEnabled -XX:+DisableExplicitGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=68"
JAVA_JMX_OPTS="-Dcom.sun.management.jmxremote.port=9981 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_GC_OPTS="-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log -Dwrite.statistics=true"

#PROGRAM_ARGS="-p [port] -s [server type, netty3|netty4|mina3]"
PROGRAM_ARGS="-p 8080 -s netty3"

nohup java $JAVA_OPTS \
           $JAVA_VM_OPTS \
           $JAVA_JMX_OPTS \
           $JAVA_VM_OPTs \
           com.hongweiyi.bench.AppServer $PROGRAM_ARGS \
           > logs/server.log 2>&1 &

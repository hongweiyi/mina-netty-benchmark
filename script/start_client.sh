#!/bin/sh
cd `dirname $0`/..
mkdir -p ./logs

JAVA_OPTS="-Djava.ext.dirs=./libs"
JAVA_VM_OPTS="-Xms4g -Xmx4g -Xmn1g -Xss256k -XX:PermSize=128m -XX:MaxPermSize=128m -XX:+UseStringCache -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:+CMSClassUnloadingEnabled -XX:+DisableExplicitGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=68"
JAVA_JMX_OPTS="-Dcom.sun.management.jmxremote.port=9981 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"
JAVA_GC_OPTS="-XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:gc.log -Dwrite.statistics=true"

#PROGRAM_ARGS="-C [client type, netty3|netty4|mina3] -n [max connection num per session] -c [test count, -1 is forever] -i [info collect interval] -p [server port] -t [concurrent thread] -w [warm up count] -o [bench timeout] -H [host,host...] -l [log file]"
PROGRAM_ARGS="-C netty3 -n 10 -c -1 -i 100 -p 8080 -t 10 -w 10000 -o 10000 -H localhost -l logs/simperf.log"

nohup java $JAVA_OPTS \
           $JAVA_VM_OPTS \
           $JAVA_JMX_OPTS \
           $JAVA_GC_OPTS com.hongweiyi.bench.AppClient $PROGRAM_ARGS \
           > logs/client.log 2>&1 &

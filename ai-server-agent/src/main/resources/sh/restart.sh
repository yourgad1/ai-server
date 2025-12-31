#!/bin/bash
export JAVA_HOME=/data/jdk17/jdk-17.0.16
export PATH=$JAVA_HOME/bin:$PATH
m=1024
n=1024
base_home=$(pwd)
app_name='c2000-ai-server-metric-1.0-SNAPSHOT'
#app_version='1.0.0'
pid=`ps -ef|grep ${app_name}|grep -v grep|grep -v startup|awk '{print$2}'`
if [ -n "${pid}" ] ;then
        kill -9 ${pid}
        sleep 10
fi
log_path="${base_home}/logs"
if [ ! -d "$log_path" ]; then
    mkdir  -p $log_path
fi
java \
-Xmx${m}m -Xms${m}m -Xmn${n}m \
-Djavax.net.debug=ssl,handshake \
-jar $base_home/${app_name}.jar  &>> $base_home/logs/${app_name}.log &
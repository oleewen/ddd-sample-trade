#!/usr/bin/env bash

pid=0
JVM_ARGS=" -Xms3g -Xmx3g -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+UseCMSInitiatingOccupancyOnly -Xloggc:log/gc.log -XX:+PrintGCDetails"
PACKAGE_NAME="${artifactId}-boot.jar"
BACK_NAME="${PACKAGE_NAME}.back"
BUILD_NAME="${PACKAGE_NAME}.build"

JAVA_HOME="/usr/local/jdk1.8.0_65"

if [ -d "${JAVA_HOME}" ];then
JAVA_COMMAND=${JAVA_HOME}/bin/java
else
JAVA_COMMAND=java
fi

if [ ${2}x = "debug"x -o ${3}x = "debug"x ];then
JVM_ARGS="${JVM_ARGS} -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,address=8787,suspend=n"
fi

if [ "${2}"x = "test"x -o "${2}"x = "prod"x -o "${2}"x = "dev"x -o "${2}"x = "pre"x ];then
PROFILE=$2
else
PROFILE="NONE"
fi

checkProfile(){
 local cluster_file=".deploy/service.cluster.txt"
    if [ -f "$cluster_file" ];then
        cluster=`cat ${cluster_file}`
        if [ "${cluster}"x = "pre"x ];then
            PROFILE="pre"
        else
            PROFILE="prod"
        fi
    elif [ "${PROFILE}"x = "NONE"x ];then
      echo "profile must input"
      exit 1
    fi
}

start(){
    cp ${BUILD_NAME} ${PACKAGE_NAME}
    checkPid
    if [ "${pid}"x != "0"x ];then
      echo "please do not repeat "
      return 1
    fi
    checkJava
       if [ "${pid}"x != "0"x ];then
       echo "please do not repeat "
          return 1
       fi

    echo "starting java process"
    checkProfile
    nohup ${JAVA_COMMAND} -jar ${JVM_ARGS} -Dspring.profiles.active=${PROFILE} ${PACKAGE_NAME}  >/dev/null 2>&1 &
    sleep 1s
    healthCheck
    return $?
}

stop(){
    cp ${PACKAGE_NAME} ${BACK_NAME}
    apiShutdown=false
    result=$(curl -s --connect-timeout 3 -m 3 -X POST 'http://127.0.0.1:8888/shutdown')
    if [ "$?"x = "0"x -a "${result}"x = "{\"message\":\"Shutting down, bye...\"}"x ];then
        echo "shutdown api call success, server return :${result}"
        apiShutdown=true
        for((i=1;i<60;i++))
        do
        checkPid
        if [ "${pid}"x != "0"x ];then
          echo "server ${pid} is running, waiting to shutdown"
          sleep 1s
          continue
        else
           checkJava
           if [ "${pid}"x != "0"x ];then
              echo "server ${pid} is running,waiting to shutdown "
              sleep 1s
              continue
           else
             echo "server is shutdown success"
             return 0
           fi

        fi
        done
    else
     echo "server shutdown interface is closed,check pid"
       checkPid
       if [ "${pid}"x != "0"x ];then
          echo "server is running,kill process ${pid}"
          kill -9 "${pid}"
          rm -fr app.pid
          return 0
       fi
       echo "pid not exist check java process"
       checkJava
       if [ "${pid}"x != "0"x ];then
          echo "process is running,kill process ${pid}"
          kill -9 "${pid}"
          rm -fr app.pid
          return 0
       fi
       echo "no process is running,nothing need stop"
       return 0
    fi
}

checkJava(){
  v_pid=$(pgrep -f "${PACKAGE_NAME}" |head -1)
  if [ "${v_pid}"x = x ];then
    echo "java process is shutdown"
    pid=0
  else
    echo "java process is running pid:${v_pid}"
    echo "${v_pid}"> app.pid
    pid=$((v_pid))
  fi
}

checkPid(){
  if [ -f "app.pid" ];then
     v_pid=$(head -1 app.pid)
     if [ "${v_pid}"x = "0"x ];then
     pid=0
     else
         ps -ax | awk '{ print $1 }' | grep -e "^${v_pid}$" > /dev/null
         if [ "$?" = "0" ];then
           echo "pid<${v_pid}> process is running"
           pid=$((v_pid))
         else
           echo "pid<${v_pid}> is not running"
           echo "clean app.pid"
           rm -fr app.pid
           pid=0
         fi
      fi
  else
     pid=0
  fi
}

healthCheck(){
   echo "starting health check......"
   sleep 1s
   for((i=1;i<=60;i++))
   do
   result=$(curl -s --connect-timeout 1 -m 1 -X GET 'http://127.0.0.1:8888/health')
   if [ "$?" = 0 -a "${result}"x = "{\"status\":\"UP\"}"x ];then
     echo ""
     echo "system start up success"
     return 0
     else
   sleep 1s
   echo -e " ${i}\c"
   fi
   done
   echo ""
   echo "system start failed"
   exit 1
}

reload(){
  checkProfile
  stop
  start
  exit $?
}



case $1 in
    start)
      start
      ;;
    stop)
      stop
      ;;
    reload)
      reload
      ;;
    healthCheck)
      healthCheck
      ;;
    *)
     echo "arg start|stop|reload|healthCheck"
esac
exit $?

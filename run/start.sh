DIR=$( cd "$( dirname "$0"  )" && pwd )
echo "cd $DIR"
cd $DIR
rootDir=/home/admin/taovip-agent
jarFile=${rootDir}/log-agent.jar
pidFile=${rootDir}/pid-agent

test -e $pidFile
testResult=$?
if [ $testResult -eq 0 ]; then
    echo "[WARN] proccess of `cat $pidFile` may be exist, plz check and stop it."
    exit 1
fi

java -Xms256m -Xmx512m -XX:PermSize=96m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
  -Xloggc:${rootDir}/log/agent-gc.log -XX:+HeapDumpOnOutOfMemoryError -classpath \
  ${jarFile} com.taovip.agent.AgentStartUp &

javaResult=$?
pid=$!

if [ $javaResult -eq 0 ]; then
    echo "[INFO] start successfully of proccess ${pid}"
    echo $pid > $pidFile
    exit 0
fi
echo "[ERROR] start failed of code ${javaResult}"
exit 2
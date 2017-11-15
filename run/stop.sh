DIR=$( cd "$( dirname "$0"  )" && pwd )
echo "cd $DIR"
cd $DIR
rootDir=/home/admin/taovip-agent
pidFile=${rootDir}/pid-agent
test -e $pidFile
testResult=$?
if [ $testResult -eq 0 ]; then
    pid=`cat $pidFile`
    echo "[INFO] $pidFile exist, now check process now kill $pid"
    process=`ps -ef | grep "$pid" | grep -v grep | wc -l`
    if [ $process -eq 0 ]; then
        echo "[WARN] process of $pid dose not exist, just remove pid file $pidFile"
        rm -f $pidFile
        exit 0;
    fi
    kill $pid
    killResult=$?
    if [ $killResult -eq 0 ]; then
        echo "[INFO] kill $pid successfully, service stoped."
        rm -f $pidFile
        exit 0
    fi
    echo "[ERROR] kill $pid failed, plz kill it manual"
    exit 1
fi
echo "[WARN] $pidFile dose not exist, plz check!"
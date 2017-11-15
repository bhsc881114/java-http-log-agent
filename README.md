#### introduce
this can be use to tail log with http api . it's been use in qipeng.com 3 years,it's reliable

<br/>
refer to:[how-can-i-follow-a-file-like-tail-f-does-in-java-without-holding-the-file-open](http://stackoverflow.com/questions/14610621/how-can-i-follow-a-file-like-tail-f-does-in-java-without-holding-the-file-ope)

#### http server:

- [simple HTTP server in Java using only Java SE API](http://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api)
- [API](http://docs.oracle.com/javase/6/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-frame.html)


#### use
- 1.package with package.sh. need maven2/3 ,java7
- 2.start cmd,default port is 11456 :
```
java -Xms256m -Xmx512m -XX:PermSize=96m -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps
-Xloggc:/home/admin/log-agent/log/agent-gc.log -XX:+HeapDumpOnOutOfMemoryError
-classpath log-agent.jar com.taovip.agent.AgentStartUp [port]
```
- 3.add tail log :     curl 'http://127.0.0.1:11456/addFile?filePath=/tmp/tmp.log&charset=utf-8'
- 4.tail log :         curl 'http://127.0.0.1:11456/log2?filePath=/tmp/tmp.log'
- 5.list tailing log : curl 'http://127.0.0.1:11456/listFiles'

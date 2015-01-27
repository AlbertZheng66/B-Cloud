
@rem auction mgr console 
@rem 2009.01.01
@echo off

rem set JAVA_HOME=./jre/

set LIB_HOME=..\libs



:normal
set PATH=%JAVA_HOME%/bin;%PATH%;

set classpath=%LIB_HOME%\*;..\dist\B-Cloud.jar


echo %classpath%

java -Djava.net.preferIPv4Stack=true com.xt.bcloud.td.io.TaskDispathcer6  -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f conf\gt-config.xml

:exit
@echo on

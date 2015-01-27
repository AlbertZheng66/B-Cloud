@echo off 

ECHO "The first parameter:" %1

SET EXE_DIR=..

IF NOT "%1" == "" SET EXE_DIR=%1

ECHO "EXE_DIR:" %EXE_DIR% 

REM echo JAVA_HOME = "%JAVA_HOME%"\bin\java

SET CP= 
FOR %%i IN (%EXE_DIR%\lib\*.jar) DO CALL :addpath %%i 


echo %CP%

set classpath=%CP%

set JAVA_OPTS=-Dcom.sun.management.jmxremote.port=29988 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false

echo %JAVA_OPTS%

set EXECUTABLE="%JAVA_HOME%\bin\java"

set CMD_LINE_ARGS=-Djava.net.preferIPv4Stack=true -Dstarter.class=com.xt.bcloud.td7.TaskDispatcher7 -classpath %CP% com.xt.core.app.Starter -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f %EXE_DIR%/conf/gt-config.xml


REM call %EXECUTABLE% start %CMD_LINE_ARGS%

REM  start "TD" %EXECUTABLE% %CMD_LINE_ARGS%

start "TD" %EXECUTABLE% -Djava.net.preferIPv4Stack=true %JAVA_OPTS% -Dstarter.class=com.xt.bcloud.td7.TaskDispatcher7 com.xt.core.app.Starter -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f %EXE_DIR%\conf\gt-config.xml

rem "%JAVA_HOME%"\bin\java -Djava.net.preferIPv4Stack=true %JAVA_OPTS% -Dstarter.class=com.xt.bcloud.td7.TaskDispatcher7 com.xt.core.app.Starter -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f conf\gt-config.xml


GOTO end 

:addpath 
SET CP=%CP%;%1 
GOTO :EOF 

:noJava 
echo. 
echo Please set JAVA_HOME environment variable. 
echo Build Exit now. 
echo. 
goto end 

:end 

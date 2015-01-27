@echo off



REM echo JAVA_HOME = "%JAVA_HOME%"\bin\java

REM SET JRE_HOME="D:\Program Files\Java\jdk1.7.0_03\jre"

ECHO "JRE_HOME=%JRE_HOME%"

SET CP= 
FOR %%i IN (.\lib\*.jar) DO CALL :addpath %%i 


echo %CP%

set classpath=%CP%

start "MDU" "%JAVA_HOME%"\bin\java -Djava.net.preferIPv4Stack=true com.xt.bcloud.mdu.MduManagerStarter -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f conf\gt-config.xml


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

@echo off 



REM echo JAVA_HOME = "%JAVA_HOME%"\bin\java

SET CP= 
FOR %%i IN (.\lib\*.jar) DO CALL :addpath %%i 


echo %CP%

set classpath=%CP%

"%JAVA_HOME%"\bin\java -Djava.net.preferIPv4Stack=true com.xt.core.app.Stoper -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f conf\gt-config.xml


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

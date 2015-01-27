@echo off

echo "start executing command %1"

set _dir=%~d1%~p1
 
echo 'changing to' %_dir%

REM 
%~d1%
chdir %_dir%
chdir ..

echo 'current dir:' %cd%

echo 'executing ' %1

echo "Using JRE_HOME: %JRE_HOME%"
SET CATALINA_HOME=%cd%

rem start "MDU-CLIENT" "%1" 
echo "calling" "%1" "%cd%"
call  "%1" "%cd%"
@echo off

rem dirname.bat
rem 2009-02-23
rem Chieh Cheng
rem http://www.CynosureX.com/

rem GNU General Public License (GPL), Version 2, June 1991

  set dirname=
  set memory=
  set directory=%~1%
  set DirPath=%directory%

:loop
  If "%DirPath%" == "" GoTo :done
  if not "%memory%" == "" set dirname=%dirname%\%memory%
  rem echo "dirname=" %dirname%
  rem echo "dirname2=" %dirname:~1%
  For /F "tokens=1* delims=\" %%a in ("%DirPath%") Do set memory=%%a
  For /F "tokens=1* delims=\" %%a in ("%DirPath%") Do Set DirPath=%%b
  GoTo :loop

:done
  if "%dirname:~0,1%" == "\" (
    set dirname0=%dirname:~1%
    echo %dirname:~1%
  ) else (
    set dirname0=%dirname%
    echo %dirname:~1%
  )
  goto :end

:help
  echo   Usage:   %~n0%~x0 "path"

:end

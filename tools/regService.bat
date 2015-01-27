"%~dp0prunsrv.exe" //IS//RCPDatabase --Install="%~dp0prunsrv.exe" --Description="RCP Embedded Database" --Jvm="%~dp0jre\bin\client\jvm.dll" --Classpath="%~dp0libs\rcp2_db_service.jar";"%~dp0libs\hsqldb.jar"; --StartMode=jvm --StartClass=com.itown.rcp.deamon.db.Starter --Startup=auto --StartMethod=start --StartParams="%~dp0db\;dbcache;20000" --StopMode=jvm --StopClass=com.itown.rcp.deamon.db.Starter --StopMethod=stop --StopParams=stop --LogPath="%~dp0logs\service" --StdOutput=auto --StdError=auto
 
rem Run the service
net start RCPDatabase
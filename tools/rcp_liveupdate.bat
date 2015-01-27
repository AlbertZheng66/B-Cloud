@echo off 
REM echo JAVA_HOME = "./jre/bin/java

set libpath=lib/

set classpath=%classpath%;%libpath%commons-beanutils.jar;%libpath%castor-1.2-xml.jar;%libpath%log4j-1.2.9.jar;%libpath%commons-logging-1.1.jar;%libpath%xerces.jar;%libpath%commons-collections-3.2.jar;%libpath%commons-httpclient-3.1.jar;%libpath%commons-codec-1.4.jar;%libpath%commons-pool-1.3.jar;%libpath%commons-lang2.2.jar;%libpath%jdom.jar;%libpath%cglib-nodep-2.2.jar;%libpath%ognl-2.6.9.jar;%libpath%javassist.jar;%libpath%servlet.jar;%libpath%servletapi-2.3.jar;%libpath%rcp-liveupdate-launcher.jar;%libpath%rcp-liveupdate.jar;%libpath%rcp2_client.jar;%libpath%beansbinding-1.2.1.jar

 "jre\bin\java" -Djava.system.class.loader=com.itown.rcp.liveupdate.classloader.RCPClassLoader2  com.itown.rcp.liveupdate.plugin.AutoUpdatePlugin

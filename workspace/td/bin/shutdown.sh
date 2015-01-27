#!/bin/sh


# JAVA_HOME = "%JAVA_HOME%"\bin\java
CLASSPATH=

for i in ./lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done


echo $CLASSPATH


$JAVA_HOME/bin/java "%JAVA_HOME%"\bin\java -Djava.net.preferIPv4Stack=true com.xt.core.app.Stoper -l com.xt.gt.sys.loader.CommandLineSystemLoader -m CLIENT_SERVER -p local -f conf/gt-config.xml

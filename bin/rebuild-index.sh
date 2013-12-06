#!/bin/bash

classpath=/opt/storm/storm-0.8.1.jar
for jar in $(find /opt/storm/lib /opt/lumify/lib -name '*.jar' 2>/dev/null); do
  classpath=${classpath}:${jar}
done

java \
-Dfile.encoding=UTF-8 \
-Djava.security.krb5.realm= \
-Djava.security.krb5.kdc= \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.lumify.storm.searchIndex.SearchIndexTool

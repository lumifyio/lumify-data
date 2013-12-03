#!/bin/bash

classpath=/opt/lumify-storm-1.0-SNAPSHOT-jar-with-dependencies.jar:/opt/storm/storm-0.8.1.jar
for jar in /opt/storm/lib/*.jar; do
  classpath=${classpath}:${jar}
done

java \
-Dfile.encoding=UTF-8 \
-Djava.security.krb5.realm= \
-Djava.security.krb5.kdc= \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.lumify.storm.searchIndex.SearchIndexTool

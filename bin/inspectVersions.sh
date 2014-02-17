#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

case "$1" in
	project | p)
  	  	SCAN_PATH=$(${DIR}/classpath.sh $2)
	  	if [ $? -ne 0 ]; then
	  	  echo "${SCAN_PATH}"
		  exit
	  	fi
	  	;;
	archive | a)
	  	SCAN_PATH=$2
	  	;;
	classpath | cp)
		SCAN_PATH=$2
		;;
	*)
	  	echo "Proper Usage: $0 (project|p {project-root} | archive|a {archive-path} | classpath|cp {classpath}) [-s|--short] [-v|--verbose]"
	  	exit
	  	;;
esac
	
classpath=$(${DIR}/classpath.sh lumify-public/lumify-tools)
if [ $? -ne 0 ]; then
    echo "${classpath}"
  exit
fi
  
java \
-Dfile.encoding=UTF-8 \
-Djava.awt.headless=true \
-Djava.security.krb5.realm= \
-Djava.security.krb5.kdc= \
-classpath ${classpath} \
-Xmx1024M \
com.altamiracorp.lumify.tools.version.VersionInspector \
--scanpath ${SCAN_PATH} \
$*
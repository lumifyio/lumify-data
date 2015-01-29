#!/bin/bash

DIR=$(cd $(dirname "$0") && pwd)

mkdir -p ${DIR}/schema

java -jar /opt/trang/trang.jar ${DIR}/data/boa-165-1310.pdf.rdf.xml ${DIR}/schema/rdf.xsd
sed -i -e 's|<xs:attribute ref="rdf:resource" use="required"/>|<xs:attribute ref="rdf:resource" />|' ${DIR}/schema/rdf.xsd


#!/bin/bash -e

DIR=$(cd $(dirname "$0") && pwd)
BUILD_DIR=${DIR}/build

function create_zip {
    NAME=$1
    ZIP_FILE_NAME=${BUILD_DIR}/sinaloa-${NAME}.zip
    echo "Creating build/$(basename ${ZIP_FILE_NAME})"
    (
        cd ${DIR}/${NAME}
        zip -rq ${ZIP_FILE_NAME} .
    )
}

rm -rf ${BUILD_DIR}/*
mkdir -p ${BUILD_DIR}
create_zip ontology
create_zip data-rdf
echo "DONE"

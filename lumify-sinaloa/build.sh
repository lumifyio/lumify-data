#!/bin/bash -e

DIR=$(cd $(dirname "$0") && pwd)
BUILD_DIR=${DIR}/build

function create_zip {
    local name=$1
    local zip_file_name=${BUILD_DIR}/sinaloa-${name}.zip
    echo "Creating build/$(basename ${zip_file_name})"
    (
        cd ${DIR}/${name}
        zip -rq ${zip_file_name} .
    )
}

rm -rf ${BUILD_DIR}
mkdir -p ${BUILD_DIR}
create_zip ontology
create_zip data-rdf
create_zip data
echo "DONE"

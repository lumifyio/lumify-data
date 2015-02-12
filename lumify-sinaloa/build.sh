#!/bin/bash -e

DIR=$(cd $(dirname "$0") && pwd)
BUILD_DIR=${DIR}/build

function create_zip {
    local name=$1
    local zip_file_name=${BUILD_DIR}/${name}.zip
    echo "Creating build/$(basename ${zip_file_name})"
    (
        cd ${DIR}
        zip -rq ${zip_file_name} ${name}
    )
}

rm -rf ${BUILD_DIR}
mkdir -p ${BUILD_DIR}
create_zip sinaloa-ontology
create_zip sinaloa-data-rdf
create_zip sinaloa-data
echo "DONE"

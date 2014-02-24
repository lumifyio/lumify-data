#!/bin/bash -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

source ${DIR}/setenv.sh
source ${DIR}/functions.sh

name="lumify-opencv"
version="2.4.5"
release="dist"

_download \
    ${name} \
    http://downloads.sourceforge.net/project/opencvlibrary/opencv-unix/${version}/opencv-${version}.tar.gz \
    opencv-${version}.tar.gz

if [ -d ${SOURCE_DIR}/opencv-${version} ]; then
  mv ${SOURCE_DIR}/opencv-${version} ${SOURCE_DIR}/${name}
fi

# problem with our distribution of cmake that doesn't set the JNI_FOUND property
sed -i 's/JNI_FOUND/1/g' ${SOURCE_DIR}/${name}/modules/java/CMakeLists.txt
sed -i 's/.*ocv_module_disable(java)/  message(FATAL_ERROR "Could not enable java module")/g' ${SOURCE_DIR}/${name}/modules/java/CMakeLists.txt

_build ${name} ${version} ${release} x86_64 i386

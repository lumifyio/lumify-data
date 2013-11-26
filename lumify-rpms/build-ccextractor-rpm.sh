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

CCEXTRACTOR_VERSION=0.66

_download \
    lumify-ccextractor \
    http://downloads.sourceforge.net/project/ccextractor/ccextractor/${CCEXTRACTOR_VERSION}/ccextractor.src.${CCEXTRACTOR_VERSION}.zip \
    ccextractor.src.${CCEXTRACTOR_VERSION}.zip

if [ -d ${SOURCE_DIR}/ccextractor.0.66 ]; then
  mv ${SOURCE_DIR}/ccextractor.0.66 ${SOURCE_DIR}/lumify-ccextractor
fi

cd ${SOURCE_DIR}/lumify-ccextractor

rm -f ${RPMBUILD_DIR}/SOURCES/lumify-ccextractor-${CCEXTRACTOR_VERSION}.tar.gz

cd ${SOURCE_DIR}
tar czf ${RPMBUILD_DIR}/SOURCES/lumify-ccextractor-${CCEXTRACTOR_VERSION}.tar.gz lumify-ccextractor/*

cp ${DIR}/specs/lumify-ccextractor.spec ${RPMBUILD_DIR}/SPECS/lumify-ccextractor.spec

rpmlint ${RPMBUILD_DIR}/SPECS/lumify-ccextractor.spec

rpmbuild -ba ${RPMBUILD_DIR}/SPECS/lumify-ccextractor.spec

cp ${RPMBUILD_DIR}/SRPMS/lumify-ccextractor-${CCEXTRACTOR_VERSION}-dist.src.rpm ${LUMIFYREPO_DIR}/SRPMS
cp ${RPMBUILD_DIR}/RPMS/x86_64/lumify-ccextractor-${CCEXTRACTOR_VERSION}-dist.x86_64.rpm ${LUMIFYREPO_DIR}/RPMS/x86_64
cp ${RPMBUILD_DIR}/SOURCES/lumify-ccextractor-${CCEXTRACTOR_VERSION}.tar.gz ${LUMIFYREPO_DIR}/source

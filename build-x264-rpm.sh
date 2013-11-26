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


_clone lumify-videolan-x264 http://git.videolan.org/git/x264.git stable

cd ${SOURCE_DIR}/lumify-videolan-x264
x264_version=$(./version.sh | awk -F '"' '/X264_POINTVER/ {print $2}' | cut -d ' ' -f 1)
x264_release=$(./version.sh | awk -F '"' '/X264_POINTVER/ {print $2}' | cut -d ' ' -f 2)

rm -f ${RPMBUILD_DIR}/SOURCES/lumify-videolan-x264-${x264_version}.tar.gz

cd ${SOURCE_DIR}
tar czf ${RPMBUILD_DIR}/SOURCES/lumify-videolan-x264-${x264_version}.tar.gz lumify-videolan-x264/*

cp ${DIR}/specs/lumify-videolan-x264.spec ${RPMBUILD_DIR}/SPECS/lumify-videolan-x264.spec
sed -i -e "s/Version:.*/Version:\t${x264_version}/" -e "s/Release:.*/Release:\t${x264_release}/" ${RPMBUILD_DIR}/SPECS/lumify-videolan-x264.spec

rpmlint ${RPMBUILD_DIR}/SPECS/lumify-videolan-x264.spec

rpmbuild -ba ${RPMBUILD_DIR}/SPECS/lumify-videolan-x264.spec

cp ${RPMBUILD_DIR}/SRPMS/lumify-videolan-x264-${x264_version}-${x264_release}.src.rpm ${LUMIFYREPO_DIR}/SRPMS
cp ${RPMBUILD_DIR}/RPMS/x86_64/lumify-videolan-x264-${x264_version}-${x264_release}.x86_64.rpm ${LUMIFYREPO_DIR}/RPMS/x86_64
cp ${RPMBUILD_DIR}/SOURCES/lumify-videolan-x264-${x264_version}.tar.gz ${LUMIFYREPO_DIR}/source

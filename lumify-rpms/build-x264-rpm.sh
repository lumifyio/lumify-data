#!/bin/bash -e

cd /home/makerpm/source/lumify-videolan-x264
export VERSION=$(./version.sh | awk -F '"' '/X264_POINTVER/ {print $2}' | cut -d ' ' -f 1)
export RELEASE=$(./version.sh | awk -F '"' '/X264_POINTVER/ {print $2}' | cut -d ' ' -f 2)
cd -

rm /home/makerpm/rpmbuild/SOURCES/lumify-videolan-x264-${VERSION}.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-videolan-x264-${VERSION}.tar.gz lumify-videolan-x264/*
cd -

echo Building ${VERSION}-${RELEASE}
cp specs/lumify-videolan-x264.spec rpmbuild/SPECS/lumify-videolan-x264.spec
sed -i -e "s/Version:.*/Version:\t${VERSION}/" -e "s/Release:.*/Release:\t${RELEASE}/" rpmbuild/SPECS/lumify-videolan-x264.spec

rpmlint rpmbuild/SPECS/lumify-videolan-x264.spec

rpmbuild -ba rpmbuild/SPECS/lumify-videolan-x264.spec

mkdir -p repo/SRPMS/
mkdir -p repo/RPMS/x86_64/
mkdir -p repo/source/
cp rpmbuild/SRPMS/lumify-videolan-x264-${VERSION}-${RELEASE}.src.rpm repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-videolan-x264-${VERSION}-${RELEASE}.x86_64.rpm repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-videolan-x264-${VERSION}.tar.gz repo/source/

#!/bin/bash -e

cd /home/makerpm/source/videolan-x264
export VERSION=$(./version.sh | awk -F '"' '/X264_POINTVER/ {print $2}' | cut -d ' ' -f 1)
export RELEASE=$(./version.sh | awk -F '"' '/X264_POINTVER/ {print $2}' | cut -d ' ' -f 2)
cd -

rm /home/makerpm/rpmbuild/SOURCES/videolan-x264-${VERSION}.tar.gz

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/videolan-x264-${VERSION}.tar.gz videolan-x264/*
cd -

echo Building ${VERSION}-${RELEASE}
cp specs/videolan-x264.spec rpmbuild/SPECS/videolan-x264.spec
sed -i -e "s/Version:.*/Version:\t${VERSION}/" -e "s/Release:.*/Release:\t${RELEASE}/" rpmbuild/SPECS/videolan-x264.spec

rpmlint rpmbuild/SPECS/videolan-x264.spec

rpmbuild -ba rpmbuild/SPECS/videolan-x264.spec

cp rpmbuild/SRPMS/videolan-x264-${VERSION}-${RELEASE}.src.rpm repo/SRPMS/
cp rpmbuild/RPMS/x86_64/videolan-x264-${VERSION}-${RELEASE}.x86_64.rpm repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/videolan-x264-${VERSION}.tar.gz repo/source/

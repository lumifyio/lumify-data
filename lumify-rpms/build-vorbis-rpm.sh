#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lumify-vorbis-1.3.3.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-vorbis-1.3.3.tar.gz lumify-vorbis/*
cd -

cp specs/lumify-vorbis.spec rpmbuild/SPECS/lumify-vorbis.spec

rpmlint rpmbuild/SPECS/lumify-vorbis.spec

QA_RPATHS=$[ 0x0001|0x0010 ] rpmbuild -ba rpmbuild/SPECS/lumify-vorbis.spec

mkdir -p repo/SRPMS/
mkdir -p repo/RPMS/x86_64/
mkdir -p repo/source/
cp rpmbuild/SRPMS/lumify-vorbis* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-vorbis* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-vorbis-1.3.3.tar.gz repo/source/

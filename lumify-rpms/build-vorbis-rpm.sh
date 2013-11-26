#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/vorbis-1.3.3.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/vorbis-1.3.3.tar.gz vorbis/*
cd -

cp specs/vorbis.spec rpmbuild/SPECS/vorbis.spec

rpmlint rpmbuild/SPECS/vorbis.spec

QA_RPATHS=$[ 0x0001|0x0010 ] rpmbuild -ba rpmbuild/SPECS/vorbis.spec

cp rpmbuild/SRPMS/vorbis* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/vorbis* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/vorbis-1.3.3.tar.gz repo/source/

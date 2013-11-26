#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lumify-fdk-aac-0.1.1.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-fdk-aac-0.1.1.tar.gz lumify-fdk-aac/*
cd -

cp specs/lumify-fdk-aac.spec rpmbuild/SPECS/lumify-fdk-aac.spec

rpmlint rpmbuild/SPECS/lumify-fdk-aac.spec

rpmbuild -ba rpmbuild/SPECS/lumify-fdk-aac.spec

mkdir -p repo/SRPMS/
mkdir -p repo/RPMS/x86_64/
mkdir -p repo/source/
cp rpmbuild/SRPMS/lumify-fdk-aac* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-fdk-aac* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-fdk-aac-0.1.1.tar.gz repo/source/

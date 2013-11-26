#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lumify-opus-1.0.3.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-opus-1.0.3.tar.gz lumify-opus/*
cd -

cp specs/lumify-opus.spec rpmbuild/SPECS/lumify-opus.spec

rpmlint rpmbuild/SPECS/lumify-opus.spec

rpmbuild -ba rpmbuild/SPECS/lumify-opus.spec

mkdir -p repo/SRPMS/
mkdir -p repo/RPMS/x86_64/
mkdir -p repo/source/
cp rpmbuild/SRPMS/lumify-opus* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-opus* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-opus-1.0.3.tar.gz repo/source/

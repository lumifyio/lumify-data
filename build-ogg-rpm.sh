#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lumify-ogg-1.3.1.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-ogg-1.3.1.tar.gz lumify-ogg/*
cd -

cp specs/ogg.spec rpmbuild/SPECS/lumify-ogg.spec

rpmlint rpmbuild/SPECS/lumify-ogg.spec

rpmbuild -ba rpmbuild/SPECS/lumify-ogg.spec

cp rpmbuild/SRPMS/lumify-ogg* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-ogg* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-ogg-1.3.1.tar.gz repo/source/

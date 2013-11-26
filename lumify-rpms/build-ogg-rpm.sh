#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/ogg-1.3.1.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/ogg-1.3.1.tar.gz ogg/*
cd -

cp specs/ogg.spec rpmbuild/SPECS/ogg.spec

rpmlint rpmbuild/SPECS/ogg.spec

rpmbuild -ba rpmbuild/SPECS/ogg.spec

cp rpmbuild/SRPMS/ogg* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/ogg* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/ogg-1.3.1.tar.gz repo/source/

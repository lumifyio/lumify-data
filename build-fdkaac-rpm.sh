#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/fdk-aac-0.1.1.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/fdk-aac-0.1.1.tar.gz fdk-aac/*
cd -

cp specs/fdk-aac.spec rpmbuild/SPECS/fdk-aac.spec

rpmlint rpmbuild/SPECS/fdk-aac.spec

rpmbuild -ba rpmbuild/SPECS/fdk-aac.spec

cp rpmbuild/SRPMS/fdk-aac* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/fdk-aac* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/fdk-aac-0.1.1.tar.gz repo/source/

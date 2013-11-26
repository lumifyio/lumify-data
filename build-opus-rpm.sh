#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/opus-1.0.3.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/opus-1.0.3.tar.gz opus/*
cd -

cp specs/opus.spec rpmbuild/SPECS/opus.spec

rpmlint rpmbuild/SPECS/opus.spec

rpmbuild -ba rpmbuild/SPECS/opus.spec

cp rpmbuild/SRPMS/opus* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/opus* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/opus-1.0.3.tar.gz repo/source/

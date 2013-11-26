#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/vpx-1.2.0.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/vpx-1.2.0.tar.gz vpx/*
cd -

cp specs/vpx.spec rpmbuild/SPECS/vpx.spec

rpmlint rpmbuild/SPECS/vpx.spec

rpmbuild -ba rpmbuild/SPECS/vpx.spec

cp rpmbuild/SRPMS/vpx* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/vpx* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/vpx-1.2.0.tar.gz repo/source/

#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lumify-vpx-1.2.0.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-vpx-1.2.0.tar.gz lumify-vpx/*
cd -

cp specs/vpx.spec rpmbuild/SPECS/lumify-vpx.spec

rpmlint rpmbuild/SPECS/lumify-vpx.spec

rpmbuild -ba rpmbuild/SPECS/lumify-vpx.spec

cp rpmbuild/SRPMS/lumify-vpx* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-vpx* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-vpx-1.2.0.tar.gz repo/source/

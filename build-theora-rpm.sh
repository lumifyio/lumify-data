#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lumify-theora-1.1.1.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-theora-1.1.1.tar.gz lumify-theora/*
cd -

cp specs/lumify-theora.spec rpmbuild/SPECS/lumify-theora.spec

rpmlint rpmbuild/SPECS/lumify-theora.spec

rpmbuild -ba rpmbuild/SPECS/lumify-theora.spec

cp rpmbuild/SRPMS/lumify-theora* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-theora* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-theora-1.1.1.tar.gz repo/source/

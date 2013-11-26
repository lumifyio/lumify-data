#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lumify-lame-3.99.5.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-lame-3.99.5.tar.gz lumify-lame/*
cd -

cp specs/lumify-lame.spec rpmbuild/SPECS/lumify-lame.spec

rpmlint rpmbuild/SPECS/lumify-lame.spec

rpmbuild -ba rpmbuild/SPECS/lumify-lame.spec

cp rpmbuild/SRPMS/lumify-lame* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-lame* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-lame-3.99.5.tar.gz repo/source/

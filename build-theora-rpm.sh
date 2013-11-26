#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/theora-1.1.1.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/theora-1.1.1.tar.gz theora/*
cd -

cp specs/theora.spec rpmbuild/SPECS/theora.spec

rpmlint rpmbuild/SPECS/theora.spec

rpmbuild -ba rpmbuild/SPECS/theora.spec

cp rpmbuild/SRPMS/theora* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/theora* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/theora-1.1.1.tar.gz repo/source/

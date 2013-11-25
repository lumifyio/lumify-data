#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lame-3.99.5.tar.gz

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lame-3.99.5.tar.gz lame/*
cd -

cp specs/lame.spec rpmbuild/SPECS/lame.spec

rpmlint rpmbuild/SPECS/lame.spec

rpmbuild -ba rpmbuild/SPECS/lame.spec

cp rpmbuild/SRPMS/lame* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lame* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lame-3.99.5.tar.gz repo/source/

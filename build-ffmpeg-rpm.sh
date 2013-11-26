#!/bin/bash -e

rm /home/makerpm/rpmbuild/SOURCES/lumify-ffmpeg-2.0.tar.gz || true

cd /home/makerpm/source
tar -cvzf /home/makerpm/rpmbuild/SOURCES/lumify-ffmpeg-2.0.tar.gz lumify-ffmpeg/*
cd -

cp specs/lumify-ffmpeg.spec rpmbuild/SPECS/lumify-ffmpeg.spec

rpmlint rpmbuild/SPECS/lumify-ffmpeg.spec

rpmbuild -ba rpmbuild/SPECS/lumify-ffmpeg.spec

mkdir -p repo/SRPMS/
mkdir -p repo/RPMS/x86_64/
mkdir -p repo/source/
cp rpmbuild/SRPMS/lumify-ffmpeg* repo/SRPMS/
cp rpmbuild/RPMS/x86_64/lumify-ffmpeg* repo/RPMS/x86_64/
cp /home/makerpm/rpmbuild/SOURCES/lumify-ffmpeg-2.0.tar.gz repo/source/

#!/bin/bash -e

cd source
wget http://downloads.xiph.org/releases/vorbis/libvorbis-1.3.3.tar.gz
tar xzf libvorbis-1.3.3.tar.gz
mv libvorbis-1.3.3 lumify-vorbis

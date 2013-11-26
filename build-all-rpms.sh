#!/bin/bash -e

./build-x264-rpm.sh
./build-fdkaac-rpm.sh
./build-lame-rpm.sh
./build-opus-rpm.sh
./build-ogg-rpm.sh
./build-vorbis-rpm.sh
./build-vpx-rpm.sh
./build-theora-rpm.sh

./update-repo.sh

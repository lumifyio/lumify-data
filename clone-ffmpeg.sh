#!/bin/bash -e

cd source
git clone http://git.videolan.org/git/ffmpeg.git lumify-ffmpeg
cd lumify-ffmpeg
git checkout n2.0

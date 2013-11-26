#!/bin/bash -e

cd source
git clone http://git.videolan.org/git/x264.git lumify-videolan-x264
cd lumify-videolan-x264
git checkout stable

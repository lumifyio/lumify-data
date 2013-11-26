#!/bin/bash -e

cd source
git clone http://git.chromium.org/webm/libvpx.git lumify-vpx
cd lumify-vpx
git checkout v1.2.0
git apply ../vpx.patc

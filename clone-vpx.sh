#!/bin/bash -e

cd source
git clone http://git.chromium.org/webm/libvpx.git vpx
cd vpx
git checkout v1.2.0

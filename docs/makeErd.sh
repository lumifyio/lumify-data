#!/bin/bash

mkdir -p build
# TODO build erd.gv from all *.desc
rm -f build/erd.gv && node js/gvCreator.js
rm -f build/erd.png && dot build/erd.gv | neato -n -Tpng -obuild/erd.png


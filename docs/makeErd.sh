#!/bin/bash

mkdir -p build
# TODO build erd.gv from all *.desc
rm -f build/erd.gv && node js/gvCreator.js
rm -f build/erd.png && dot -Gsplines=none build/erd.gv | neato -n -Gsplines=ortho -Tpng -obuild/erd.png


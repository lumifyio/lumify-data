#!/bin/bash

rm -f erd.png && dot -Gsplines=none erd.gv | neato -n -Gsplines=ortho -Tpng -oerd.png


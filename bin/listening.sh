#!/bin/bash

for bind_proc in $(sudo netstat -anopt | awk '/LISTEN/ {printf "%s_%s\n", $4, $7}'); do
  bind=$(echo ${bind_proc} | cut -d '_' -f 1)
  pid=$(echo ${bind_proc} | cut -d '_' -f 2 | cut -d '/' -f 1)
  cmd=$(cat /proc/${pid}/cmdline)
  printf "%s\t%.80s\n" ${bind} ${cmd} | column -t -s '\t'
done

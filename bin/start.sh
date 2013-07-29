#!/bin/bash

if [ "${VIRTUALIZATION_DISABLED}" = 'true' ]; then
  cd /opt && ./start.sh
else
  vagrant ssh -c 'cd /opt && ./start.sh'
fi

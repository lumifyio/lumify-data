#!/bin/bash

/vagrant/deployment/control.sh localhost stop
service jetty stop

/vagrant/deployment/control.sh localhost rmlogs

find /home /root -name '.bash_history' -exec rm -f {} \;

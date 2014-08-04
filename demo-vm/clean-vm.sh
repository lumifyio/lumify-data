#!/bin/bash

/vagrant/deployment/control.sh localhost stop
service jetty stop

/vagrant/deployment/control.sh localhost rmlogs
rm -f /opt/jetty/logs/*
rm -f /opt/lumify/logs/*

find /home /root -name '.bash_history' -exec rm -f {} \;

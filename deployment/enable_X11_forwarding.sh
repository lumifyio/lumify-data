#!/bin/bash

yum -y install xorg-x11-xauth

sed -e 's/#*X11Forwarding.*/X11Forwarding yes/' \
    -e 's/#*X11UseLocalhost.*/X11UseLocalhost no/' \
    -i /etc/ssh/sshd_config

kill -1 $(cat /var/run/sshd.pid)

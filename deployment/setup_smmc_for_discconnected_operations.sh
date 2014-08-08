#!/bin/bash

sed -i -e 's/^[^;]/;/' /etc/resolv.conf
sed -i -e 's/.*UseDNS.*/UseDNS no/' /etc/ssh/sshd_config

sed -i -e 's/.*BOOTPROTO.*/BOOTPROTO=static/' \
       -e 's/.*IPADDR.*//' \
       /etc/sysconfig/network-scripts/ifcfg-bond0
ip_bond0=$(ip addr show bond0 2>/dev/null | awk '/inet / {print $2}' | sed -e 's/\/.*//')
echo "IPADDR=${ip_bond0}" >> /etc/sysconfig/network-scripts/ifcfg-bond0

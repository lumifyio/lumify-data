#!/bin/bash

# http://wiki.centos.org/TipsAndTricks/BondingInterfaces

cat <<-EO_CONF > /etc/modprobe.d/bond0.conf
  alias bond0 bonding
  options bond0 mode=6 miimon=100
EO_CONF

cat <<-EO_BOND > /etc/sysconfig/network-scripts/ifcfg-bond0
	DEVICE=bond0
	BOOTPROTO=dhcp
	ONBOOT=yes
	HOTPLUG=yes
	TYPE=Ethernet
	PEERDNS=no
EO_BOND

function _eth {
  local device=$1

  ifdown ${device}

  cat <<-EO_ETH > /etc/sysconfig/network-scripts/ifcfg-${device}
	DEVICE=${device}
	MASTER=bond0
	SLAVE=yes
	TYPE=Ethernet
EO_ETH
}

_eth eth0
_eth eth1

ifdown bond0
ifup bond0

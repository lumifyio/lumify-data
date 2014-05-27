install
text
reboot
url --url http://10.0.1.243/centos/6.4
lang en_US.UTF-8
keyboard us
%include /tmp/eth0.ks
network --onboot no --device eth1 --bootproto dhcp --noipv6
rootpw --iscrypted $6$J.BgMgiat5c5sXmw$iI0WHkzGZCQlq5fnxvbZoLDPAH4TeegSdCDW2C3YbTR.QAOmlqcQlpWrF75A/po.6IHP77k54tZuWNFDuszm10
firewall --service=ssh
authconfig --enableshadow --passalgo=sha512
selinux --permissive
timezone --utc America/New_York
bootloader --location=mbr --driveorder=sda,sdb --append="crashkernel=auto rhgb quiet"
clearpart --all
part / --fstype=ext4 --asprimary --size=8096
part /data0 --fstype=ext4 --grow --asprimary --size=200
part /data1 --fstype=ext4 --grow --asprimary --size=200
repo --name="nic-wks03" --baseurl=http://10.0.1.243/centos/6.4 --cost=100

%packages --nobase
@core
%end

%pre
eth0_ip=$(ip addr show eth0 2>/dev/null | awk '/inet / {print $2}' | sed -e 's/\/.*//')
octet4=$(echo ${eth0_ip} | cut -d . -f 4)
name=$(printf 'smmc%02d' $((${octet4} - 200)))
echo "network --device eth0 --bootproto dhcp --hostname ${name}" > /tmp/eth0.ks
%end

%post
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
ifdown eth0
cat <<-EO_ETH0 > /etc/sysconfig/network-scripts/ifcfg-eth0
	DEVICE=eth0
	MASTER=bond0
	SLAVE=yes
	TYPE=Ethernet
EO_ETH0
ifdown eth0
cat <<-EO_ETH1 > /etc/sysconfig/network-scripts/ifcfg-eth1
	DEVICE=eth1
	MASTER=bond0
	SLAVE=yes
	TYPE=Ethernet
EO_ETH1
ifdown bond0
ifup bond0
%end

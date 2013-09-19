Provisioning the Supermicro MicroCloud servers with Razor
=========================================================

Razor is being rewritten but we are still using the old version.
See [puppetlabs/Razor](https://github.com/puppetlabs/Razor) and
[workflow](https://github.com/puppetlabs/Razor/wiki/workflow) for more info.

Razor and Puppet are installed on VMs on the "hadoop" network.
ssh to `nic-hadoop-admin` and then on to `nic-hadoop-razor`


puppet setup
------------
TBD


razor setup
-----------
```
# download and add the microkernel image
curl -L -O https://github.com/downloads/puppetlabs/Razor-Microkernel/rz_mk_prod-image.0.9.1.6.iso
sudo razor image add -t mk -n mk-0.9.3.0 -p ./rz_mk_prod-image.0.9.1.6.iso

# download and add the os image
curl -O http://centos.servint.com/centos-6/6.4/isos/x86_64/CentOS-6.4-x86_64-minimal.iso
sudo razor image add -t os -n centos -v 6.4 -p ./CentOS-6.4-x86_64-minimal.iso

# add a template for the new os
sudo razor model get template
sudo razor model add -t centos_6 -l install_centos_6.4 -i 2F5i41Wq6PWLQRwueU756d

# add a broker for the puppet server
sudo razor broker add -p puppet -n nic-hadoop-puppet -d puppet -s 192.168.203.11

# list the discovered clients (nodes)
sudo razor node

# add a policy to install the os (model) and handoff to puppet (broker) on clients (nodes) with the specified tag
sudo razor policy get templates
sudo razor policy add -p linux_deploy -l deploy_centos_6.4 -m 342uK53cCMyJfrWYp7Ihdv -b kNuUUS1EPXfskcbbcxftv -t Supermicro
```

razor operation
---------------
```
sudo /opt/razor/bin/razor_daemon.rb start

# enable the policy
sudo razor policy update 4PZfLyVeMBZG8p6GqSwmPP -e true

# list the clients currently being deployed
sudo razor active_model
```

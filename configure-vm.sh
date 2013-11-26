#!/bin/bash

rpm -q epel-release-6-8 > /dev/null \
  || rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm

yum -y groupinstall development-tools
yum -y install yasm fedora-packager

id -u makerpm > /dev/null \
  || useradd makerpm

id -Gn makerpm | grep -q mock \
  || usermod -a -G mock makerpm

echo 'makerpm
makerpm' | passwd makerpm 2> /dev/null

su - makerpm -c "mkdir -p /home/makerpm/repo/RPMS/x86_64 /home/makerpm/repo/{SRPMS,source}"
su - makerpm -c "createrepo /home/makerpm/repo"

cat <<-EOM > /etc/yum.repos.d/lumify-local.repo
[lumify-local]
name=Local Lumify Repository
baseurl=file:///home/makerpm/repo
enabled=1
gpgcheck=0
EOM

su - makerpm -c rpmdev-setuptree
su - makerpm -c "mkdir -p /home/makerpm/source"

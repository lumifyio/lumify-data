#!/bin/bash

LUMIFY_REPO_URL="http://63.141.238.205:8081/redhat"

cat <<-EOM > /etc/yum.repos.d/lumify.repo
[lumify]
name=Lumify
baseurl=${LUMIFY_REPO_URL}
enabled=1
gpgcheck=0
EOM

rpm -q epel-release-6-8 > /dev/null \
  || rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm

yum -y groupinstall development-tools
yum -y install fedora-packager

id -u makerpm > /dev/null \
  || useradd makerpm

id -Gn makerpm | grep -q mock \
  || usermod -a -G mock makerpm

echo 'makerpm
makerpm' | passwd makerpm 2> /dev/null

su - makerpm -c rpmdev-setuptree

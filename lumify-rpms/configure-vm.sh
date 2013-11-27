#!/bin/bash

rpm -q epel-release-6-8 > /dev/null \
  || rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm

yum -y install yasm libtool fedora-packager cmake

# install ant
if [ ! -d /opt/ant ]; then
  mkdir -p /opt
  curl "http://www.poolsaboveground.com/apache//ant/binaries/apache-ant-1.9.2-bin.tar.gz" -s -L --fail -o /opt/apache-ant-1.9.2-bin.tar.gz
  $(cd /opt && tar xzf /opt/apache-ant-1.9.2-bin.tar.gz)
  ln -s /opt/apache-ant-1.9.3 /opt/ant
fi

# install java
if [ ! -f /opt/jdk-6u45-linux-amd64.rpm ]; then
  curl "https://s3.amazonaws.com/RedDawn/jdk-6u45-linux-amd64.rpm" -s -L --fail -o /opt/jdk-6u45-linux-amd64.rpm
fi
if [ ! -d /usr/java/default ]; then
  rpm -i /opt/jdk-6u45-linux-amd64.rpm
fi
if [ ! -f /etc/profile.d/java_home.sh ]; then
  cat <<-EOM > /etc/profile.d/java_home.sh
export JAVA_HOME=/usr/java/default
export PATH=\$PATH:\$JAVA_HOME/bin
EOM
fi

id -u makerpm > /dev/null \
  || useradd makerpm

id -Gn makerpm | grep -q mock \
  || usermod -a -G mock makerpm

echo 'makerpm
makerpm' | passwd makerpm 2> /dev/null

# allow makerpm to install via rpm
grep -q ^makerpm /etc/sudoers
if [ $? -ne 0 ]; then
  echo "makerpm ALL=/bin/rpm" >> /etc/sudoers
fi

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

#!/bin/bash

# add the EPEL repo
rpm -q epel-release-6-8 > /dev/null \
  || rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm

# configure yum to install every possible architecture for every package
grep -q 'multilib_policy=all' /etc/yum.conf
if [ $? -ne 0 ]; then
  echo >> /etc/yum.conf
  echo 'multilib_policy=all' >> /etc/yum.conf
fi

# install required RPMs
yum -y install yasm libtool fedora-packager cmake zlib-devel glibc-devel libgcc libstdc++

# install Ant
if [ ! -h /opt/ant ]; then
  mkdir -p /opt
  curl "http://archive.apache.org/dist/ant/binaries/apache-ant-1.9.2-bin.tar.gz" -s -L --fail -o /opt/apache-ant-1.9.2-bin.tar.gz
  (cd /opt && tar xzf /opt/apache-ant-1.9.2-bin.tar.gz)
  ln -s /opt/apache-ant-1.9.2 /opt/ant
fi

if [ ! -f /usr/bin/ant ]; then
  ln -s /opt/ant/bin/ant /usr/bin/ant
fi

# download and install JDK
if [ ! -f /opt/jdk-6u45-linux-amd64.rpm ]; then
  mkdir -p /opt
  curl "https://s3.amazonaws.com/RedDawn/jdk-6u45-linux-amd64.rpm" -s -L --fail -o /opt/jdk-6u45-linux-amd64.rpm
fi

rpm -q jdk-1.6.0_45-fcs > /dev/null \
  || rpm -i /opt/jdk-6u45-linux-amd64.rpm

if [ ! -f /etc/profile.d/java_home.sh ]; then
  cat <<-EOM > /etc/profile.d/java_home.sh
export JAVA_HOME=/usr/java/default
export PATH=\$PATH:\$JAVA_HOME/bin
EOM
fi

# install s3cmd
if [ ! -f /etc/yum.repos.d/s3tools.repo ]; then
  curl "http://s3tools.org/repo/RHEL_6/s3tools.repo" -s -L --fail -o /etc/yum.repos.d/s3tools.repo
fi

rpm -q s3cmd > /dev/null \
  || yum -y install s3cmd

# create the makerpm user
id -u makerpm > /dev/null \
  || useradd makerpm

id -Gn makerpm | grep -q mock \
  || usermod -a -G mock makerpm

echo 'makerpm
makerpm' | passwd makerpm 2> /dev/null

# allow makerpm to use sudo to run rpm
grep -q ^makerpm /etc/sudoers
if [ $? -ne 0 ]; then
  echo "makerpm ALL=NOPASSWD:/bin/rpm" >> /etc/sudoers
fi

su - makerpm -c "mkdir -p /home/makerpm/repo/RPMS/{i386,x86_64} /home/makerpm/repo/{SRPMS,source}"
su - makerpm -c "createrepo /home/makerpm/repo"

cat <<-EOM > /etc/yum.repos.d/lumify-local.repo
[lumify-local]
name=Local Lumify Repository
baseurl=file:///home/makerpm/repo
enabled=1
gpgcheck=0
EOM

# setup the RPM buld environment
su - makerpm -c rpmdev-setuptree
su - makerpm -c "mkdir -p /home/makerpm/source"

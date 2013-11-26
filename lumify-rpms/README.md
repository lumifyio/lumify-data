lumify-rpms
===========

See https://fedoraproject.org/wiki/How_to_create_an_RPM_package for some documentation.

# Getting Started

```bash
yum install @development-tools
yum install fedora-packager
```

You may need /etc/yum.repos.d/epel.repo to install fedora-packager

```
[epel]
name=Extra Packages for Enterprise Linux 6 - $basearch
#baseurl=http://download.fedoraproject.org/pub/epel/6/$basearch
mirrorlist=https://mirrors.fedoraproject.org/metalink?repo=epel-6&arch=$basearch
failovermethod=priority
enabled=1
gpgcheck=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-EPEL-6

[epel-debuginfo]
name=Extra Packages for Enterprise Linux 6 - $basearch - Debug
#baseurl=http://download.fedoraproject.org/pub/epel/6/$basearch/debug
mirrorlist=https://mirrors.fedoraproject.org/metalink?repo=epel-debug-6&arch=$basearch
failovermethod=priority
enabled=0
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-EPEL-6
gpgcheck=1

[epel-source]
name=Extra Packages for Enterprise Linux 6 - $basearch - Source
#baseurl=http://download.fedoraproject.org/pub/epel/6/SRPMS
mirrorlist=https://mirrors.fedoraproject.org/metalink?repo=epel-source-6&arch=$basearch
failovermethod=priority
enabled=0
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-EPEL-6
gpgcheck=1
```

```bash
/usr/sbin/useradd makerpm
usermod -a -G mock makerpm
passwd makerpm
```

```bash
su makerpm
cd ~/
git clone git@github.com:nearinfinity/lumify-rpms.git
rpmdev-setuptree
```

Create a reference to the local repo. Create a file /etc/yum.repos.d/lumify-local.repo

```
[lumify-local]
name=Lumify
baseurl=file:///home/makerpm/repo
enabled=1
gpgcheck=0
```

Run the clone scripts, then the build scripts, then the update-repo.sh script.

```
yum install lumify-videolan-x264
yum install lumify-fdk-aac
yum install lumify-lame
yum install lumify-opus
yum install lumify-ogg
yum install lumify-vorbis
yum install lumify-vpx
yum install lumify-theora
yum install lumify-ffmpeg
```

# Target machine

Create the file /etc/yum.repos.d/lumify.repo

```
[lumify]
name=Lumify
baseurl=http://63.141.238.205:8081/redhat/
enabled=1
gpgcheck=0
```

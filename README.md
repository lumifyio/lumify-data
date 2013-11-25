lumify-rpms
===========

See https://fedoraproject.org/wiki/How_to_create_an_RPM_package for some documentation.

# Getting Started

```bash
yum install @development-tools
yum install fedora-packager
```

```bash
/usr/sbin/useradd makerpm
usermod -a -G mock makerpm
passwd makerpm
```

```bash
su makerpm
cd ~/
rpmdev-setuptree
```

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
git clone git@github.com:nearinfinity/lumify-rpms.git
rpmdev-setuptree
```

Run the clone scripts, then the build scripts, then the update-repo.sh script.

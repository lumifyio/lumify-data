lumify-rpms
===========

## Development

### Prerequisites

1. see https://fedoraproject.org/wiki/How_to_create_an_RPM_package
2. run `configure-vm.sh` or similar

### Next

0. `sudo su - makerpm`
1. run the clone scripts
2. run the build scripts
3. run the `update-repo.sh` script


## Use

### Configuration

1. create `/etc/yum.repos.d/lumify.repo`

```
[lumify]
name=Lumify
baseurl=http://63.141.238.205:8081/redhat/
enabled=1
gpgcheck=0
```

### Installation

```
yum install lumify-ffmpeg
yum install lumify-ccextractor
yum install lumify-opencv
yum install lumify-tesseract
```

lumify-rpms
===========

## Development

### Prerequisites

1. see https://fedoraproject.org/wiki/How_to_create_an_RPM_package
2. run `configure-vm.sh` or similar

### Next

1. `sudo su - makerpm`
2. run the `build-all.sh` script
3. run the `update-repo.sh` script
4. publish `repo.tar.gz` to S3


## Use

### Configuration

1. create `/etc/yum.repos.d/lumify.repo`

```
[lumify]
name=Lumify
baseurl=http://bits.lumify.io/yum/
enabled=1
gpgcheck=0
```

### Installation

```
yum install lumify-ffmpeg # the codecs will be installed as dependencies
yum install lumify-ccextractor
yum install lumify-tesseract
yum install lumify-tesseract-eng
yum install lumify-opencv
```

License
=======

Copyright 2014 Altamira Technologies Corporation

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

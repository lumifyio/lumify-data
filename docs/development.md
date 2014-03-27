The development environments are configured and controlled by [Vagrant](http://www.vagrantup.com/).
The `Vagrantfile` in the root of this repo will install everything you need to get started and keep you in sync with other developers.


## Virtual Machines

The Lumify Vagrant configuration defines six Virtual Machines:

  - `rpm` - used to compile our dependencies
  - `puppet` - used to manage the local SMMC cluster
  - `dev` - used for development including closed source enterprise features **(DEFAULT)**
  - `qlix` - used for QLIX integration development (w/ httpd, Tomcat, and MySQL)
  - `test` - used for automated integration testing
  - `demo-opensource` - used to create the downloadable open source demo VM
  - `demo-enterprise` - used to create an enterprise demo VM


## Vagrant Setup

1. install [VitualBox (v. 4.2.12)](https://www.virtualbox.org/wiki/Download_Old_Builds_4_2)
1. install the [VirtualBox Extension Pack](https://www.virtualbox.org/wiki/Download_Old_Builds_4_2)
1. install [Vagrant](http://docs.vagrantup.com/v2/installation/)


## Using Vagrant

In the root directory where you have cloned this repo:

        vagrant up dev
        vagrant ssh dev

- if you're running the command from Altamira's McLean facility, you should append the proxy URL to make downloading faster: `PROXY_URL=http://10.0.1.243:3128 vagrant up dev`
- the first time you run `vagrant up` it will download a base CentOS 6.4 image and then provision the VM via Puppet taking several minutes and displaying tons of progress messages
- subsequent times you run `vagrant up` will take less time and will skip Puppet provisioning unless you include the `--provision` option

- after `vagrant ssh` you will be inside the VM as the `vagrant` user who has `sudo` privileges
- the `/vagrant` directory inside the VM is mapped to the root directory on your host where you have cloned this repo
- Lumify software is installed in the following directories:
    - `/usr/lib`
    - `/opt`
    - `/opt/lumify`

- run `vagrant halt` on your host to gracefully shutdown the VM


## Local Setup

You may choose to install Lumify dependencies on your host to allow you to run some parts of the application outside of a VM.
Additional documentation is available for the following platforms:

- [OS X](development-osx.md)
- [Linux](development-linux.md)


## Running Lumify

- [Common Setup and the Web UI](running-lumify.md)
- [Storm Ingest](running-lumify-storm-ingest.md)


## Sample Data

- [Sample Data Sets](sample-data.md)
- [GEOINT Data](sample-data-geoint.md)
- [Regression Test Data](sample-data-testing.md)
- videos with closed captioning transcripts - http://ncam.wgbh.org/invent_build/web_multimedia/mobile-devices/sample-clips

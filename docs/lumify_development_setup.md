# Lumify Development Setup
---

This guide will help developers new to Lumify rapidly install and configure the tools and dependencies required to build and contribute to the project.

## Pre-configuration

This guide assumes that the developer has already configured a suitable Java development environment including the following tools. Platform-specific installation instructions can be found on the site for each tool if they have not yet been installed. Unless otherwise noted, the versions below are the minimum supported version.

*	[Java Development Kit (JDK)](http://java.oracle.com) _1.6_
*	[Maven](http://maven.apache.org) _3.0.5_
*	[Git](http://git-scm.com) _1.8_

## Git Repository

The Lumify Enterprise project can be found at [http://github.com/altamiracorp/lumify-enterprise](http://github.com/altamiracorp/lumify-enterprise). Clone this repository to your development machine to work with the Lumify source.

**`$LUMIFY_HOME` will be used throughout this document to refer to the root of the cloned repository.**

## Dependencies

The following tools are required for compilation and execution of the Lumify source code.  Instructions are provided for installation on [Macintosh OS X](osx-environment.md) and [Linux](linux-environment.md) environments.

*	ffmpeg
*	OpenCV
*	teseseract
*	NodeJS

## VirtualBox

[VirtualBox](https://virtualbox.org) is the default Virtual Machine host for Vagrant. Other VM hosts may be supported, but instructions will only be provided for the configuration and use of VirtualBox with Vagrant.

**Note that the development VMs are configured for VirtualBox 4.2.x.  They have not been fully tested with VirtualBox 4.3.x or later.**

1.	Download and install [VirtualBox](https://www.virtualbox.org/wiki/Download_Old_Builds_4_2).
1.	Download and install the [VirtualBox Extension Pack](https://www.virtualbox.org/wiki/Download_Old_Builds_4_2).

## Host-Only Network Configuration

The Lumify Virtual Machines are configured to use the IP address `192.168.33.10` on the host-only network interface `vboxnet0`. The following command will list the host-only networks currently configured in VirtualBox.

```
VBoxManage list hostonlyifs
```

If `vboxnet0` does not exist or is not configured for the IP address `192.168.33.1` and network mask `255.255.255.0`, execute the commands below to create and/or modify it.

**Note that if `vboxnet0` is defined for a different subnet, you may have to edit existing VM configurations that are using that interface after executing these commands.**

	```
	VBoxManage hostonlyif create
	VBoxManage hostonlyif ipconfig vboxnet0 --ip 192.168.33.1
	```
	
## Vagrant

The Lumify development environment is configured and controlled by [Vagrant](http://vagrantup.com).  The `$LUMIFY_HOME/Vagrantfile` will install and configure everything you need to get started and keep you in sync with other developers.

### Lumify Virtual Machines

The `$LUMIFY_HOME/Vagrantfile` defines three separate VMs for Lumify development.  Only one of these machines may be active at any time because all three use the same IP address, `192.168.33.10`.

*	**`dev`**
	
	A development environment.  This will be the machine you use most.
	
*	`rpm`

	This machine is used to build the Lumify RPMs.
	
*	`demo`

	This machine is used to build the Lumify demo VM.

You can find project-related tools and files in the following directories on each VM:

*	`/vagrant/home`

	The `vagrant` user's home directory.
	
*	`/vagrant`

	This directory is mapped from the host machine's `$LUMIFY_HOME` directory.
	It contains the project source code and support files.
	
*	`/vagrant/bin`

	This directory contains [scripts](#scripts) that can be used to configure and control
	the Lumify services.
	
*	`/opt`

	All project-related tools are installed here.
	
*	`/opt/lumify`

	Lumify configuration and data directory.

### Using Vagrant

Download and install [Vagrant](http://downloads.vagrantup.com), then execute the commands below from the `$LUMIFY_HOME` directory to control and use each virtual machine.

*	`vagrant up [dev|rpm|demo]`

	This will start the requested virtual machine. It may take several minutes to
	provision and configure each VM the first time you run this command.
	
*	`vagrant ssh`

	This will ssh into the currently running VM as the `vagrant` user.  The `vagrant`
	user has full `sudo` privileges.

*	`vagrant halt [dev|rpm|demo]`

	This will initiate a graceful shutdown of the target VM. If run without a VM name,
	it will attempt to stop all running virtual machines.

*	`vagrant -h`

	Displays all available vagrant commands.


### VM Initialization

After the `dev` VM has been provisioned, the Lumify services need to be initialized.  The following commands will get the development environment fully up and running.

1.	`vagrant up dev`

	Start the `dev` VM if it is not already running.
	
1.	`vagrant ssh`

	SSH into the dev VM as the `vagrant` user.
	
1.	`/vagrant/bin/080_Ontology.sh`

	Loads the development Ontologies.

## Running Lumify Services

1.	`/vagrant/bin/stormLocal.sh`

	Starts the public Storm topologies.
	
1.	`/vagrant/bin/stormEnterpriseLocal.sh`

	Starts the enterprise Storm topologies.
	
1.	`/vagrant/bin/900_Server.sh`

	Starts the Lumify web server.

## Lumify URLs

The following URLs can be used to view and manage the Lumify web application and services once the `dev` VM is running.

*	[Lumify Application](https://192.168.33.10:8443)
*	[Hadoop Administration](http://192.168.33.10:50070/dfshealth.jsp)
*	[Storm UI](http://192.168.33.10:8081)

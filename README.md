red-dawn
========

open analytic platform

Vagrant
=======
From your checked out `red-dawn` directory:

1. install [VitualBox](https://www.virtualbox.org/wiki/Downloads)
1. install the [VirtualBox Extension Pack](https://www.virtualbox.org/wiki/Downloads)
1. install [Vagrant](http://docs.vagrantup.com/v2/installation/)
1. `vagrant box add centos6.4 http://developer.nrel.gov/downloads/vagrant-boxes/CentOS-6.4-x86_64-v20130427.box`
1. `vagrant up`
1. `vagrant ssh`

NLP Storm stuff
===============

-Models are needed, find them here:

http://opennlp.sourceforge.net/models-1.5/

-Topology, Spout, and initial bolt are specifically written for a handful of Orion Data Layer ASCII formatted files. Need to develop bolts to handle other formats

Annoying gotcha:

-The storm-contrib-mongo project references a parent pom that is not deployed into a repository. Here is how I fixed it (not ideal, but it worked)

1. Download the parent pom to a storm-contrib directory [here](https://raw.github.com/nathanmarz/storm-contrib/master/pom.xml "Parent POM")  
2. Comment out the modules  
3. mvn install it


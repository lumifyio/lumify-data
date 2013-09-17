turning it on
=============

aws
---
1. manually create required VPC, subnet, security group, and ssh keypair AWS resources
1. configure the [aws](https://github.com/dsingley/aws) scripts
1. define the cluster nodes in an `*_instances.txt` file
1. use `bin/spinup.rb` to instantiate the EC2 resources defined in the `*_instances.txt` file

puppet
------
1. define the software configuration for the cluser nodes in a Puppet manifest file (e.g. `puppet/manifests/lumify_demo.pp`)

aws-deploy
----------
1. manually configure an Elastic IP for the puppet server
1. manually configure an Elastic IP for the web server
1. add a security group to the web server that allows inbound traffic:

```
ec2-describe-instances --filter 'tag:aliases=www*' | awk '/^INSTANCE/ {print $2} /^GROUP/ {print $2, $3}'
ec2-describe-group --filter 'group-name=http*'
ec2-modify-instance-attribute <instance id> --group-id <existing group id> --group-id <existing group id> --group-id <new group id>
```

1. push our software to the puppet server: `./push.sh <puppet-server-elastic-ip> ../aws/cluster_name_hosts`
1. ssh to the puppet server (forwarding your ssh agent): `ssh -A root@<puppet-server-elastic-ip>`
1. install our software on the puppet server and via puppet on all the other cluster nodes: `./init.sh cluster_name_hosts`
1. monitor the progress of the other nodes: `tail -f run_puppet.*.log`
1. configure ssh for the proxy users: `./setup_ssh.ssh cluster_name_hosts`
1. start and perform initial setup of our services: `./start_all.sh cluster_name_hosts`
1. ssh from the puppet server to the Hadoop namenode and Accumulo master servers when prompted to format and init

setup
=====

1. ssh from the puppet server to the Hadoop namenode to populate the HDFS /conf directory, stage GeoNames data, and setup Oozie:

```
mkdir /data0/import
mv *.tgz /data0/import
mv setup_conf.sh setup_geonames.sh setup_oozie.sh setup_import.sh /data0/import
chown -R hdfs:hdfs /data0/import
su - hdfs
cd /data0/import
./setup_conf.sh
./setup_geonames.sh http://10.0.3.10:8080
./setup_oozie.sh
```

1. ssh from the puppet server to the Haddop namenode to start Oozie: `service oozie start`
1. ssh from the puppet server to the Haddop namenode to enable the Ooozie web interface: `cp -r /usr/lib/oozie/libext/ext-2.2 /var/lib/oozie/oozie-server/webapps/oozie`
1. ssh from the puppet server to the Hadoop namenode to ingest the geonames data:

```
su - hdfs
cd /data0/import
oozie job -oozie http://localhost:11000/oozie \
          -config jobs/job-common.properties \
          -Doozie.wf.application.path='${nameNode}/user/${user.name}/${workflowRoot}/geonames-import' \
          -run
```

1. browse to the web app and use the admin page to upload an ontology: http://<web-server-elastic-ip/admin/uploadOntology.html

ingest data
===========

*option 1:* web upload and `oozie aggregate/mr-jobs`

*option 2:* stage in HDFS `/import/1-ready` with `oozie coordinators/file-import` and `oozie aggregate/mr-jobs`

turning it off
==============
1. use `bin/list.rb cluster_name` in [aws](https://github.com/dsingley/aws) to identify the EC2 instances
1. run `ec2-terminate-instances <the list of instance ids reported>`
1. use `bin/list.rb cluster_name` again to list orphaned EBS volumes
1. run the provided for-loop with `ec2-delete-volume` in place of `echo`

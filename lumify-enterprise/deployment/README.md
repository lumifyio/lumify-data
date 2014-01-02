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

- ssh from the puppet server to the namenode and run:

```
/root/setup_config.sh
```

- ssh from the puppet server to one of the kafka servers and run:

```
/opt/lumify/kafka-clear.sh --zookeeper 10.0.3.101:2181
```

- ssh from the puppet server to storm nimbus server and run:

```
n=5

/opt/storm/bin/storm jar lumify-storm-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.altamiracorp.lumify.storm.StormRunner -tpb $((${n} * 8)) -w $((${n} * 2)) -ph $((${n} * 4))

/opt/storm/bin/storm jar lumify-enterprise-storm-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.altamiracorp.lumify.storm.StormEnterpriseRunner -tpb $((${n} * 8)) -w $((${n} * 2)) -ph $((${n} * 4))

/opt/storm/bin/storm jar lumify-twitter-1.0-SNAPSHOT-jar-with-dependencies.jar \
  com.altamiracorp.lumify.storm.twitter.StormRunner -tpb $((${n} * 8)) -w $((${n} * 2)) -ph $((${n} * 4))
```

turning it off
==============
1. use `bin/list.rb cluster_name` in [aws](https://github.com/dsingley/aws) to identify the EC2 instances
1. run `ec2-terminate-instances <the list of instance ids reported>`
1. use `bin/list.rb cluster_name` again to list orphaned EBS volumes
1. run the provided for-loop with `ec2-delete-volume` in place of `echo`

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
1. define the software configuration for the cluser nodes in a Puppet manifest file (e.g. `puppet/manifests/reddawn_demo.pp`)

aws-deploy
----------
1. manually configure an Elastic IP for the puppet server
1. push our software to the puppet server: `./push.sh <puppet-server-elastic-ip> ../aws/cluster_name_hosts`
1. ssh to the puppet server (forwarding your ssh agent): `ssh -A root@<puppet-server-elastic-ip>`
1. install out software on the puppet server all the other cluster nodes: `./init.sh cluster_name_hosts`
1. monitor the progress of the other nodes: `tail -f run_puppet.*.log`
1. manually configure an Elastic IP for the web server
1. add a security group to the web server that allows inbound traffic:

```
ec2-describe-instances --filter 'tag:aliases=www*' | awk '/^INSTANCE/ {print $2} /^GROUP/ {print $2, $3}'
ec2-describe-group --filter 'group-name=http*'
ec2-modify-instance-attribute <instance id> --group-id <existing group id> --group-id <existing group id> --group-id <new group id>
```

turning it off
==============
1. use `bin/list.rb cluster_name` in [aws](https://github.com/dsingley/aws) to identify the EC2 instances
1. run `ec2-terminate-instances <the list of instance ids reported>`
1. use `bin/list.rb cluster_name` again to list orphaned EBS volumes
1. run the provided for-loop with `ec2-delete-volume` in place of `echo`

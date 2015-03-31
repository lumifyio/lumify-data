# Cluster Control

From the puppet master machine, utilize the control.sh shell script.

** A text file must be created that defines the cluster nodes and their role(s) **
Example file structure:

```
10.0.3.60    ip-10-0-3-60.ec2.internal    ip-10-0-3-60 puppet proxy syslog                                                              10.0.3.61    ip-10-0-3-61.ec2.internal    ip-10-0-3-61 namenode resourcemanager secondarynamenode accumulomaster rabbitmq61 node61 es61 zk61 www61
```

Execute `./control.sh <hosts_file>` to see the possible options to execute on the cluster

Clearing common logs across the cluster:
`./control.sh <hosts_file> rmlogs`

Restarting cluster services:
`./control.sh <hosts_file> restart`


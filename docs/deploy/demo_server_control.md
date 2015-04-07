# Demo Server Control

This document describes common scenarios for troubleshooting the demo server `try.lumify.io`. The same scripts can be applied to other demo instances deployed in EC2 and configured with the [Initial Deployment](initial_deployment.html) instructions.

## Nodes

This document assumes a single node cluster (with a separate puppet master). If operating in a multi-node cluster, commands executed on the worker node should be run on the node running the targeted service. All commands run on the puppet master should always be run on the puppet master.

## Hosts File

The hosts file describes the nodes and their functions for a particular demo cluster. It is used by the `control.sh` script to ensure that commands are executed on the appropriate machine. The hosts file should be located in the `/root` folder of the puppet master and will be named `demo_0N_hosts` where `N` is the demo cluster ID. In all commands, `demo_0N_hosts` should be replaced with the correct hosts file.

## SSH

### Puppet Master

```
<local> $ ssh -A root@puppet.try.lumify.io
```

### Demo Server

```
<local> $ ssh -A root@try.lumify.io
```

## Service Control

All services can be stopped and started by the `control.sh` script on the puppet master. This script is found in the `/root` directory. It expects commands in the following format:

```
<puppet> $ ./control.sh demo_0N_hosts <command> [service]
```

Running `control.sh` without arguments will provide a list of the available commands and service names. If the service name is omitted, the command will be applied to all known services.

### Examples

```
// Stop all services
<puppet> $ ./control.sh demo_0N_hosts stop

// Start all services
<puppet> $ ./control.sh demo_0N_hosts start

// Restart all services
<puppet> $ ./control.sh demo_0N_hosts restart

// Delete all log files
<puppet> $ ./control.sh demo_0N_hosts rmlogs

// Restart the Jetty webserver
<puppet> $ ./control.sh demo_0N_hosts restart jetty
```

## Yarn Control

Lumify requires two yarn jobs: `graph-property-worker` and `long-running-process`. These jobs are controlled directly from the demo server, but must be run as the `yarn` user.

### List Running Jobs

```
<root@demo> $ su - yarn -c "yarn application -list"

Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF-8
15/04/07 14:54:18 INFO client.RMProxy: Connecting to ResourceManager at namenode/10.0.3.71:8032
Total number of applications (application-types: [] and states: [SUBMITTED, ACCEPTED, RUNNING]):2
                Application-Id	    Application-Name	    Application-Type	      User	     Queue	             State	       Final-State	       Progress	                       Tracking-URL
application_1428415231500_0001	graph-property-worker	                YARN	      yarn	 root.yarn	           RUNNING	         UNDEFINED	             0%	                                N/A
application_1428415231500_0002	long-running-process	                YARN	      yarn	 root.yarn	           RUNNING	         UNDEFINED	             0%	                                N/A

```
The above output indicates both jobs are running. If either is not, they will need to be started.

### Stop a Running Job

List the running jobs and note the `Application-Id` for the job you want to stop. This will be referred to as `$APPLICATION_ID`

```
<root@demo> $ su - yarn -c "yarn application -kill $APPLICATION_ID"
```

### Start Jobs

Use one or both of these commands to start the jobs that are not running. Replace ${VERSION} with the current Lumify version.

```
<root@demo> $ su - yarn

// long-running-process job
<yarn@demo> $ yarn jar /tmp/yarn/lumify-long-running-process-yarn-${VERSION}-with-dependencies.jar -jar /tmp/yarn/lumify-long-running-process-yarn-${VERSION}-with-dependencies.jar

// graph-property-worker job
<yarn@demo> $ yarn jar /tmp/yarn/lumify-graph-property-worker-yarn-${VERSION}-with-dependencies.jar -jar /tmp/yarn/lumify-graph-property-worker-yarn-${VERSION}-with-dependencies.jar -envPATH=$PATH -envLD_LIBRARY_PATH=/usr/local/lib:/usr/local/share/OpenCV/java
```

## Restart Demo Environment

The commands below can be used to fully clean and restart the demo environment.

```
<local> $ ssh -A root@puppet.try.lumify.io
<root@puppet> $ ./control.sh demo_0N_hosts stop
<root@puppet> $ ./control.sh demo_0N_hosts rmlogs
<root@puppet> $ ./control.sh demo_0N_hosts start
<root@puppet> $ exit

<local> $ ssh -A root@try.lumify.io
<root@demo> $ su - yarn
<yarn@demo> $ yarn application -list
// if jobs are running, kill them
<yarn@demo> $ yarn application -kill $APPLICATION_ID
// restart yarn jobs
<yarn@demo> $ yarn jar /tmp/yarn/lumify-long-running-process-yarn-${VERSION}-with-dependencies.jar -jar /tmp/yarn/lumify-long-running-process-yarn-${VERSION}-with-dependencies.jar
<yarn@demo> $ yarn jar /tmp/yarn/lumify-graph-property-worker-yarn-${VERSION}-with-dependencies.jar -jar /tmp/yarn/lumify-graph-property-worker-yarn-${VERSION}-with-dependencies.jar -envPATH=$PATH -envLD_LIBRARY_PATH=/usr/local/lib:/usr/local/share/OpenCV/java
<yarn@demo> $ exit
<root@demo> $ exit
```

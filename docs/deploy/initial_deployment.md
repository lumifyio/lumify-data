# Deploying a Demo Environment on EC2

Follow the instructions below to build and deploy a single or multi-node Lumify cluster on Amazon EC2. Note that both single and multi-node deployments require a separate puppet master server.

## Conventions

The following replacement values are used throughout this document.

Symbol        | Value
--------------|----------
`X`           | Node function (see **IP Addresses**)
`Y`           | Cluster ID (see **IP Addresses**)
`Z`           | Instance number (see **IP Addresses**)
`$LUMIFY_ALL` | Local directory where lumify-all project is cloned
`$SEC_GRAPH`  | Local directory where secure-graph project is cloned
`$PUPPET_IP`  | The Elastic IP assigned to the Puppet Master
`$NODE_IP`    | The Elastic IP assigned to the worker/web node in a single node cluster
`${VERSION}`  | The Lumify or SecureGraph versionn)

### IP Addresses

Lumify uses a coded IP address scheme to quickly identify the purpose of a node and the cluster it belongs to. All nodes are contained in the `demo` Virtual Private Cloud (VPC) with subnet `10.0.3.0/255`. The final octet, `XYZ`, is coded as follows:

-	`X`, the hundreds place, indicates the server function and is only present for multi-node deployments
-	`Y`, the tens place, indicates the cluster the machine belongs to
-	`Z`, the ones place, indicates the instance number

Octet    | Meaning
---------|------------
 0yz     | Single node deployment, typically omitted (e.g. `10.0.3.60`)
 1yz     | Worker Node
 2yz     | Web Server Node
 xYz     | Node belongs to demo cluster `Y`
 xY0     | Puppet master for demo cluster `y`
 xy[1-9] | Instance 1-9 of the worker/webserver nodes in demo cluster `y`

##### Examples
-	`10.0.3.60`: Puppet master for cluster 6
-	`10.0.3.61`: Single node instance for cluster 6
-	`10.0.3.113`: Worker instance 3 for cluster 1
-	`10.0.3.211`: Web Server instance 1 for cluster 1

### Names

Amazon's `Name` tag should be configured as follows for instances, volumes and security groups.

-	**Instance**: `ip-10-0-3-XYZ`
-	**Volumes**:  `ip-10-0-3-XYZ_volN` (`N` is the volume number [0-9])
-	**Security Group**: `lumify_demo_0Y` (default group for the cluster, others may also be applied)

### Aliases

Each instance should have an `aliases` tag indicating the function(s) of the node. Multiple functions should be specified as a comma-separated list.

Alias               | Purpose
--------------------|----------
`puppet`            | Puppet Master
`proxy`             | Web Proxy
`syslog`            | Log Server
`namenode`          | Hadoop Name Node
`secondarynamenode` | Hadoop Secondary Name Node
`resourcemanager`   | Hadoop Resource Manager
`accumulomaster`    | Accumulo Master
`rabbitmqYZ`        | RabbitMQ Node `Z` for cluster `Y`
`nodeYZ`            | Hadoop/Accumulo Node `Z` for cluster `Y`
`esYZ`              | ElasticSearch Node `Z` for cluster `Y`
`zkYZ`              | Zookeeper Node `Z` for cluster `Y`
`wwwYZ`             | Web Server `Z` for cluster `Y`

---

## Lumify Preparation

-	Clone [`lumify-all`](https://github.com/altamiracorp/lumify-all) into `$LUMIFY_ALL`
-	Clone [`lumify`](https://github.com/lumifyio/lumify) as `$LUMIFY_ALL/lumify-public`
-	Clone [`secure-graph`](https://github.com/lumifyio/securegraph) into `$SEC_GRAPH`
	
	```
	git clone ssh://git@github.com/altamiracorp/lumify-all
	cd lumify-all
	git clone ssh://git@github.com/lumifyio/lumify lumify-public
	cd ..
	git clone ssh://git@github.com/lumifyio/securegraph
	```
	
-	Build all `lumify` artifacts
	
	```
	cd $LUMIFY_ALL/lumify-public
	mvn -DskipTests=true -Pweb-war clean install
	```
	
-	Build all SecureGraph artifacts

	```
	<local> $ cd $SEC_GRAPH
	<local> $ mvn -DskipTests=true clean install
	```
-	Create a directory for the cluster deployment and copy files needed for deployment.

	```
	<local> $ cd $LUMIFY_ALL
	<local> $ mkdir deployment/lumify_demo_0Y
	
	<local> $ cp lumify-public/web/war/target/lumify-web-war-${VERSION}.war deployment/lumify_demo_0Y/lumify.war
	<local> $ cp lumify-public/tools/cli/target/lumify-cli-${VERSION}-with-dependencies.jar \
	          deployment/lumify_demo_0Y
	<local> $ cp $SEC_GRAPH/securegraph-elasticsearch-plugin/target/release/elasticsearch-securegraph-${VERSION}.zip \
	          deployment/lumify_demo_0Y
	          
	<local> $ mkdir deployment/lumify_demo_0Y/weblib
	<local> $ cp lumify-public/web/plugins/terms-of-use/target/lumify-terms-of-use-${VERSION}.jar \
			  lumify-public/web/plugins/auth-social/target/lumify-auth-social-${VERSION}-jar-with-dependencies.jar \
			  lumify-public/web/plugins/dev-tools/target/lumify-web-dev-tools-${VERSION}.jar \
			  lumify-public/core/plugins/model-bigtable/target/lumify-model-bigtable-${VERSION}-jar-with-dependencies.jar \
	          deployment/lumify_demo_0Y/weblib
	          
	<local> $ mkdir deployment/lumify_demo_0Y/gpw
	<local> $ cp `find lumify-public/graph-property-worker/plugins -name "lumify-gpw-*-with-dependencies.jar"` \
	          deployment/lumify_demo_0Y/gpw
	          
	<local> $ mkdir deployment/lumify_demo_0Y/yarn
	<local> $ cp lumify-public/tools/long-running-process-yarn/target/lumify-long-running-process-yarn-${VERSION}-with-dependencies.jar \
	             lumify-public/graph-property-worker/graph-property-worker-yarn/lumify-graph-property-worker-yarn-${VERSION}-with-dependencies.jar \
	             deployment/lumify_demo_0Y/yarn
	             
	<local> $ mkdir deployment/lumify_demo_0Y/jai
	<local> $ cd deployment/lumify_demo_0Y/jai
	<local> $ wget https://bits.lumify.io/extra/jai-1_1_3-lib-linux-amd64-jdk.bin
	<local> $ wget https://bits.lumify.io/extra/jai_imageio-1_1-lib-linux-amd64-jdk.bin
	```
---

## Cluster Setup

All steps below are taken from Amazon's EC2 management console.

### Elastic IPs

Each demo cluster needs a minimum of 2 Elastic IP addresses, one for the Puppet Master and one for the web server or load balancer.

-	Open the `Elastic IPs` console (`Network & Security` group)
-	Use the `Allocate New Address` to create the required addresses. You can use existing addresses if they are not currently assigned to an instance.

### Security Group

Create the default security group for the new cluster. This will allow all nodes
in the cluster to connect to one another.

-	Open the `Security Groups` console (`Network & Security` group)
-	Select `Create Security Group`
	-	**Security Group Name**: `lumify_demo_0Y`
	-	**Description**`: `lumify_demo_0Y`
	-	**VPC**: `vpc-13f7ba76 (10.0.0.0/16) | Demo VPC`
	-	**`Create`**
-	Edit the `Name` tag for the new group, set it to `lumify_demo_0Y`
-	Select the new group so its details are visible in the bottom panel
-	Click the `Inbound` tab
-	Select `Edit`
	-	**Type**: `Custom TCP Rule`
	-	**Protocol**: `TCP`
	-	**Port Range**: `0-65535`
	-	**Source**: `Custom IP`
	-	In the IP box, begin typing the Security Group name (`lumify_demo_0Y`) and select that group from the auto-complete box.
	-	**`Save`**

### Puppet Master

Both single and multi-node clusters require a separate Puppet Master node.

-	Open the `Instances` console (`Instances` group)
-	Select an existing puppet master instance (`10.0.3.Y0`) and, in the `Actions` drop-down, choose `Launch More Like This`.
	-	Select `3. Configure Instance`
	-	Under `Network Interfaces`, assign the IP `10.0.3.Y0` to interface `eth0`
	-	Select `4. Add Storage`
	-	Verify the Root drive
		-	**Type**: `Root`
		-	**Device**: `/dev/sda`
		-	**Size (GiB)**: `8`
		-	**Volume Type**: `General Purpose (SSD)`
	-	Select `5. Tag Instance`
		-	**Name**: `ip-10-0-3-Y0`
		-	**Project**: `lumify_demo_0Y`
		-	**aliases**: `puppet,proxy,syslog`
	-	Select `6. Configure Security Group`
	-	Assign to the groups:
		-	`lumify_demo_0Y`
		-	`ssh`
	-	Select `7. Review`
	-	Verify all settings look correct
	-	`Launch`
		-	**Choose an existing key pair**: `demo`
		-	Click acknowledgement
		-	`Launch Instances`
-	Open `Volumes` console (`Elastic Block Store` group)
-	Verify a drive is connected to `10.0.3.Y0`
-	Edit `Name` tag, if necessary, setting it to `ip-10-0-3-Y0_vol0`
-	Open `Elastic IPs` console (`Network & Security` group)
-	Select an unassigned IP address (allocate a new one if necessary)
-	`Associate Address`
	-	**Instance**: `ip-10-0-3-Y0` (select from auto-complete)
	-	`Associate`

### Single Node

In a single node cluster, there will only be one node that doubles as both a Worker and a Web Server. It will be assigned the IP address `10.0.3.Y1`.

-	Open the `Instances` console (`Instances` group)
-	Select an existing single node instance (`10.0.3.Y1`) and, in the `Actions` drop-down, choose `Launch More Like This`.
	-	Select `3. Configure Instance`
	-	Under `Network Interfaces`, assign the IP `10.0.3.Y1` to interface `eth0`
	-	Select `4. Add Storage`
	-	Verify the Root drive
		-	**Type**: `Root`
		-	**Device**: `/dev/sda`
		-	**Size (GiB)**: `8`
		-	**Volume Type**: `General Purpose (SSD)`
	-	`Add New Volume`
		-	**Type**: `EBS`
		-	**Device**: `/dev/sdf`
		-	**Size (GiB)**: `1024`
		-	**Volume Type**: `General Purpose (SSD)`
		-	**Delete on Termination**: `Off`
		-	**Encrypted**: `Off`
	-	Select `5. Tag Instance`
		-	**Name**: `ip-10-0-3-Y1`
		-	**Project**: `lumify_demo_0Y`
		-	**aliases**: `namenode,resourcemanager,secondarynamenode,accumulomaster,rabbitmqY1,nodeY1,esY1,zkY1,wwwY1`
	-	Select `6. Configure Security Group`
	-	Assign to the groups:
		-	`lumify_demo_0Y`
		-	`ssh`
		-	`http-and-https`
	-	Select `7. Review`
	-	Verify all settings look correct
	-	`Launch`
		-	**Choose an existing key pair**: `demo`
		-	Click acknowledgement
		-	`Launch Instances`
-	Open `Volumes` console (`Elastic Block Store` group)
-	Verify an 8 GiB drive is connected to `10.0.3.Y1`
	-	Edit `Name` tag, if necessary, setting it to `ip-10-0-3-Y1_vol0`
-	Verify a 1024 GiB drive is connected to `10.0.3.Y1`
	-	Edit `Name` tag, if necessary, setting it to `ip-10-0-3-Y1_vol1`
-	Open `Elastic IPs` console (`Network & Security` group)
-	Select an unassigned IP address (allocate a new one if necessary)
-	`Associate Address`
	-	**Instance**: `ip-10-0-3-Y1` (select from auto-complete)
	-	`Associate`

---

## Lumify Deployment

### Hosts Files

A hosts file must be created that defines the nodes in the cluster and their roles.

-	Create `$LUMIFY_ALL/deployment/lumify_demo_0Y/demo_0Y_hosts` with the following contents (one line per node):

	```
	10.0.3.Y0    ip-10-0-3-Y0.ec2.internal    ip-10-0-3-Y0 puppet proxy syslog
	10.0.3.Y1    ip-10-0-3-Y1.ec2.internal    ip-10-0-3-Y1 namenode resourcemanager secondarynamenode accumulomaster rabbitmqY1 nodeY1 esY1 zkY1 wwwY1
	```

### Push to Puppet Master

-	Execute `push.sh` and copy artifacts to the puppet master

	```
	<local> $ cd $LUMIFY_ALL/deployment
	<local> $ ./push.sh $PUPPET_IP lumify_demo_0Y/demo_0Y_hosts
	<local> $ scp -r lumify.xml lumify_demo_0Y/* root@PUPPET_IP:~
	```

-	Execute the following to configure a single node cluster.
	
	 _If the `puppet agent` command reports there is already a puppet configuration running, continue with the file copies until it is done. The `puppet agent` command must be run before continuing to HDFS configuration._

	```
	<local>   $ ssh -A root@$PUPPET_IP
	<puppet>  $ ./init.sh demo_0Y_hosts
	
	<puppet>  $ scp lumify-cli-${VERSION}-with-dependencies.jar root@10.0.3.Y1:~
	<puppet>  $ ssh -A 10.0.3.Y1
	<worker>  $ puppet agent -t
	
	// if worker node is a web server
	<worker>  $ mkdir -p /opt/lumify/config /opt/lumify/lib
	<worker>  $ ssh -A $EXISTING_WEB_INSTANCE
	<oth-web> $ cd /opt/lumify/config
	<oth-web> $ scp google-analytics.properties lumify-oauth.properties \
	            lumify-terms-of-use.properties z-sinaloa.properties \
	            root@10.0.3.Y1:/opt/lumify/config
	<oth-web> $ exit
	<worker>  $ exit
	<puppet>  $ scp weblib/* root@10.0.3.Y1:/opt/lumify/lib
	<puppet>  $ scp gpw/* root@10.0.3.Y1:~
	<puppet>  $ scp -r yarn root@10.0.3.Y1:/tmp
	<puppet>  $ scp elasticsearch-securegraph-${VERSION}.zip root@10.0.3.Y1:~
	
	// puppet agent -t must complete before this section is executed
	<puppet>  $ scp jai/* root@10.0.3.Y1:/usr/java/default/
	<puppet>  $ ssh -A 10.0.3.Y1
	<worker>  $ cd /usr/java/default
	<worker>  $ chmod u+x jai*.bin
	<worker>  $ ./jai-1_1_3-lib-linux-amd64-jdk.bin && ./jai_imageio-1_1-lib-linux-amd64-jdk.bin
	<worker>  $ exit
	```

-	Configure HDFS and Accumulo

	```
	<puppet> $ ./control.sh demo_0Y_hosts first
	
	// in separate session (2)
	<2.local>  $ ssh -A root@$NODE_IP
	<2.worker> $ su - hdfs
	<2.worker> $ hdfs namenode -format
	<2.worker> $ exit
	// type yes to continue control.sh script
	
	// when prompted by control.sh
	<2.worker> $ su - accumulo
	<2.worker> $ /opt/accumulo/bin/accumulo init
	// when prompted by init script
	// Instance name: lumify
	// root password: password
	<2.worker> $ exit
	// type yes to continue control.sh script
	
	// after control.sh completes
	<2.worker> $ ./setup_hdfs.sh
	<2.worker> $ ./setup_config.sh
	<2.worker> $ ./setup_libcache.sh
	```

-	Configure ElasticSearch

	```
	<worker> $ /usr/share/elasticsearch/bin/plugin -r securegraph
	<worker> $ /usr/share/elasticsearch/bin/plugin -u file:///root/elasticsearch-securegraph-${VERSION}ERSION.zip -i securegraph
	<puppet> $ ./control.sh demo_0Y_hosts restart elasticsearch
	```

-	Restart the web server

	```
	<puppet> $ ./control.sh demo_0Y_hosts restart jetty
	```

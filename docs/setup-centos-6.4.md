# Lumify on CentOS 6.4

## Performance Considerations

1. at least 2 CPU cores
1. at least 4GB of RAM
1. at least 8GB of disk space

## Network Considerations

1. **a single bridged network interface is recommended** when running as a desktop virtual machine
1. documentation and sample configuration files will use the IP address `192.168.33.10`
1. where possible these instructions will automate configuring the system to use your actual IP address

### Disable IPTables

*as root:*

        chkconfig iptables off
        service iptables stop


## Install Dependencies

### Java 6

1. browse to http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase6-419409.html
1. find `jdk-6u45-linux-x64-rpm.bin`
1. accept the license agreement
1. click on `jdk-6u45-linux-x64-rpm.bin`
1. login and the file will download

*as root:*

        cd ~
        chmod u+x jdk-6u45-linux-x64-rpm.bin
        ./jdk-6u45-linux-x64-rpm.bin
        echo "export JAVA_HOME=/usr/java/jdk1.6.0_45/jre; export PATH=\$JAVA_HOME/bin:\$PATH" > /etc/profile.d/java.sh


### EPEL Repository

*as root:*

        rpm -ivH http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm


### Git, NodeJS, NPM, and NPM modules

*as root:*

        yum install -y git nodejs npm

        npm install -g bower
        npm install -g inherits
        npm install -g grunt
        npm install -g grunt-cli


### Maven

*as root:*

        cd ~
        curl http://archive.apache.org/dist/maven/binaries/apache-maven-3.0.5-bin.tar.gz -O
        cd /opt
        tar xzf ~/apache-maven-3.0.5-bin.tar.gz
        ln -s apache-maven-3.0.5 maven
        echo "export MVN_HOME=/opt/maven; export PATH=\$MVN_HOME/bin:\$PATH" > /etc/profile.d/maven.sh


## Install Servers

### Hadoop and Zookeeper

*as root:*

        cd ~
        curl http://archive.cloudera.com/cdh4/one-click-install/redhat/6/x86_64/cloudera-cdh-4-0.x86_64.rpm -O
        rpm -ivH cloudera-cdh-4-0.x86_64.rpm
        yum install -y hadoop-0.20-conf-pseudo zookeeper-server

        mkdir -p /var/lib/hadoop-hdfs/cache/hdfs/dfs/{name,namesecondary,data}
        chown hdfs:hdfs /var/lib/hadoop-hdfs/cache/hdfs/dfs/{name,namesecondary,data}
        
        service zookeeper-server init
        # The following warning is expected (and ok):
        # No myid provided, be sure to specify it in /var/lib/zookeeper/myid if using non-standalone

        ip_address=$(ip addr show eth0 | awk '/inet / {print $2}' | cut -d / -f 1)
        echo "${ip_address} $(hostname)" >> /etc/hosts

        sed -i -e "s/localhost:8020/${ip_address}:8020/" /usr/lib/hadoop/etc/hadoop/core-site.xml

        vi /usr/lib/hadoop/etc/hadoop/hdfs-site.xml
        # add the following in the configuration element:
          <property>
            <name>dfs.permissions.enabled</name>
            <value>false</value>
          </property>
          <property>
            <name>dfs.datanode.synconclose</name>
            <value>true</value>
          </property>


### Accumulo

*as root:*

        useradd -g hadoop accumulo

        cd ~
        curl http://www.us.apache.org/dist/accumulo/1.5.1/accumulo-1.5.1-bin.tar.gz -O
        cd /usr/lib
        tar xzf ~/accumulo-1.5.1-bin.tar.gz
        ln -s accumulo-1.5.1 accumulo
        cp accumulo/conf/examples/1GB/standalone/* accumulo/conf
        
        chown -R accumulo:hadoop accumulo/
        
        sed -i -e "s|HADOOP_PREFIX=/path/to/hadoop|HADOOP_PREFIX=/usr/lib/hadoop|" \
               -e "s|JAVA_HOME=/path/to/java|JAVA_HOME=/usr/java/jdk1.6.0_45/jre|" \
               -e "s|ZOOKEEPER_HOME=/path/to/zookeeper|ZOOKEEPER_HOME=/usr/lib/zookeeper|" \
          accumulo/conf/accumulo-env.sh

        vi accumulo/conf/accumulo-site.xml
        # 1) follow the instructions in the comment in the general.classpaths property

        vi accumulo/conf/accumulo-site.xml
        # 2) add the following in the configuration element:
          <property>
              <name>instance.dfs.uri</name>
              <value>hdfs://192.168.33.10:8020</value>
           </property>

        ip_address=$(ip addr show eth0 | awk '/inet / {print $2}' | cut -d / -f 1)
        sed -i -e "s/192.168.33.10/${ip_address}/" accumulo/conf/accumulo-site.xml
        sed -i -e "s/localhost/${ip_address}/" accumulo/conf/monitor


### Elasticsearch

*as root:*

        useradd -g hadoop esearch

        cd ~
        curl https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.1.2.tar.gz -O
        cd /usr/lib
        tar xzf ~/elasticsearch-1.1.2.tar.gz
        ln -s elasticsearch-1.1.2 elasticsearch
        
        chown -R esearch:hadoop elasticsearch/

        elasticsearch/bin/plugin install org.securegraph/securegraph-elasticsearch-plugin/0.6.0
        elasticsearch/bin/plugin install mobz/elasticsearch-head


### Storm

*as root:*

        cd ~
        curl http://www.us.apache.org/dist/incubator/storm/apache-storm-0.9.2-incubating/apache-storm-0.9.2-incubating.tar.gz -O
        cd /opt
        tar xzf ~/apache-storm-0.9.2-incubating.tar.gz
        ln -s apache-storm-0.9.2-incubating storm

        mkdir storm/logs

        ip_address=$(ip addr show eth0 | awk '/inet / {print $2}' | cut -d / -f 1)
        echo "storm.zookeeper.servers:" >> /opt/storm/conf/storm.yaml
        echo " - ${ip_address}" >> /opt/storm/conf/storm.yaml
        echo "nimbus.host: ${ip_address}" >> /opt/storm/conf/storm.yaml
        echo "supervisor.slots.ports: [6700, 6701, 6702, 6703]" >> /opt/storm/conf/storm.yaml
        echo "ui.port: 8081" >> /opt/storm/conf/storm.yaml


### RabbitMQ

*as root:*

        yum install -y erlang

        cd ~
        curl http://www.rabbitmq.com/releases/rabbitmq-server/v3.2.3/rabbitmq-server-3.2.3-1.noarch.rpm -O
        rpm -ivH rabbitmq-server-3.2.3-1.noarch.rpm

        /usr/sbin/rabbitmq-plugins enable rabbitmq_management
        service rabbitmq-server restart


### Jetty with SSL support

*as root:*

        cd ~
        curl -L 'http://eclipse.org/downloads/download.php?file=/jetty/stable-8/dist/jetty-distribution-8.1.15.v20140411.tar.gz&r=1' \
             -o jetty-distribution-8.1.15.v20140411.tar.gz
        cd /opt
        tar xzf ~/jetty-distribution-8.1.15.v20140411.tar.gz
        ln -s jetty-distribution-8.1.15.v20140411 jetty

        mkdir -p /opt/lumify/config
        /usr/java/default/bin/keytool -genkeypair -keysize 2048 -keyalg RSA -keypass password -storepass password \
                                      -dname CN=lumify -keystore /opt/lumify/config/jetty.jks

        vi jetty/etc/jetty.xml
        # add the following after the existing addConnector call
          <Call name="addConnector">
            <Arg>
              <New class="org.eclipse.jetty.server.ssl.SslSelectChannelConnector">
                <Arg>
                  <New class="org.eclipse.jetty.http.ssl.SslContextFactory">
                    <Set name="keyStore">/opt/lumify/config/jetty.jks</Set>
                    <Set name="keyStorePassword">password</Set>
                    <Set name="trustStore">/opt/lumify/config/jetty.jks</Set>
                    <Set name="trustStorePassword">password</Set>
                    <Set name="needClientAuth">false</Set>
                  </New>
                </Arg>
                <Set name="host"><Property name="jetty.host" /></Set>
                <Set name="port"><Property name="jetty.port" default="8443"/></Set>
                <Set name="maxIdleTime">300000</Set>
                <Set name="Acceptors">2</Set>
                <Set name="statsOn">false</Set>
                <Set name="lowResourcesConnections">20000</Set>
                <Set name="lowResourcesMaxIdleTime">5000</Set>
              </New>
            </Arg>
          </Call>

        mkdir -p jetty/contexts-DISABLED
        mv jetty/contexts/* jetty/contexts-DISABLED
        mkdir -p jetty/webapps-DISABLED
        mv jetty/webapps/* jetty/webapps-DISABLED


## Setup

### format and initialize Hadoop HDFS and Accumulo

*as root:*

        sudo -u hdfs hdfs namenode -format
        service hadoop-hdfs-namenode start
        service hadoop-hdfs-secondarynamenode start
        service hadoop-hdfs-datanode start

        service zookeeper-server start

        sudo -u accumulo /usr/lib/accumulo/bin/accumulo init --instance-name lumify --password password


### start services

*as root:*

        service hadoop-hdfs-namenode start
        service hadoop-hdfs-secondarynamenode start
        service hadoop-hdfs-datanode start

        service zookeeper-server start

        sudo -u accumulo /usr/lib/accumulo/bin/start-all.sh

        sudo -u esearch /usr/lib/elasticsearch/bin/elasticsearch -d

        service rabbitmq-server start

        /opt/storm/bin/storm nimbus 2>&1 > /opt/storm/logs/nimbus-console.out &
        /opt/storm/bin/storm supervisor 2>&1 > /opt/storm/logs/supervisor-console.out &

### clone the Lumify projects

        cd ~
        git clone https://github.com/lumifyio/lumify-root.git
        git clone https://github.com/lumifyio/lumify.git


### configure Lumify

        sudo mkdir -p /opt/lumify/{config,lib,logs}

        cd ~/lumify
        sudo cp docs/{lumify.properties,log4j.xml} /opt/lumify/config

        ip_address=$(ip addr show eth0 | awk '/inet / {print $2}' | cut -d / -f 1)
        sudo sed -i -e "s/192.168.33.10/${ip_address}/" /opt/lumify/config/lumify.properties

        /usr/lib/hadoop/bin/hadoop fs -mkdir -p /lumify/libcache


## Build and Deploy Lumify

### install Lumify Root

        cd ~/lumify-root
        mvn install


### build and deploy the Lumify web application and authentication plugin

        cd ~/lumify
        mvn package -P web-war -pl web/war -am
        mvn package -pl web/plugins/auth-username-only -am

        sudo cp web/war/target/lumify-web-war-0.2.0-SNAPSHOT.war /opt/jetty/webapps/lumify.war
        sudo cp web/plugins/auth-username-only/target/lumify-web-auth-username-only-0.2.0-SNAPSHOT.jar /opt/lumify/lib

        sudo /opt/jetty/bin/jetty.sh restart

1. browse to https://localhost:8443/lumify
1. login by entering any username (if using the `lumify-web-auth-username-only` plugin)


### build and deploy the Lumify Storm topology

        cd ~/lumify
        mvn package -pl storm/storm -am

**TODO: build and copy property workers**

        /opt/storm/bin/storm jar storm/storm/target/lumify-storm-0.2.0-SNAPSHOT-jar-with-dependencies.jar io.lumify.storm.StormRunner


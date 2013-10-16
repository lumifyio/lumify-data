# Lumify Storm Topology

## Vagrant Configuration
1. ```$ vagrant up```
2. ```$ vagrant ssh```
3. ```$ /opt/format.sh``` (format every time you come from develop)
4. ```$ /opt/start.sh```
5. add the following to ```/opt/lumify/config/configuration.properties``` on your local machine and Vagrant VM:


### HDFS

1. Create the data directory used to store imported files for processing:
`$ hadoop fs -mkdir /lumify/data`

2. Create the configuration directory used to store OpenNLP's configuration files:
`$ hadoop fs -mkdir /lumify/config/opennlp`

3. Copy OpenNLP configuration files to HDFS:
`$ hadoop fs -put /vagrant/conf/opennlp/* /lumify/config/opennlp`

4. Create the configuration directory used to store OpenCV's configuration files:
`$ hadoop fs -mkdir /lumify/config/opencv`

5. Copy OpenCV configuration files to HDFS:
`$ hadoop fs -put /vagrant/conf/opencv/* /lumify/config/opencv`

### Kafka Queue

1. Create and publish data to required queue topics:
`$ /opt/kafka-clear.sh`

The required topics are: text, video, and image

## Running the Topology

### Create Topology Jar
from outside of Vagrant 
1. Run `$ bin/stormLocal.sh`
2. Generate the topology jar with: `$ storm/clusterbuild.sh`

### Topology Execution 
from within Vagrant
1. On the storm cluster, execute:`$ /opt/storm-run.sh`
2. `$ hadoop fs -put /vagrant/data/import/* /lumify/data/unknown`

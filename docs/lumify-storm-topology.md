# Lumify Storm Topology

## Vagrant Configuration
1. ```$ vagrant up```
2. ```$ vagrant ssh```
3. ```$ /opt/lumify/format.sh``` (format every time you come from develop)
4. ```$ /opt/lumify/start.sh```
5. ```$ /opt/lumify/shell.sh accumulo```
        Run ```setauths -u root -s "ontology,user,workspace"```
6. make sure to update your ```/opt/lumify/config/configuration.properties``` on your local machine and Vagrant VM to contain the configurations under the [Application Configuration Wiki Page](https://github.com/nearinfinity/lumify/wiki/Application-Configuration)

### HDFS

1. Create the data directory used to store imported files for processing:
`$ hadoop fs -mkdir /lumify/data`

2. Create the configuration directory used to store OpenNLP's configuration files:
`$ hadoop fs -mkdir /lumify/config/opennlp`

3. Copy OpenNLP configuration files to HDFS:
`$ hadoop fs -put /vagrant/config/opennlp/* /lumify/config/opennlp`

4. Create the configuration directory used to store KnownEntities configuration files:
`$ hadoop fs -mkdir /lumify/config/knownEntities`

5. Copy KnownEntities configuration files to HDFS:
`$ hadoop fs -put /vagrant/config/knownEntities/* /lumify/config/knownEntities`

6. Create the configuration directory used to store OpenCV's configuration files:
`$ hadoop fs -mkdir /lumify/config/opencv`

7. Copy OpenCV configuration files to HDFS:
`$ hadoop fs -put /vagrant/config/opencv/* /lumify/config/opencv`

### Kafka Queue

1. Create and publish data to required queue topics:
`$ /opt/lumify/kafka-clear.sh`

The required topics are: term, video, artifactHighlight, processedVideo, and structuredData

## Create the Ontology
`$ bin/080_Ontology.sh`

### Local Topology Execution
1. Run `$ bin/stormLocal.sh`

### Cluster Topology Execution
1. Generate the topology jar with: `$ storm/clusterbuild.sh`  (run on local machine)
2. On the storm cluster, execute:`$ /opt/lumify/storm-run.sh`
3. `$ hadoop fs -put /vagrant/data/import/* /lumify/data/unknown`

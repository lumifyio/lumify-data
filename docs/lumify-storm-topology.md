# Lumify Storm Topology

## Vagrant Configuration
1. ```vagrant up```
2. ```/opt/format.sh``` (format every time you come from develop)
3. ```/opt/start.sh```
4. add ```nlpConfPathPrefix=/lumify/config/opennlp``` to ```/opt/lumify/config/configuration.properties```
5. ```cp /opt/lumify/config/configuration.properties .```
6. ```sudo mv /vagrant/config/configuration.properties /opt/lumify/config/```

### HDFS
1. Create the data directory used to store imported files for processing:
```hadoop fs -mkdir /lumify/data```

2. Create the configuration directory used to store OpenNLP's configuration files:
```hadoop fs -mkdir /lumify/config/opennlp```

3. Copy OpenNLP configuration files to HDFS:
```hadoop fs -put /vagrant/conf/opennlp/* /lumify/config/opennlp```

### Kafka Queue

1. Create and publish data to required queue topics:
```/opt/kafka-clear.sh```

The required topics are: text, video, and image

## Running the Topology

### Create Topology Jar ###
1. Run ```bin/storm-local.sh```
2. Generate the topology jar with: ```storm/clusterbuild.sh```

### Topology Execution ###
1. On the storm cluster, execute:```/opt/storm-run.sh```
2. hadoop fs -put /vagrant/data/import/* /lumify/data/unknown

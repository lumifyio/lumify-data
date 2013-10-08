# Lumify Storm Topology

## Vagrant Configuration

### HDFS
1. Create the data directory used to store imported files for processing:
```hadoop fs -mkdir /lumify/data```

2. Create the configuration directory used to store OpenNLP's configuration files:
```hadoop fs -mkdir /lumify/config/opennlp```

3. Copy OpenNLP configuration files to HDFS:
```hadoop fs -put /vagrant/conf/opennlp/* /lumify/config/opennlp```

### Kafka Queue

1. Create and publish data to required queue topics:
```/opt/kafka/bin/kafka-console-producer.sh --topic <topic> --zookeeper localhost:2181/kafka```

The required topics are: text, video, and image

## Running the Topology

### Create Topology Jar
1. Generate the topology jar with:
```storm/clusterbuild.sh```

### Topology Execution

1. On the storm cluster, execute:
```/opt/storm/bin/storm jar <jar> com.altamiracorp.lumify.storm.StormRunner --datadir=/lumify/data```
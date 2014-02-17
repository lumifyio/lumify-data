**Complete the [Common Setup](running-lumify.md) steps before attempting ingest.**


## Kafka Queue

Unless using the `BigTableWorkQueueRepository`, create required Kafka queues with:

    /opt/lumify/kafka-clear.sh


## Development Ontology

    bin/080_Ontology.sh


## Storm Topologies

Run the Storm topology locally (without submitting it to the nimbus server):

    bin/stormLocal.sh

It will execute in the foreground, use another console window to run the enterprise topology:

    bin/stormEnterpirseLocal.sh


## Ingest Data

    hadoop fs -mkdir /lumify/data/unknown
    hadoop fs -put <DIRECTORY_WITH_NO_SUBDIRECTORIES>/* /lumify/data/unknown

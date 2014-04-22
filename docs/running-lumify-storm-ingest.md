**Complete the [Common Setup](running-lumify.md) steps before attempting ingest.**


## Development Ontology

    bin/080_Ontology.sh


## Storm Topologies

Run the Storm topology locally (without submitting it to the nimbus server):

    bin/stormLocal.sh

It will execute in the foreground, use another console window to run the enterprise topology:

    bin/stormEnterpriseLocal.sh


## Ingest Data

    hadoop fs -mkdir /lumify/data/unknown
    hadoop fs -put <DIRECTORY_WITH_NO_SUBDIRECTORIES>/* /lumify/data/unknown

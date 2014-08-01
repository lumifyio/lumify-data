**Complete the [Common Setup](running-lumify.md) steps before attempting ingest.**


## Development Ontology

    lumify-public/bin/owlImport.sh lumify-public/examples/ontology-dev/

## Ingest Data

To ingest data from lumify-all/data/import:

    lumify-public/bin/importData.sh

## Storm Topologies

Run the Storm topology:

    lumify-public/bin/stormLocal.sh

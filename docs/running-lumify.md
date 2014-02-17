## Common Setup

### Format

    /opt/lumify/format.sh


### Accumulo Authorizations

Authorize the user that web application connects with to view data with the `ontology` security label.

    /usr/lib/accumulo/bin/accumulo shell -u root -p password -e "setauths -u root -s 'ontology'"


### Local Config Directory

Configure `.properties` and Log4j files on your local filesystem (VM and/or host).

Run a script:

    /vagrant/bin/config.sh

Or copy the following files to `/opt/lumify/confg`

- lumify-public/docs/log4j.xml
- lumify-public/docs/lumify.properties
- docs/lumify-enterprise.properties
- docs/lumify-clavin.properties

In either case modify the values for your environment.


## Web UI

    /vagrant/bin/900_Server.sh

Browse to https://localhost:8443

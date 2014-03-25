## Common Setup

### Format

    /opt/lumify/format.sh

### Local Config Directory

Configure `.properties` and Log4j files on your local filesystem (VM and/or host).

Run a script:

    sudo /vagrant/bin/config.sh

Or copy the following files to `/opt/lumify/config`

- lumify-public/docs/log4j.xml
- lumify-public/docs/lumify.properties
- docs/lumify-enterprise.properties
- docs/lumify-clavin.properties

In either case modify the values for your environment.


## Web UI

    /vagrant/bin/900_Server.sh

Browse to https://localhost:8443

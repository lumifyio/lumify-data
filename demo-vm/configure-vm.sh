#!/bin/bash

mkdir -p /opt/lumify/config
cp /vagrant/demo-vm/configuration.properties /opt/lumify/config/configuration.properties

cp /vagrant/deployment/application.xml /opt/jetty/contexts
cp /vagrant/lumify-public/web/target/application-1.0-SNAPSHOT.war /opt/jetty/webapps/application.war

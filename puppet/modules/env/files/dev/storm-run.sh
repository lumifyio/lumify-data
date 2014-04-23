#!/bin/bash

/opt/storm/bin/storm jar /vagrant/storm/target/lumify-storm-1.0-SNAPSHOT-jar-with-dependencies.jar io.lumify.storm.StormRunner --datadir=/lumify/data

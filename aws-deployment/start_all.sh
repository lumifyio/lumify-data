#!/bin/bash

hosts_file=$1

./start_hadoop.sh ${hosts_file}
./start_zookeeper.sh ${hosts_file}
./start_accumulo.sh ${hosts_file}
./start_blur.sh ${hosts_file}
./start_elasticsearch.sh ${hosts_file}

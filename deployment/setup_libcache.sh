#!/bin/bash -eu

hadoop fs -rm -r /lumify/libcache || true
hadoop fs -mkdir -p /lumify/libcache
hadoop fs -put lumify-*.jar /lumify/libcache

#!/bin/bash -eu

# TODO: check to see if HDFS is already formatted, and ofer to reformat only with confimation

sudo -u hadoop /opt/hadoop/bin/hadoop namenode -format

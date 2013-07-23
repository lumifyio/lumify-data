#!/bin/bash -e

case "$1" in
  accumulo)
    open http://192.168.33.10:50095/status
    ;;
  namenode)
    open http://192.168.33.10:50070
    ;;
  datanode)
    open http://192.168.33.10:50075
    ;;
  secondarynamenode)
    open http://192.168.33.10:50090
    ;;
  jobtracker)
    open http://192.168.33.10:50030
    ;;
  tasktracker)
    open http://192.168.33.10:50060
    ;;
  *)
    echo 'you must specify a supported web console, one of: accumulo, namenode, datanode, secondarynamenode, jobtracker, tasktracker'
    ;;
esac

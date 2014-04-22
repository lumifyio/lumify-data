#!/bin/bash -e

case "$1" in
  accumulo | a)
    open http://192.168.33.10:50095
    ;;
  namenode | nn)
    open http://192.168.33.10:50070
    ;;
  datanode | dn)
    open http://192.168.33.10:50075
    ;;
  secondarynamenode | sn)
    open http://192.168.33.10:50090
    ;;
  jobtracker | jt)
    open http://192.168.33.10:50030
    ;;
  tasktracker | tt)
    open http://192.168.33.10:50060
    ;;
  storm | s)
    open http://192.168.33.10:8081
    ;;
  rabbitmq | q)
    open http://192.168.33.10:15672
    ;;
  *)
    echo 'you must specify a supported console, one of:'
    awk '/[a-z]+\)/ {print $1}' $0 | sed -e 's/)//' -e 's/^/  /'
    ;;
esac

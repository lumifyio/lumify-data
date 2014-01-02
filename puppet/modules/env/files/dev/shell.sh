#!/bin/bash -e

case "$1" in
  accumulo)
    sudo -u accumulo /usr/lib/accumulo/bin/accumulo shell -u root
    ;;
  blur)
    sudo -u blur /usr/lib/apache-blur/bin/blur shell
    ;;
  zk)
    sudo -u zookeeper /usr/lib/zookeeper/bin/zkCli.sh
    ;;
  *)
    echo 'you must specify a supported shell, one of:'
    awk '/[a-z]+\)/ {print $1}' $0 | sed -e 's/)//' -e 's/^/  /'
    ;;
esac

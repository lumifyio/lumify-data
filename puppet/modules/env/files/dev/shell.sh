#!/bin/bash -e

tool=$1; shift
for p in "$@"; do
  params="${params} \"${p}\""
done

case "${tool}" in
  accumulo)
    sudo -u accumulo bash -c "/usr/lib/accumulo/bin/accumulo shell -u root -p password ${params}"
    ;;
  blur)
    sudo -u blur bash -c "/usr/lib/apache-blur/bin/blur shell ${params}"
    ;;
  zk)
    sudo -u zookeeper bash -c "/usr/lib/zookeeper/bin/zkCli.sh ${params}"
    ;;
  *)
    echo 'you must specify a supported shell, one of:'
    awk '/[a-z]+\)/ {print $1}' $0 | sed -e 's/)//' -e 's/^/  /'
    ;;
esac

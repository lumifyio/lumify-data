#!/bin/bash -eu

if [ "${VIRTUALIZATION_DISABLED}" = 'true' ]; then
  SOURCE="${BASH_SOURCE[0]}"
  while [ -h "$SOURCE" ]; do
    DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
  done
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

  module_dirs=$DIR/../puppet/modules:$DIR/../puppet/puppet-modules
  manifests_dir=$DIR/../puppet/manifests
else
  module_dirs=$(mount | awk '/modules.*vboxsf/ {print $1}')
  manifests_dir=$(mount | awk '/manifests.*vboxsf/ {print $1}')
fi

manifest=${manifests_dir}/dev.pp
if [ $# -gt 0 ]; then
  echo "$1" | grep '\.pp$'
  if [ $? -eq 0 ]; then
    manifest="$1"
    shift
  fi
fi

module_path=$(echo ${module_dirs} | sed -e 's/ /:/g')

echo sudo puppet apply --modulepath ${module_path} ${manifest} $*
sudo puppet apply --modulepath ${module_path} ${manifest} $*

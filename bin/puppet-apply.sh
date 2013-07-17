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

module_path=$(echo ${module_dirs} | sed -e 's/ /:/g')
manifest=red-dawn.pp

sudo puppet apply --modulepath ${module_path} ${manifests_dir}/${manifest} $*

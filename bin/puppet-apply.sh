#!/bin/bash -eu

module_dirs=$(mount | awk '/modules.*vboxsf/ {print $1}')
module_path=$(echo ${module_dirs} | sed -e 's/ /:/g')
manifests_dir=$(mount | awk '/manifests.*vboxsf/ {print $1}')
manifest=red-dawn.pp

sudo puppet apply --modulepath ${module_path} ${manifests_dir}/${manifest} $*

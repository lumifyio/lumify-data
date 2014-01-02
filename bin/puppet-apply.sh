#!/bin/bash -eu

module_dirs=$(mount | awk '/modules.*vboxsf/ {print $1}')
manifests_dir=$(mount | awk '/manifests.*vboxsf/ {print $1}')

manifest=${manifests_dir}/dev_vm.pp
if [ $# -gt 0 ]; then
  echo "$1" | grep '\.pp$'
  if [ $? -eq 0 ]; then
    manifest="$1"
    shift
  fi
fi

module_path=$(echo ${module_dirs} | sed -e 's/ /:/g')

cmd="sudo puppet apply --hiera_config /vagrant/puppet/hiera-vm.yaml --modulepath ${module_path} ${manifest} $*"
echo ${cmd}
${cmd}

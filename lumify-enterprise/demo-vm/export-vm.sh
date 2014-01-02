#!/bin/bash -eu

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

function _get_vm_property {
  local id=$1
  local key=$2

  VBoxManage showvminfo ${id} --machinereadable | awk -F '=' "/${key}=/ {print \$2}" | sed -e 's/"//g'
}

function _set_vm_property {
  local id=$1
  local key=$2
  local value=$3

  VBoxManage modifyvm ${id} --${key} ${value}
}

function _remove_vm_shared_folder {
  local id=$1
  local name=$2

  VBoxManage sharedfolder remove ${id} --name ${name}
}


id=$(cat ${DIR}/../.vagrant/machines/demo/virtualbox/id)
version=$(date +'%Y-%m-%d')
ova_filename=${DIR}/lumify-${version}.ova

vagrant halt demo

for name in $(_get_vm_property ${id} 'SharedFolderNameMachineMapping[0-9]'); do
  _remove_vm_shared_folder ${id} ${name}
done

original_vm_name=$(_get_vm_property ${id} name)
_set_vm_property ${id} name lumify-demo

rm -f ${ova_filename}
VBoxManage export ${id} \
                  -o ${ova_filename} \
                  --vsys 0 \
                  --product 'Lumify' \
                  --producturl 'http://lumify.io' \
                  --version ${version} \
                  --eulafile ${DIR}/LICENSE-2.0.html

_set_vm_property ${id} name ${original_vm_name}

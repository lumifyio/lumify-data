#!/bin/bash -e

clone_dir=$1

if [ "$2" ]; then
  branch_name=$(echo $2 | sed -e 's|origin/||')
  cd /vagrant && git checkout ${branch_name}
fi

rm -rf ${clone_dir}
git clone file:///vagrant ${clone_dir} --depth 1

cd ${clone_dir} && git log -n 1

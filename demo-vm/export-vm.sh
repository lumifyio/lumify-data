#!/bin/bash -eu

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"


id=$(cat ${DIR}/../.vagrant/machines/demo/virtualbox/id)
version=$(date +'%Y-%m-%d')
ova_filename=${DIR}/lumify-${version}.ova

(cd ${DIR}/.. && vagrant halt demo)

rm -f ${ova_filename}

VBoxManage export ${id} \
                  -o ${ova_filename} \
                  --vsys 0 \
                  --product 'Lumify' \
                  --producturl 'http://lumify.io' \
                  --version ${version} \
                  --eulafile ${DIR}/LICENSE-2.0.html

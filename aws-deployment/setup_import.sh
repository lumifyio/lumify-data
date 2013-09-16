#!/bin/bash -eu

http_proxy=$1
shift

function download_and_upload {
  local url=$1
  local hdfs_path=$2

  local filename=$(echo ${url} | sed -e 's|.*/||')
  local extensionless_filename=$(echo ${filename} | sed -e 's|\..*||')
  local tmp_dir="/data0/setup/tmp/$$.${extensionless_filename}"

  mkdir -p ${tmp_dir}

  (cd ${tmp_dir}; curl -x ${http_proxy} -s -O ${url})
  (cd ${tmp_dir}; unzip -q ${filename})

  files=$(find ${tmp_dir} -type f '!' -name '*.zip' '!' -name '* *' '!' -name '.*')

  echo ${files} | wc -w
  hadoop fs -put ${files} ${hdfs_path} || true

  rm -rf ${tmp_dir}
}

hadoop fs -mkdir /import/1-ready /import/2-processing /import/3-imported || true

for url in $*; do
  echo ${url}
  download_and_upload ${url} /import/1-ready
done

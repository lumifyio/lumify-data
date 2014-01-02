#!/bin/bash -eu

http_proxy=$1
shift

function download_and_upload {
  local url=$1
  local hdfs_path=$2

  local filename=$(echo ${url} | sed -e 's|.*/||')
  local extensionless_filename=$(echo ${filename} | sed -e 's|\..*||')
  local tmp_dir="/data0/import/tmp/$$.${extensionless_filename}"

  mkdir -p ${tmp_dir}

  (cd ${tmp_dir}; curl -x ${http_proxy} -s -O ${url})
  (cd ${tmp_dir}; unzip -q ${filename})

  files=$(find ${tmp_dir} -type f '!' -name '*.zip' '!' -name '* *' '!' -name '.*')

  echo ${files} | wc -w
  hadoop fs -put ${files} ${hdfs_path} || true

  rm -rf ${tmp_dir}
}

hadoop fs -mkdir -p /lumify/data/unknown

for url in $*; do
  echo ${url}
  download_and_upload ${url} /lumify/data/unknown
done

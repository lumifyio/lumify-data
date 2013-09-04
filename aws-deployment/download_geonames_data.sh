#!/bin/bash -eu

http_proxy=$1

function download_and_upload {
  local url=$1
  local hdfs_path=$2

  local filename=$(echo ${url} | sed -e 's|.*/||')
  local file_extension=$(echo ${filename} | sed -e 's|.*\.||')
  local extensionless_filename=$(echo ${filename} | sed -e 's|\..*||')
  local tmp_dir="/tmp/$$.${extensionless_filename}"

  mkdir ${tmp_dir}

  (cd ${tmp_dir}; curl -x ${http_proxy} -s -O ${url})

  if [ "${file_extension}" = 'zip' ]; then
    (cd ${tmp_dir}; unzip -q ${filename})
  fi

  hadoop fs -rmr ${hdfs_path} || true
  hadoop fs -mkdir ${hdfs_path}
  hadoop fs -put ${tmp_dir}/${extensionless_filename}.txt ${hdfs_path}

  rm -rf ${tmp_dir}
}

download_and_upload http://download.geonames.org/export/dump/admin1CodesASCII.txt /import/geoNames/admin1
download_and_upload http://download.geonames.org/export/dump/countryInfo.txt      /import/geoNames/countryInfo
download_and_upload http://download.geonames.org/export/dump/allCountries.zip     /import/geoNames/placeNames
download_and_upload http://download.geonames.org/export/zip/US.zip                /import/geoNames/postalCodes

#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

classpath=$(${DIR}/classpath.sh core)
if [ $? -ne 0 ]; then
  echo "${classpath}"
  exit
fi

if [ "$1" != '' ]; then
  filename=$1
  admin1codeFilename=$2
  countryInfoFilename=$3
  postalCodeFilename=$4
else
  filename=${DIR}/../data/allCountries.txt
  admin1codeFilename=${DIR}/../data/admin1CodesASCII.txt
  countryInfoFilename=${DIR}/../data/countryInfo.txt
  postalCodeFilename=${DIR}/../data/postalCodes.txt
fi

java \
-Dfile.encoding=UTF-8 \
-classpath ${classpath} \
com.altamiracorp.lumify.location.GeoNamesImporter \
--filename=${filename} \
--admin1code=${admin1codeFilename} \
--countryinfo=${countryInfoFilename} \
--postalcode=${postalCodeFilename}
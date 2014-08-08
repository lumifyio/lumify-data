#!/bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"


date=$(date '+%Y%m%dT%H%M')

(
  cd ${DIR}/..
  git pull
  git tag | grep -q last-bundle
  [ $? -eq 0 ] && ref='last-bundle..master' || ref='master'
  bundle_filename="${DIR}/../../lumify-all.${date}.bundle"
  git bundle create ${bundle_filename} ${ref}
  git tag -f last-bundle master
  git push origin :last-bundle
  git push origin last-bundle
  echo "created ${bundle_filename}"
)

(
  cd ${DIR}/../lumify-public
  git pull
  git tag | grep -q last-bundle
  [ $? -eq 0 ] && ref='last-bundle..master' || ref='master'
  bundle_filename="${DIR}/../../lumify-public.${date}.bundle"
  git bundle create ${bundle_filename} ${ref}
  git tag -f last-bundle master
  git push origin :last-bundle
  git push origin last-bundle
  echo "created ${bundle_filename}"
)

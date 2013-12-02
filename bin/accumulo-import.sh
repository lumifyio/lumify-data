#!/bin/bash

if [[ ! -f "$1" ]]; then
  echo 'Please specify local .zip file to import tables from'
  exit 1
fi

TABLES_ZIP_OR_TGZ=$(readlink -f $1)

ACCUMULO_SHELL='/usr/lib/accumulo/bin/accumulo shell -u root -p password'

tmpdir=$(mktemp -d -t $(basename $0)-XXXX)

case $(basename ${TABLES_ZIP_OR_TGZ}) in
  *.zip)
    (cd ${tmpdir} && unzip ${TABLES_ZIP_OR_TGZ})
    ;;
  *.tar.gz | *.tgz)
    (cd ${tmpdir} && tar xzf ${TABLES_ZIP_OR_TGZ})
    ;;
  *)
    echo "ERROR: unsupported archive type: ${TABLES_ZIP_OR_TGZ}"
    exit 1
    ;;
esac

for dir in $(find ${tmpdir} -name 'exportMetadata.zip' | xargs -L1 dirname | sed -e 's|.*/||'); do
  $ACCUMULO_SHELL -e "deletetable -f ${dir}"

  echo "importing ${dir}..."

  hadoop fs -mkdir /tmp/importTables/${dir}
  hadoop fs -put ${tmpdir}/${dir}/* /tmp/importTables/${dir}

  $ACCUMULO_SHELL -e "importtable ${dir} /tmp/importTables/${dir}"
done

hadoop fs -rm -R /tmp/importTables
rm -rf ${tmpdir}

function _clone {
  local name=$1
  local url=$2
  local treeish=$3

  if [ -d ${SOURCE_DIR}/${name}/.git ]; then
    (cd ${SOURCE_DIR}/${name} && git fetch)
  else
    git clone ${url} ${SOURCE_DIR}/${name}
  fi

  (cd ${SOURCE_DIR}/${name} && git checkout ${treeish})
}

function _download {
  local name=$1
  local url=$2
  local fname=$3

  if [ ! -d ${SOURCE_DIR}/${name} ]; then
    curl "${url}" -s -L --fail -o "${SOURCE_DIR}/${fname}"

    case ${fname##*.} in
    zip)
      unzip -od "${SOURCE_DIR}" "${SOURCE_DIR}/${fname}"
      ;;
    *)
      echo "ERROR: unhandled extension: ${fname##*.}"
      exit 1
      ;;
    esac
  fi
}

function _build {
  local name=$1
  local version=$2
  local release=$3

  cd ${SOURCE_DIR}
  tar czf ${RPMBUILD_DIR}/SOURCES/${name}-${version}.tar.gz ${name}/*

  cat ${DIR}/specs/${name}.spec \
    | sed -e "s/Version:.*/Version:\t${version}/" \
          -e "s/Release:.*/Release:\t${release}/" \
    > ${RPMBUILD_DIR}/SPECS/${name}.spec

  rpmlint ${RPMBUILD_DIR}/SPECS/${name}.spec

  rpmbuild -ba ${RPMBUILD_DIR}/SPECS/${name}.spec

  cp ${RPMBUILD_DIR}/SRPMS/${name}-${version}-${release}.src.rpm          ${LUMIFYREPO_DIR}/SRPMS
  cp ${RPMBUILD_DIR}/RPMS/x86_64/${name}-${version}-${release}.x86_64.rpm ${LUMIFYREPO_DIR}/RPMS/x86_64
  cp ${RPMBUILD_DIR}/SOURCES/${name}-${version}.tar.gz                    ${LUMIFYREPO_DIR}/source
}

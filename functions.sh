function _banner {
  local message="$*"

  echo -n $'\n\e[01;35m'
  echo ${message}
  echo -n $'\e[00;35m'
  printf '%*s\n' "${COLUMNS:-$(tput cols)}" | tr ' ' -
  echo -n $'\e[00;00m'

  dash=$-
  set +e
  [ "${LOG_FILE}" ] && echo "$(date +'%Y-%m-%d %H:%M:%S') ${message}" >> ${LOG_FILE}
  echo ${dash} | grep -q e
  [ $? -eq 0 ] && set -e
}

function _error {
  local message="$*"

  echo -n $'\e[01;31m'
  echo -n "ERROR: "
  echo -n $'\e[00;31m'
  echo "${message}"
  echo -n $'\e[00;00m'

  dash=$-
  set +e
  [ "${LOG_FILE}" ] && echo "$(date +'%Y-%m-%d %H:%M:%S') ERROR: ${message}" >> ${LOG_FILE}
  echo ${dash} | grep -q e
  [ $? -eq 0 ] && set -e
}

function _clone {
  local name=$1
  local url=$2
  local treeish=$3

  if [ -d ${SOURCE_DIR}/${name}/.git ]; then
    _banner "[clone] ${name} - fetching"
    (cd ${SOURCE_DIR}/${name} && git fetch)
  else
    _banner "[clone] ${name} - cloning ${url}"
    git clone ${url} ${SOURCE_DIR}/${name}
  fi

  _banner "[clone] ${name} - checking out '${treeish}'"
  (cd ${SOURCE_DIR}/${name} && git checkout ${treeish})
}

function _download {
  local name=$1
  local url=$2
  local fname=$3

  if [ ! -f ${SOURCE_DIR}/${fname} ]; then
    _banner "[download] ${name} - downloading ${url}"
    curl "${url}" -s -L --fail -o "${SOURCE_DIR}/${fname}"
  fi

  if [ ! -d ${SOURCE_DIR}/${name} ]; then
    _banner "[download] ${name} - extracting ${fname}"
    case ${fname##*.} in
    zip)
      unzip -o ${SOURCE_DIR}/${fname} -d ${SOURCE_DIR}
      ;;
    gz)
      (cd ${SOURCE_DIR} && tar xzf ${SOURCE_DIR}/${fname})
      ;;
    *)
      _error "unhandled extension: ${fname##*.}"
      exit 1
      ;;
    esac
  fi
}

function _build {
  local name=$1; shift
  local version=$1; shift
  local release=$1; shift

  _banner "[build] ${name} - creating source tar.gz"
  cd ${SOURCE_DIR}
  tar czf ${RPMBUILD_DIR}/SOURCES/${name}-${version}.tar.gz ${name}/*

  _banner "[build] ${name} - creating spec file"
  cat ${DIR}/specs/${name}.spec \
    | sed -e "s/Version:.*/Version:\t${version}/" \
          -e "s/Release:.*/Release:\t${release}/" \
          -e "s|Source:.*|Source:\t${LUMIFYREPO_URL}/source/%{name}-%{version}.tar.gz|" \
    > ${RPMBUILD_DIR}/SPECS/${name}.spec

  _banner "[build] ${name} - running rpmlint"
  rpmlint ${RPMBUILD_DIR}/SPECS/${name}.spec

  _banner "[build] ${name} - running rpmbuild"
  rpmbuild -ba ${RPMBUILD_DIR}/SPECS/${name}.spec

  _banner "[build] ${name} - copying rpm to repo"
  cp ${RPMBUILD_DIR}/RPMS/$(arch)/${name}-${version}-${release}.$(arch).rpm ${LUMIFYREPO_DIR}/RPMS/$(arch)

  _banner "[build] ${name} - copying source RPM and source tar.gz to repo"
  cp ${RPMBUILD_DIR}/SRPMS/${name}-${version}-${release}.src.rpm ${LUMIFYREPO_DIR}/SRPMS
  cp ${RPMBUILD_DIR}/SOURCES/${name}-${version}.tar.gz           ${LUMIFYREPO_DIR}/source
}

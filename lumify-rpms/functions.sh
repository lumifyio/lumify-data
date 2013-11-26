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
    #curl "${url}" -s -L --fail -o "${SOURCE_DIR}/${fname}"
    local extension="${fname##*.}"
    case $extension in
    "zip")
        unzip -od "${SOURCE_DIR}" "${SOURCE_DIR}/${fname}"
        ;;
    *)
        echo "Unhandled extension to extract $extension"
        exit 1
        ;;
    esac
  fi
}

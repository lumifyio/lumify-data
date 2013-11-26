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

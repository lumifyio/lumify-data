#!/bin/bash

# run the symlinks that point to this script
# passing a single argument of the subtree name

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

GITTREES_FILE="${DIR}/../.gittrees"


function _subtree_names {
  echo $(cat ${GITTREES_FILE} | awk '/subtree/ {printf("%s ", $2)}' | sed -e 's/[]"]//g')
}

function _match_subtree_name {
  local name=$(echo $1 | sed -e 's|/$||')
  local subtree=''

  for t in $(_subtree_names); do
    if [ "${name}" = ${t} ]; then
      subtree=${t}
      break
    fi
  done

  echo ${subtree}
}

function _invalid_subtree {
  echo 'ERROR: please specify one of the following subtrees:'
  for subtree in $(_subtree_names); do
    echo "  ${subtree}"
  done
  exit -1
}

function _section_for_subtree {
  local subtree=$1

  grep "\[subtree \"${subtree}\"\]" ${GITTREES_FILE} -A 3
}

function _subtree_config_value {
  local subtree=$1
  local key=$2

  echo "$(_section_for_subtree ${subtree})" | awk -F ' ' "/${key}/ {print \$3}"
}

function _pull_subtree {
  local subtree=$1

  local url=$(_subtree_config_value ${subtree} url)
  local path=$(_subtree_config_value ${subtree} path)
  local branch=$(_subtree_config_value ${subtree} branch)

  echo "git subtree pull --prefix=${path} ${url} ${branch}"
  (cd ${DIR}/.. && git subtree pull --prefix=${path} ${url} ${branch})
}

function _push_subtree {
  local subtree=$1

  local url=$(_subtree_config_value ${subtree} url)
  local path=$(_subtree_config_value ${subtree} path)
  local branch=$(_subtree_config_value ${subtree} branch)

  echo "git subtree push --prefix=${path} ${url} ${branch}"
  (cd ${DIR}/.. && git subtree push --prefix=${path} ${url} ${branch})
}


subtree=$(_match_subtree_name $1)
[ "${subtree}" ] || _invalid_subtree

case $(basename $0) in
  pull-subtree.sh)
    _pull_subtree ${subtree}
    ;;
  push-subtree.sh)
    _push_subtree ${subtree}
    ;;
  *)
    echo "ERROR: unexpected script name: $0"
    exit -2
    ;;
esac

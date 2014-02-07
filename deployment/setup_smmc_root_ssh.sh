#!/bin/bash -u

hosts_file=$1

if [ "$(id -un)" = 'vagrant' ]; then
  echo "you probably don't want to run $0 as vagrant!"
  exit 1
fi

[ -f ${HOME}/.ssh/id_rsa ] || ssh-keygen

if [ -e /tmp/ssh-agent.$(id -un) ]; then
  echo "using existing ssh-agent"
  export SSH_AUTH_SOCK=/tmp/ssh-agent.$(id -un)
else
  echo "starting new ssh-agent"
  eval $(ssh-agent -a /tmp/ssh-agent.$(id -un))
fi
if ssh-add -l | grep -q ${HOME}/.ssh/id_rsa; then
  echo "our key is already loaded"
else
  echo "loading our key"
  ssh-add
fi

for host in $(awk '!/puppet|osm/ {print $1}' ${hosts_file}); do
  ssh -o PasswordAuthentication=no root@${host} hostname &>/dev/null
  if [ $? -ne 0 ]; then
    echo "copying our public key to ${host}"
    which ssh-copy-id &>/dev/null
    if [ $? -eq 0 ]; then
      ssh-copy-id root@${host}
    else
      cat ${HOME}/.ssh/id_rsa.pub | ssh root@${host} 'cat >> ${HOME}/.ssh/authorized_keys'
    fi
  fi
done

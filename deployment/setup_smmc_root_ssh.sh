#!/bin/bash -u

hosts_file=$1

if [ "$(id -un)" != 'root' ]; then
  echo "$0 must be run as root!"
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
  echo "loading out key"
  ssh-add
fi

for host in $(awk '!/puppet|osm/ {print $1}' ${hosts_file}); do
  ssh -o PasswordAuthentication=no ${host} hostname &>/dev/null
  if [ $? -ne 0 ]; then
    echo "copying our public key to ${host}"
    ssh-copy-id ${host}
  fi
done

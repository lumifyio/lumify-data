#!/bin/bash -eu

latest_modules=$(ls ~/modules-*.tgz | tail -1)
latest_puppet_modules=$(ls ~/puppet-modules-*.tgz | tail -1)
latest_site=$(ls ~/site-*.pp | tail -1)
latest_hiera_cluster=$(ls ~/hiera-cluster-*.yaml | tail -1)
latest_hiera_secrets=$(ls ~/hiera-secrets-*.yaml | tail -1)

( cd /etc/puppet
  tar xzf ${latest_modules}
  unlink lumify-modules || true
  ln -s $(basename ${latest_modules} .tgz)/puppet/modules lumify-modules

  tar xzf ${latest_puppet_modules}
  unlink puppet-modules || true
  ln -s $(basename ${latest_puppet_modules} .tgz) puppet-modules
)

( cd /etc/puppet/manifests
  cp ${latest_site} .
  unlink site.pp || true
  ln -s $(basename ${latest_site}) site.pp
)

( mkdir -p /etc/puppet/hiera && cd /etc/puppet/hiera
  cp ${latest_hiera_cluster} .
  unlink cluster.yaml || true
  ln -s $(basename ${latest_hiera_cluster}) cluster.yaml

  if [ ! -L secrets.yaml ]; then
    cp ${latest_hiera_secrets} .
    ln -s $(basename ${latest_hiera_secrets}) secrets.yaml
  fi

  cd /etc/puppet
  unlink hiera.yaml || true
  ln -s $(basename ${latest_modules} .tgz)/puppet/hiera-cluster.yaml hiera.yaml
)

# (re)start the puppetmaster service
set +u
[ "$1" = 'start' ] && action=start || action=restart
set -u
service puppetmaster ${action}

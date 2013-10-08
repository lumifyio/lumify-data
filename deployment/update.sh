#!/bin/bash -eu

latest_modules=$(ls ~/modules-*.tgz | tail -1)
latest_puppet_modules=$(ls ~/puppet-modules-*.tgz | tail -1)
latest_site=$(ls ~/site-*.pp | tail -1)
latest_hiera=$(ls ~/hiera-*.yaml | tail -1)

( cd /etc/puppet
  tar xzf ${latest_modules}
  unlink lumify-modules || true
  ln -s $(basename ${latest_modules} .tgz)/puppet/modules lumify-modules

  unlink hiera || true
  ln -s $(basename ${latest_modules} .tgz)/puppet/hiera hiera

  unlink hiera.yaml || true
  ln -s $(basename ${latest_modules} .tgz)/puppet/hiera-lumify_demo.yaml hiera.yaml

  tar xzf ${latest_puppet_modules}
  unlink puppet-modules || true
  ln -s $(basename ${latest_puppet_modules} .tgz) puppet-modules
)
( cd /etc/puppet/hiera
  cp ${latest_hiera} ..
  ln -s ../$(basename ${latest_hiera}) cluster.yaml
)
( cd /etc/puppet/manifests
  unlink site.pp || true
  cp ${latest_site} .
  ln -s $(basename ${latest_site}) site.pp
)

# (re)start the puppetmaster service
set +u
[ "$1" = 'start' ] && action=start || action=restart
set -u
service puppetmaster ${action}

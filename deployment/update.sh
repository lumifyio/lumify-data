#!/bin/bash -eu

latest_modules=$(ls ~/modules-*.tgz | tail -1)
latest_puppet_modules=$(ls ~/puppet-modules-*.tgz | tail -1)

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
( cd /etc/puppet/manifests
  unlink site.pp || true
  ln -s ../$(basename ${latest_modules} .tgz)/puppet/manifests/lumify_demo.pp site.pp
)

# (re)start the puppetmaster service
set +u
[ "$1" = 'start' ] && action=start || action=restart
set -u
service puppetmaster ${action}

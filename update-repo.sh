#!/bin/sh

source ${DIR}/setenv.sh

rm -rf repo/repodata
createrepo --update -o repo/ --baseurl=${LUMIFYREPO_URL}/SRPMS repo/SRPMS
createrepo --update -o repo/ --baseurl=${LUMIFYREPO_URL}/RPMS/x86_64 repo/RPMS/x86_64

rm repo.tar.gz
tar -cvzf repo.tar.gz repo/*

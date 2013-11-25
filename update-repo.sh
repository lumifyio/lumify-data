#!/bin/sh

rm -rf repo/repodata
createrepo --update -o repo/ --baseurl=http://63.141.238.205:8081/redhat/SRPMS repo/SRPMS
createrepo --update -o repo/ --baseurl=http://63.141.238.205:8081/redhat/RPMS/x86_64 repo/RPMS/x86_64

rm repo.tar.gz
tar -cvzf repo.tar.gz repo/*

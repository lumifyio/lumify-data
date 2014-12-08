#!/bin/bash -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"

source ${DIR}/setenv.sh
source ${DIR}/functions.sh


httpd_version=2.4.9
apr_version=1.5.1
apr_util_version=1.5.3
distcache_version=1.4.5-23

_banner "[$(basename ${SOURCE})] - downloading httpd source"
set -x
curl http://archive.apache.org/dist/httpd/httpd-${httpd_version}.tar.bz2 -s -L --fail -o ${SOURCE_DIR}/httpd-${httpd_version}.tar.bz2
set +x

_banner "[$(basename ${SOURCE})] - downloading apr source"
set -x
curl http://archive.apache.org/dist/apr/apr-${apr_version}.tar.bz2 -s -L --fail -o ${SOURCE_DIR}/apr-${apr_version}.tar.bz2
set +x

_banner "[$(basename ${SOURCE})] - downloading apr-util source"
set -x
curl http://archive.apache.org/dist/apr/apr-util-${apr_util_version}.tar.bz2 -s -L --fail -o ${SOURCE_DIR}/apr-util-${apr_util_version}.tar.bz2
set +x

_banner "[$(basename ${SOURCE})] - downloading disctcache source"
set -x
curl http://dl.fedoraproject.org/pub/fedora/linux/releases/18/Fedora/source/SRPMS/d/distcache-${distcache_version}.src.rpm -s -L --fail -o ${SOURCE_DIR}/distcache-${distcache_version}.src.rpm
set +x

_banner "[$(basename ${SOURCE})] - building apr"
set -x
sudo yum -y install doxygen
rpmbuild -tb ${SOURCE_DIR}/apr-${apr_version}.tar.bz2
sudo rpm -ivh --force ${RPMBUILD_DIR}/RPMS/$(arch)/apr-${apr_version}-1.$(arch).rpm
sudo rpm -ivh --force ${RPMBUILD_DIR}/RPMS/$(arch)/apr-devel-${apr_version}-1.$(arch).rpm
mkdir -p ${LUMIFYREPO_DIR}/RPMS/$(arch)
cp ${RPMBUILD_DIR}/RPMS/$(arch)/apr-${apr_version}-1.$(arch).rpm \
   ${RPMBUILD_DIR}/RPMS/$(arch)/apr-devel-${apr_version}-1.$(arch).rpm \
   ${LUMIFYREPO_DIR}/RPMS/$(arch)
set +x

_banner "[$(basename ${SOURCE})] - building apr-util"
set -x
sudo yum -y install libuuid-devel postgresql-devel sqlite-devel mysql-devel freetds-devel unixODBC-devel nss-devel expat-devel db4-devel openldap-devel
rpmbuild -tb ${SOURCE_DIR}/apr-util-${apr_util_version}.tar.bz2
sudo rpm -ivh --force ${RPMBUILD_DIR}/RPMS/$(arch)/apr-util-${apr_util_version}-1.$(arch).rpm
sudo rpm -ivh --force ${RPMBUILD_DIR}/RPMS/$(arch)/apr-util-devel-${apr_util_version}-1.$(arch).rpm
mkdir -p ${LUMIFYREPO_DIR}/RPMS/$(arch)
cp ${RPMBUILD_DIR}/RPMS/$(arch)/apr-util-${apr_util_version}-1.$(arch).rpm \
   ${RPMBUILD_DIR}/RPMS/$(arch)/apr-util-devel-${apr_util_version}-1.$(arch).rpm \
   ${LUMIFYREPO_DIR}/RPMS/$(arch)
set +x

_banner "[$(basename ${SOURCE})] - building distcache"
set -x
rpmbuild --rebuild ${SOURCE_DIR}/distcache-${distcache_version}.src.rpm
sudo rpm -ivh --force ${RPMBUILD_DIR}/RPMS/$(arch)/distcache-${distcache_version}.$(arch).rpm
sudo rpm -ivh --force ${RPMBUILD_DIR}/RPMS/$(arch)/distcache-devel-${distcache_version}.$(arch).rpm
mkdir -p ${LUMIFYREPO_DIR}/RPMS/$(arch)
cp ${RPMBUILD_DIR}/RPMS/$(arch)/distcache-${distcache_version}.$(arch).rpm \
   ${RPMBUILD_DIR}/RPMS/$(arch)/distcache-devel-${distcache_version}.$(arch).rpm \
   ${LUMIFYREPO_DIR}/RPMS/$(arch)
set +x

_banner "[$(basename ${SOURCE})] - building httpd"
set -x
sudo yum -y install pcre-devel lua-devel libxml2-devel mailcap
rpmbuild -tb ${SOURCE_DIR}/httpd-${httpd_version}.tar.bz2
sudo rpm -ivh --force ${RPMBUILD_DIR}/RPMS/$(arch)/httpd-${httpd_version}-1.$(arch).rpm
sudo rpm -ivh --force ${RPMBUILD_DIR}/RPMS/$(arch)/mod_ssl-${httpd_version}-1.$(arch).rpm
mkdir -p ${LUMIFYREPO_DIR}/RPMS/$(arch)
cp ${RPMBUILD_DIR}/RPMS/$(arch)/httpd-${httpd_version}-1.$(arch).rpm \
   ${RPMBUILD_DIR}/RPMS/$(arch)/mod_ssl-${httpd_version}-1.$(arch).rpm \
   ${LUMIFYREPO_DIR}/RPMS/$(arch)
set +x

#include env::demo
require buildtools
require java
include macro::git
include env::dev::nodejs

# no firewall for local vms
service { 'iptables' :
  enable => false,
  ensure => 'stopped',
}

class { 'env::common::config' :
  main_properties_filename => 'lumify-qlix.properties',
}

include httpd
include httpd::mod_jk
include httpd::mod_ssl

file { '/opt/lumify/config/lumify-vm.lumify.io.cert.pem' :
  ensure => file,
  source => 'file:///vagrant/config/ssl/lumify-vm.lumify.io.cert.pem',
  owner   => 'root',
  group   => 'root',
  mode    => 'u=rw,go=r',
}

file { '/opt/lumify/config/lumify-vm.lumify.io.key.pem' :
  ensure => file,
  source => 'file:///vagrant/config/ssl/lumify-vm.lumify.io.key.pem',
  owner   => 'root',
  group   => 'root',
  mode    => 'u=rw,go=r',
}

file { '/opt/lumify/config/lumify-ca.cert.pem' :
  ensure => file,
  source => 'file:///vagrant/config/ssl/lumify-ca.cert.pem',
  owner   => 'root',
  group   => 'root',
  mode    => 'u=rw,go=r',
}

service { 'httpd' :
  ensure => running,
  enable => true,
  require => [ Package['httpd'],
               File['/opt/lumify/config/lumify-vm.lumify.io.cert.pem'],
               File['/opt/lumify/config/lumify-vm.lumify.io.key.pem'],
               File['/opt/lumify/config/lumify-ca.cert.pem']
             ],
}

class { 'java::tar' :
  version => '7u51',
  dir     => '/opt',
  creates => '/opt/jdk1.7.0_51'
}

# http://docs.puppetlabs.com/puppet/latest/reference/lang_classes.html#inheritance
$tomcat_java_home = '/opt/jdk1.7.0_51'
include tomcat::worker

$mysql_databases = {
  'lumify' => {
    ensure  => 'present',
    charset => 'utf8',
  },
}

$mysql_password = '*2AD2DCE7BF4A4A7CC54AA964F76F920772B4947C'
$mysql_users = {
  'lumify@localhost' => {
    ensure         => 'present',
    password_hash  => $mysql_password,
  },
  'lumify@%' => {
    ensure         => 'present',
    password_hash  => $mysql_password,
  },
}

$mysql_grants = {
  'lumify@localhost/lumify.*' => {
    ensure     => 'present',
    options    => ['GRANT'],
    privileges => ['ALL'],
    table      => 'lumify.*',
    user       => 'lumify@localhost',
  },
  'lumify@%/lumify.*' => {
    ensure     => 'present',
    options    => ['GRANT'],
    privileges => ['ALL'],
    table      => 'lumify.*',
    user       => 'lumify@%',
  },
}

class { 'mysql::server' :
  #remove_default_accounts => true,
  restart => true,
  override_options => { 'mysqld' => { 'bind-address' => '0.0.0.0' } },
  databases => $myql_databases,
  users => $mysql_users,
  grants => $mysql_grants,
}

package { 'openldap-servers' :
  ensure => present,
}

package { 'openldap-clients' :
  ensure => present,
}

file { '/etc/openldap/certs/lumify-vm.lumify.io.cert.pem' :
  ensure => file,
  source => 'file:///vagrant/config/ssl/lumify-vm.lumify.io.cert.pem',
  owner   => 'root',
  group   => 'root',
  mode    => 'u=rw,go=r',
  require => Package['openldap-servers'],
}

file { '/etc/openldap/certs/lumify-vm.lumify.io.key.pem' :
  ensure => file,
  source => 'file:///vagrant/config/ssl/lumify-vm.lumify.io.key.pem',
  owner   => 'root',
  group   => 'root',
  mode    => 'u=rw,go=r',
  require => Package['openldap-servers'],
}

file { '/etc/openldap/certs/lumify-ca.cert.pem' :
  ensure => file,
  source => 'file:///vagrant/config/ssl/lumify-ca.cert.pem',
  owner   => 'root',
  group   => 'root',
  mode    => 'u=rw,go=r',
  require => Package['openldap-servers'],
}

exec { 'enable-ldaps' :
  command => "/bin/sed -i -e 's/SLAPD_LDAPS=.*/SLAPD_LDAPS=yes/' /etc/sysconfig/ldap",
  unless  => "/bin/grep -q 'SLAPD_LDAPS=yes' /etc/sysconfig/ldap",
  notify  => Service['slapd'],
  require => [ Package['openldap-servers'],
               File['/etc/openldap/certs/lumify-vm.lumify.io.cert.pem'],
               File['/etc/openldap/certs/lumify-vm.lumify.io.key.pem'],
               File['/etc/openldap/certs/lumify-ca.cert.pem']
             ],
}

$ldap_config_username = 'cn=config'
$ldap_config_password = 'lumify'
$ldap_config_ldif_filename = '/etc/openldap/slapd.d/cn\=config/olcDatabase\=\{0\}config.ldif'

exec { 'set-ldap-config-password' :
  command => "/bin/echo -n 'olcRootPW: ' >> ${ldap_config_ldif_filename} && /usr/sbin/slappasswd -s '${ldap_config_password}' >> ${ldap_config_ldif_filename}",
  unless  => "/bin/grep -q 'olcRootPW' ${ldap_config_ldif_filename}",
  notify  => Service['slapd'],
  require => Package['openldap-servers'],
}

$ldap_bdb_suffix = 'dc=lumify,dc=io'
$ldap_bdb_rootdn = 'cn=root,dc=lumify,dc=io'
$ldap_bdb_password = 'lumify'
$ldap_bdb_ldif_filename = '/etc/openldap/slapd.d/cn\=config/olcDatabase\=\{2\}bdb.ldif'

exec { 'set-ldap-bdb-suffix' :
  command => "/bin/sed -i -e 's/olcSuffix:.*/olcSuffix: ${ldap_bdb_suffix}/' ${ldap_bdb_ldif_filename}",
  unless  => "/bin/grep -q 'olcSuffix: ${ldap_bdb_suffix}' ${ldap_bdb_ldif_filename}",
  notify  => Service['slapd'],
  require => Package['openldap-servers'],
}

exec { 'set-ldap-bdb-rootdn' :
  command => "/bin/sed -i -e 's/olcRootDN:.*/olcRootDN: ${ldap_bdb_rootdn}/' ${ldap_bdb_ldif_filename}",
  unless  => "/bin/grep -q 'olcRootDN: ${ldap_bdb_rootdn}' ${ldap_bdb_ldif_filename}",
  notify  => Service['slapd'],
  require => Package['openldap-servers'],
}

exec { 'set-ldap-bdb-password' :
  command => "/bin/echo -n 'olcRootPW: ' >> ${ldap_bdb_ldif_filename} && /usr/sbin/slappasswd -s '${ldap_bdb_password}' >> ${ldap_bdb_ldif_filename}",
  unless  => "/bin/grep -q 'olcRootPW' ${ldap_bdb_ldif_filename}",
  notify  => Service['slapd'],
  require => Package['openldap-servers'],
}

service { 'slapd' :
  ensure => running,
  enable => true,
  require => [ Package['openldap-servers'],
               Exec['enable-ldaps'],
               Exec['set-ldap-config-password']
             ],
}

exec { 'configure-ldaps' :
  command => "/usr/bin/ldapmodify -x -D '${ldap_config_username}' -w '${ldap_config_password}' -v -f /vagrant/config/ssl/tls.ldif",
  unless  => "/usr/bin/ldapsearch -x -D '${ldap_config_username}' -w '${ldap_config_password}' -LLL -b 'cn=config' '(objectclass=olcGlobal)' olcTLSCertificateFile | /bin/grep -q /etc/openldap/certs/lumify-vm.lumify.io.cert.pem",
  require => [ Package['openldap-clients'],
               Service['slapd'],
             ],
}

exec { 'ldap-add' :
  cwd     => '/vagrant/config/ssl',
  command => "/vagrant/config/ssl/ldap_add.sh '${ldap_bdb_rootdn}' '${ldap_bdb_password}'",
  unless  => "/usr/bin/ldapsearch -x -D '${ldap_bdb_rootdn}' -w '${ldap_bdb_password}' -LLL -b '${ldap_bdb_suffix}' '(cn=Alice)' cn | grep -q Alice",
  require => [ Package['openldap-clients'],
               Service['slapd'],
             ],
}

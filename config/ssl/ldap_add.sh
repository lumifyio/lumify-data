#!/bin/bash -u

LDAP_MANAGER_DN=$1
LDAP_MANAGER_PASSWORD=$2

ldapadd -x -D ${LDAP_MANAGER_DN} -w ${LDAP_MANAGER_PASSWORD} -v -f init.ldif

ldapadd -x -D ${LDAP_MANAGER_DN} -w ${LDAP_MANAGER_PASSWORD} -v -f users/people.ldif

for person_ldif in users/*.ldif; do
  [ "${person_ldif}" != 'users/people.ldif' ] || continue
  ldapadd -x -D ${LDAP_MANAGER_DN} -w ${LDAP_MANAGER_PASSWORD} -v -f ${person_ldif}
done

ldapadd -x -D ${LDAP_MANAGER_DN} -w ${LDAP_MANAGER_PASSWORD} -v -f groups/groups.ldif

for group_ldif in groups/*.ldif; do
  [ "${group_ldif}" != 'users/groups.ldif' ] || continue
  ldapadd -x -D ${LDAP_MANAGER_DN} -w ${LDAP_MANAGER_PASSWORD} -v -f ${group_ldif}
done

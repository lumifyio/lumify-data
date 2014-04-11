#!/bin/bash -eu

first_name=$1
last_name=$2

cn=${first_name}
cn_filename=$(echo -n ${cn} | tr 'A-Z' 'a-z' | tr -cs 'a-z' '_')

LDIF_GROUP_DN='ou=people,dc=lumify,dc=io'
OPENSSL_CONF=./openssl.cnf

openssl req -new -nodes -subj "/CN=${cn}" -newkey rsa:2048 \
            -keyout users/${cn_filename}.key.pem \
            -out users/${cn_filename}.request.pem

openssl ca -config ${OPENSSL_CONF} -policy policy_anything \
           -in users/${cn_filename}.request.pem \
           -out users/${cn_filename}.cert.pem

rm users/${cn_filename}.request.pem

openssl pkcs12 -export -name ${cn_filename} \
               -chain -CAfile lumify-ca.cert.pem -caname lumify-ca \
               -inkey users/${cn_filename}.key.pem \
               -in users/${cn_filename}.cert.pem \
               -out users/${cn_filename}.pkcs12

cert=$(sed -n '/-----BEGIN CERTIFICATE-----/,$p' users/${cn_filename}.cert.pem | awk '!/-----/ {printf "%s%s\n", (NR == 2 ? "" : " "), $1}')

cat <<EOF > users/${cn_filename}.ldif
dn: cn=${cn},${LDIF_GROUP_DN}
objectClass: top
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
cn: ${cn}
displayname: ${first_name} ${last_name}-Y-
employeenumber: $(($(cat CA/serial) - 1))
givenname: ${first_name}
sn: ${last_name}
usercertificate;binary:: ${cert}
EOF

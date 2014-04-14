#!/bin/bash

echo "please make sure you want to run this script"
exit 1

OPENSSL_CONF=./openssl.cnf

openssl req -new -newkey rsa:2048 -nodes \
            -keyout lumify-vm.lumify.io.key.pem \
            -out lumify-vm.lumify.io.request.pem

openssl ca -config ${OPENSSL_CONF} -policy policy_anything \
           -in lumify-vm.lumify.io.request.pem \
           -out lumify-vm.lumify.io.cert.pem

rm lumify-vm.lumify.io.request.pem

openssl pkcs12 -export -name lumify-vm \
               -chain -CAfile lumify-ca.cert.pem -caname lumify-ca \
               -inkey lumify-vm.lumify.io.key.pem \
               -in lumify-vm.lumify.io.cert.pem \
               -out lumify-vm.lumify.io.pkcs12

keytool -importkeystore \
        -srckeystore lumify-vm.lumify.io.pkcs12 \
        -srcstoretype PKCS12 \
        -destkeystore lumify-vm.lumify.io.jks

#!/bin/bash

echo "please make sure you want to run this script"
exit 1

OPENSSL_CONF=./openssl.cnf

openssl req -new -newkey rsa:2048 \
            -keyout lumify-ca.key.pem \
            -out lumify-ca.request.pem

openssl ca -config ${OPENSSL_CONF} -create_serial -selfsign -extensions v3_ca -days 3650 \
           -keyfile lumify-ca.key.pem \
           -in lumify-ca.request.pem \
           -out lumify-ca.cert.pem

rm lumify-ca.request.pem

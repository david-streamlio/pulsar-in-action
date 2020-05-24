#!/bin/bash

# Share the client X.509 certificates
if compgen -G "/pulsar/manning/security/authentication/tls/*.pem" > /dev/null; then
   cp /pulsar/manning/security/authentication/tls/*.pem /pulsar/manning/dropbox/ 
fi

# Share the JWT tokens with the client     
if compgen -G "/pulsar/manning/security/authentication/jwt/*-token.txt" > /dev/null; then
   cp /pulsar/manning/security/authentication/jwt/*-token.txt /pulsar/manning/dropbox/  
fi

# Share the public encryption key
if compgen -G "/pulsar/manning/security/encryption/*.pem" > /dev/null; then
   cp /pulsar/manning/security/encryption/*.pem /pulsar/manning/dropbox/
fi

# Share the public CA Truststore
if [ -f "/pulsar/manning/security/cert-authority/certs/ca.cert.pem" ]; then
   cp /pulsar/manning/security/cert-authority/certs/ca.cert.pem /pulsar/manning/dropbox/
fi
#!/bin/bash

# Share the tokens with the client     
cp /pulsar/manning/security/jwt/*-token.txt /pulsar/manning/dropbox/  

# Share the public encryption key
cp /pulsar/manning/security/encryption/*.pem /pulsar/manning/dropbox/ 

# Share the public CA Truststore
cp /pulsar/manning/security/cert-authority/certs/ca.cert.pem /pulsar/manning/dropbox/
#!/bin/bash

export PASSWORD=secret-password
mkdir -p /pulsar/manning/security/encryption

# Generate the private key
openssl ecparam -name secp521r1 \
   -genkey \
   -param_enc explicit \
   -out /pulsar/manning/security/encryption/ecdsa_privkey.pem
   
# Generate the public key
openssl ec -pubout \
           -outform pem \
           -in /pulsar/manning/security/encryption/ecdsa_privkey.pem \
           -out /pulsar/manning/security/encryption/ecdsa_pubkey.pem 

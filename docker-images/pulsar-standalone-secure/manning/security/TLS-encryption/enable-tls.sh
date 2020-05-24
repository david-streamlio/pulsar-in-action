#!/bin/bash

cd /pulsar/manning/security/cert-authority
export CA_HOME=$(pwd)
export CA_PASSWORD=secret-password

mkdir certs crl newcerts private
chmod 700 private/
touch index.txt index.txt.attr
echo 1000 > serial

# Generate the certificate authority private key
openssl genrsa -aes256 \
   -passout pass:${CA_PASSWORD} \
   -out /pulsar/manning/security/cert-authority/private/ca.key.pem \
   4096

# Restrict Acess to the certificate authority private key to prevent unauthorized use   
chmod 400 /pulsar/manning/security/cert-authority/private/ca.key.pem

# Create the root X.509 certificate.
openssl req -config openssl.cnf \
  -key /pulsar/manning/security/cert-authority/private/ca.key.pem \
  -new -x509 \
  -days 7300 \
  -sha256 \
  -extensions v3_ca \
  -out /pulsar/manning/security/cert-authority/certs/ca.cert.pem \
  -subj '/C=US/ST=CA/L=Palo Alto/O=gottaeat.com' \
  -passin pass:${CA_PASSWORD}
  
# Restrict Acess to the public X.509 certificate to prevent unauthorized use  
chmod 444 /pulsar/manning/security/cert-authority/certs/ca.cert.pem

export BROKER_PASSWORD=my-secret

# Generate the Server Certificate private key
openssl genrsa -passout pass:${BROKER_PASSWORD} \
   -out /pulsar/manning/security/cert-authority/broker.key.pem \
    2048

# Convert the key to PEM format
openssl pkcs8 -topk8 -inform PEM -outform PEM \
      -in /pulsar/manning/security/cert-authority/broker.key.pem \
      -out /pulsar/manning/security/cert-authority/broker.key-pk8.pem -nocrypt

# Generate the server certificate request       
openssl req -config /pulsar/manning/security/cert-authority/openssl.cnf \
      -key /pulsar/manning/security/cert-authority/broker.key.pem -new -sha256 \
      -out /pulsar/manning/security/cert-authority/broker.csr.pem \
      -subj '/C=US/ST=CA/L=Palo Alto/O=gottaeat.com/CN=pulsar.gottaeat.com' \
      -passin pass:${BROKER_PASSWORD}
      
# Sign the server certificate with the CA
openssl ca -config /pulsar/manning/security/cert-authority/openssl.cnf \
      -extensions server_cert \
      -days 1000 -notext -md sha256 -batch \
      -in /pulsar/manning/security/cert-authority/broker.csr.pem \
      -out /pulsar/manning/security/cert-authority/broker.cert.pem \
      -passin pass:${CA_PASSWORD}
#!/bin/bash

cd /pulsar/manning/security/cert-authority
export CA_HOME=$(pwd)
export PASSWORD=secret-password

mkdir certs crl newcerts private
chmod 700 private/
touch index.txt
echo 1000 > serial

# Generate the private key
openssl genrsa -aes256 -passout pass:${PASSWORD} \
   -out /pulsar/manning/security/cert-authority/private/ca.key.pem 4096
   
chmod 400 /pulsar/manning/security/cert-authority/private/ca.key.pem

# Create the certificate, be sure to adjust CN to match the docker hostname specified with `docker -h `
openssl req -config openssl.cnf \
  -key /pulsar/manning/security/cert-authority/private/ca.key.pem \
  -new -x509 \
  -days 7300 \
  -sha256 \
  -extensions v3_ca \
  -out /pulsar/manning/security/cert-authority/certs/ca.cert.pem \
  -subj '/C=US/ST=CA/L=Palo Alto/CN=pulsar.gottaeat.com' \
  -passin pass:${PASSWORD}
  

chmod 444 /pulsar/manning/security/cert-authority/certs/ca.cert.pem

# Generate the Cert key
openssl genrsa -passout pass:${PASSWORD} -out /pulsar/manning/security/cert-authority/broker.key.pem 2048

# Convert the key to PEM format
openssl pkcs8 -topk8 -inform PEM -outform PEM \
      -in /pulsar/manning/security/cert-authority/broker.key.pem \
      -out /pulsar/manning/security/cert-authority/broker.key-pk8.pem -nocrypt

# Generate the certificate request       
openssl req -config /pulsar/manning/security/cert-authority/openssl.cnf \
      -key /pulsar/manning/security/cert-authority/broker.key.pem -new -sha256 \
      -out /pulsar/manning/security/cert-authority/broker.csr.pem \
      -subj '/C=US/ST=CA/L=Palo Alto/O=WEB/CN=pulsar.gottaeat.com' \
      -passin pass:${PASSWORD}
      
# Sign the certificate with the CA
openssl ca -config /pulsar/manning/security/cert-authority/openssl.cnf \
      -extensions server_cert \
      -days 1000 -notext -md sha256 -batch \
      -in /pulsar/manning/security/cert-authority/broker.csr.pem \
      -out /pulsar/manning/security/cert-authority/broker.cert.pem \
      -passin pass:${PASSWORD}
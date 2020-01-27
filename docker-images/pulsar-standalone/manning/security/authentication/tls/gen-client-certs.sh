#!/bin/bash

cd /pulsar/manning/security/cert-authority
export CA_HOME=$(pwd)
export CA_PASSWORD=secret-password

function generate_client_cert() {
	
	local CLIENT_ID=$1
	local CLIENT_ROLE=$2
	local CLIENT_PASSWORD=$3

   # Generate the Client Certificate private key
   openssl genrsa -passout pass:${CLIENT_PASSWORD} \
      -out /pulsar/manning/security/authentication/tls/${CLIENT_ID}.key.pem \
       2048
    
   # Convert the key to PEM format
   openssl pkcs8 -topk8 -inform PEM -outform PEM \
      -in /pulsar/manning/security/authentication/tls/${CLIENT_ID}.key.pem \
      -out /pulsar/manning/security/authentication/tls/${CLIENT_ID}-pk8.pem -nocrypt

   # Generate the client certificate request       
   openssl req -config /pulsar/manning/security/cert-authority/openssl.cnf \
      -key /pulsar/manning/security/authentication/tls/${CLIENT_ID}.key.pem -new -sha256 \
      -out /pulsar/manning/security/authentication/tls/${CLIENT_ID}.csr.pem \
      -subj "/C=US/ST=CA/L=Palo Alto/O=gottaeat.com/CN=${CLIENT_ROLE}" \
      -passin pass:${CLIENT_PASSWORD}
      
   # Sign the server certificate with the CA
   openssl ca -config /pulsar/manning/security/cert-authority/openssl.cnf \
      -extensions usr_cert \
      -days 100 -notext -md sha256 -batch \
      -in /pulsar/manning/security/authentication/tls/${CLIENT_ID}.csr.pem \
      -out /pulsar/manning/security/authentication/tls/${CLIENT_ID}.cert.pem \
      -passin pass:${CA_PASSWORD}

   # Remove the client key and certifcate request once we are finished
   rm -f /pulsar/manning/security/authentication/tls/${CLIENT_ID}.csr.pem
   rm -f /pulsar/manning/security/authentication/tls/${CLIENT_ID}.key.pem
}

# Create a certificate for Adam with admin role-level access
generate_client_cert admin admin admin-secret

# Create a certificate for the web app with webapp role-level access
generate_client_cert webapp-service webapp webapp-secret

# Create a certificate for Peggy who with payment-role level access
generate_client_cert peggy payments payment-secret

# Create a certificate for David who needs driver-role level access
generate_client_cert david driver davids-secret

# Create a certificate for Ron who needs resturantuer level access
generate_client_cert ron restaurateur restaurateur-secret




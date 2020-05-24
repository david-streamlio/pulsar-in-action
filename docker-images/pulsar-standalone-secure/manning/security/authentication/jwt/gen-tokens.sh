#!/bin/bash

# Create a public/private keypair. The private key should be stored in a safe location 
/pulsar/bin/pulsar tokens create-key-pair \
   --output-private-key /pulsar/manning/security/authentication/jwt/my-private.key \
   --output-public-key /pulsar/manning/security/authentication/jwt/my-public.key
   
# Create the token for the admin role
/pulsar/bin/pulsar tokens create \
     --private-key file:///pulsar/manning/security/authentication/jwt/my-private.key \
     --expiry-time 1y \
     --subject admin > /pulsar/manning/security/authentication/jwt/admin-token.txt

# Create the token for the webapp role
/pulsar/bin/pulsar tokens create \
     --private-key file:///pulsar/manning/security/authentication/jwt/my-private.key \
     --expiry-time 1y \
     --subject webapp > /pulsar/manning/security/authentication/jwt/webapp-role-token.txt
     
# Create the token for the payment role
/pulsar/bin/pulsar tokens create \
     --private-key file:///pulsar/manning/security/authentication/jwt/my-private.key \
     --expiry-time 1y \
     --subject payment > /pulsar/manning/security/authentication/jwt/payment-role-token.txt
     
# Create the token for the driver role
/pulsar/bin/pulsar tokens create \
     --private-key file:///pulsar/manning/security/authentication/jwt/my-private.key \
     --expiry-time 1y \
     --subject driver > /pulsar/manning/security/authentication/jwt/driver-role-token.txt
     
# Create the token for the restaurateur role
/pulsar/bin/pulsar tokens create \
     --private-key file:///pulsar/manning/security/authentication/jwt/my-private.key \
     --expiry-time 1y \
     --subject restaurateur > /pulsar/manning/security/authentication/jwt/restaurateur-role-token.txt              


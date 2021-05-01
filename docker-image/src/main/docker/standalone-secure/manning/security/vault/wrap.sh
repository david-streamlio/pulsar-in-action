#!/bin/bash

# Wrap it
WRAP_TOKEN=`curl --header "X-Vault-Wrap-TTL: 28800" \
	-H "X-Vault-Token:myroot" --request POST \
	--data '{"policies":["encrypted-demo-app-policy"]}' \
    http://vault:8200/v1/auth/token/create | jq .wrap_info.token | tr -d '"'`
    
# Get a new token
curl -H "X-Vault-Token:myroot" --request POST \
   --data '{"policies": "default"}' \
   http://vault:8200/v1/auth/token/create

# use wrap token to unwrap the request
CLIENT_TOKEN=`curl --header 'X-Vault-Token:'"$WRAP_TOKEN"'' --request POST \
    http://vault:8200/v1/sys/wrapping/unwrap | jq .auth.client_token`
   
# Use the new client token to access the secrets that were wrapped
curl --header 'X-Vault-Token:'"$CLIENT_TOKEN"'' \
    http://vault:8200/v1/secret/data/dev
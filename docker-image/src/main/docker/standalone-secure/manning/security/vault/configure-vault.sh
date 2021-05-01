#!/bin/bash

export VAULT_ADDR="http://vault:8200"

/pulsar/manning/scripts/vaultSecretPublisher.sh ${VAULT_ADDR} \
    /secret/data/encryption/public \
    -t myroot \
    -k contents \
    -f /pulsar/manning/security/encryption/ecdsa_pubkey.pem
    
/pulsar/manning/scripts/vaultSecretPublisher.sh ${VAULT_ADDR} \
    /secret/data/encryption/private \
    -t myroot \
    -k contents \
    -f /pulsar/manning/security/encryption/ecdsa_privkey.pem
    
/pulsar/manning/scripts/vaultSecretPublisher.sh ${VAULT_ADDR} \
    /secret/data/tls/trustcerts \
    -t myroot \
    -k contents \
    -f /pulsar/manning/security/cert-authority/certs/ca.cert.pem

/pulsar/manning/scripts/vaultSecretPublisher.sh ${VAULT_ADDR} \
    /secret/data/tls/auth/admin/cert \
    -t myroot \
    -k contents \
    -f /pulsar/manning/security/authentication/tls/admin.cert.pem
    
/pulsar/manning/scripts/vaultSecretPublisher.sh ${VAULT_ADDR} \
    /secret/data/tls/auth/admin/key \
    -t myroot \
    -k contents \
    -f /pulsar/manning/security/authentication/tls/admin-pk8.pem

# Create policies for accessing the Public/private keys for message encryption
echo "Creating the public key policy"
curl -H "X-Vault-Token:myroot" --request POST \
   --data '{"policy":"path \"secret/data/*\" {capabilities = [\"read\"]} "}' \
   ${VAULT_ADDR}/v1/sys/policy/producer-policy

echo "Creating the private key policy"        
curl -H "X-Vault-Token:myroot" --request POST \
   --data '{"policy":"path \"secret/data/*\" {capabilities = [\"read\"]} "}' \
   ${VAULT_ADDR}/v1/sys/policy/consumer-policy
   
echo "Creating the encrypted-demo-app policy"
curl -H "X-Vault-Token:myroot" --request POST \
   --data '{"policy":"path \"secret/data/app/encrypted-demo/*\" {capabilities = [\"read\"]} "}' \
   ${VAULT_ADDR}/v1/sys/policy/encrypted-demo-app-policy

# Create users

echo "Enable App Role"
curl -H "X-Vault-Token:myroot" --request POST \
    --data '{ "type": "approle"}' \
   ${VAULT_ADDR}/v1/sys/auth/approle
    
echo "Create a named role for the producer"
curl -H "X-Vault-Token:myroot" --request POST \
    --data '{ "token_period": "8h", "token_ttl": "1m", "token_max_ttl":"8h", "token_policies": "producer-policy" }' \
   ${VAULT_ADDR}/v1/auth/approle/role/message-producer
    
echo "Create a named role for the consumer"
curl -H "X-Vault-Token:myroot" --request POST \
    --data '{ "token_ttl": "20m", "token_max_ttl":"8h", "policies": "consumer-policy" }' \
   ${VAULT_ADDR}/v1/auth/approle/role/message-consumer

ROLE_ID=`curl -H "X-Vault-Token:myroot" --request GET \
  ${VAULT_ADDR}/v1/auth/approle/role/message-producer/role-id | jq .data.role_id`

SECRET_ID=`curl -H "X-Vault-Token:myroot" --request POST \
  ${VAULT_ADDR}/v1/auth/approle/role/message-producer/secret-id | jq .data.secret_id`
 
echo "Store the role id and secret id"
curl -H "X-Vault-Token:myroot" --request POST \
   --data '{"data": {"trust_cert_secret":"/secret/data/tls/trustCerts", "role_id": '"${ROLE_ID}"', "secret_id": '"${SECRET_ID}"'} }' \
  ${VAULT_ADDR}/v1/secret/data/app/encrypted-demo/init
   
echo "Validate that the data was stored"
curl -H "X-Vault-Token:myroot" \
  ${VAULT_ADDR}/v1/secret/data/app/encrypted-demo/init | jq .
  
WRAP_TOKEN=`curl --header "X-Vault-Wrap-TTL: 28800" \
	-H "X-Vault-Token:myroot" --request POST \
	--data '{"policies":["encrypted-demo-app-policy"]}' \
    http://vault:8200/v1/auth/token/create | jq .wrap_info.token | tr -d '"'`
    
echo $WRAP_TOKEN
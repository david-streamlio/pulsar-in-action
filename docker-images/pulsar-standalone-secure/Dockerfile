# FROM apachepulsar/pulsar-standalone:latest
FROM apachepulsar/pulsar-all:latest as pulsar

# Restart from
FROM openjdk:8-jdk

# Copy pulsar files from pulsar-all
COPY --from=pulsar /pulsar /pulsar

RUN apt-get update && apt-get install net-tools vim jq -y

#############################################################################
# Environment Variables
#############################################################################
ENV PULSAR_HOME=/pulsar

#############################################################################
# Use the modified configuration
#############################################################################
COPY conf/standalone.conf $PULSAR_HOME/conf/standalone.conf
COPY conf/client.conf $PULSAR_HOME/conf/client.conf

#############################################################################
# Add all of our special scripts, tools, and schemas
#############################################################################
ADD manning $PULSAR_HOME/manning

RUN chmod a+x $PULSAR_HOME/manning/scripts/*.sh \
              $PULSAR_HOME/manning/security/*.sh \
              $PULSAR_HOME/manning/security/*/*.sh \
              $PULSAR_HOME/manning/security/authentication/*/*.sh

#############################################################################
# Transport Encryption using TLS
#############################################################################
RUN ["/bin/bash", "-c", "/pulsar/manning/security/TLS-encryption/enable-tls.sh"]

#############################################################################
# Generate TLS client certificates for authentication
#############################################################################
RUN ["/bin/bash", "-c", "/pulsar/manning/security/authentication/tls/gen-client-certs.sh"]

#############################################################################
# Generate JWT tokens for authentication
#############################################################################
RUN ["/bin/bash", "-c", "/pulsar/manning/security/authentication/jwt/gen-tokens.sh"]

#############################################################################
# Generate RSA public/private key pair for message encryption
#############################################################################
RUN ["/bin/bash", "-c", "/pulsar/manning/security/message-encryption/gen-rsa-keys.sh"]

##############################################################################
# Store all of the generated Certificates, JWT Tokens, and Keys in Vault
##############################################################################
# RUN ["/bin/bash", "-c", "/pulsar/manning/security/vault/configure-vault.sh"]

##############################################################################
# Start Pulsar in Standalone mode
##############################################################################
CMD ["/pulsar/bin/pulsar", "standalone"]

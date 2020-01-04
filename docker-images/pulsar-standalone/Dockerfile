FROM apachepulsar/pulsar-standalone:latest
RUN apt-get install net-tools vim -y

#####################################################################
# Environment Variables
#####################################################################
ENV PULSAR_HOME=/pulsar

#####################################################################
# Use the modified configuration
#####################################################################
COPY conf/standalone.conf $PULSAR_HOME/conf/standalone.conf
COPY conf/client.conf $PULSAR_HOME/conf/client.conf

#####################################################################
# Add all of our special scripts, tools, and schemas
#####################################################################
ADD manning $PULSAR_HOME/manning

RUN chmod a+x $PULSAR_HOME/manning/scripts/*.sh $PULSAR_HOME/manning/security/*.sh

#####################################################################
# Transport Encryption using TLS
#####################################################################
RUN ["/bin/bash", "-c", "/pulsar/manning/security/create-certificates.sh"]

#####################################################################
# Generate JWT tokens for authentication
#####################################################################
RUN ["/bin/bash", "-c", "/pulsar/manning/security/generate-jwt-tokens.sh"]

#####################################################################
# Generate RSA public/private key pair for message encryption
#####################################################################
RUN ["/bin/bash", "-c", "/pulsar/manning/security/generate-keys.sh"]
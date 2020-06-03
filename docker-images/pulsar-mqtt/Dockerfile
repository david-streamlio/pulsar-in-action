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

COPY protocols/pulsar-protocol-handler-mqtt-*.nar $PULSAR_HOME/protocols/

##############################################################################
# Start Pulsar in Standalone mode
##############################################################################
CMD ["/pulsar/bin/pulsar", "standalone"]


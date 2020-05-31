package com.manning.pulsar.chapter6.clients;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class TlsClient {
	public static void main(String[] args) throws PulsarClientException {
		final String HOME = "/Users/david/exchange";
		final String TOPIC = "persistent://public/default/test-topic";
		
		PulsarClient client = PulsarClient.builder()
			    .serviceUrl("pulsar://localhost:6651/")
			    .tlsTrustCertsFilePath(HOME + "/ca.cert.pem")
			    .build();

		Producer<byte[]> producer = 
		  client.newProducer().topic(TOPIC).create();
		
		for (int idx = 0; idx < 100; idx++) {
			producer.send("Hello TLS".getBytes());
		}		
		System.exit(0);
	}
}

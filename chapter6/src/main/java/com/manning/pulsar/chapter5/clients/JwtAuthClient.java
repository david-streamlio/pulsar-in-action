package com.manning.pulsar.chapter5.clients;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class JwtAuthClient {
	public static void main(String[] args) throws PulsarClientException {
		final String HOME = "/Users/david/exchange";
		final String TOPIC = "persistent://public/default/test-topic";
		
		PulsarClient client = PulsarClient.builder()
		   .serviceUrl("pulsar+ssl://localhost:6651/")
		   .tlsTrustCertsFilePath(HOME + "/ca.cert.pem")
		   .authentication(
			  AuthenticationFactory.token(() -> {
			     try {
					return new String(
						Files.readAllBytes(
							Paths.get(HOME+"/admin-token.txt")));
				 } catch (IOException e) {
					return "";
				 }
			 })).build();

		Producer<byte[]> producer = 
		  client.newProducer().topic(TOPIC).create();
		
		for (int idx = 0; idx < 100; idx++) {
			producer.send("Hello JWT Auth".getBytes());
		}		
		System.exit(0);
	}
}

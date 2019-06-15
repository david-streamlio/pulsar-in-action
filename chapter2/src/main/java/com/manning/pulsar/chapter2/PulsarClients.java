package com.manning.pulsar.chapter2;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class PulsarClients {
	
	private static PulsarClient client;
	private static Producer<byte[]> producer;
	private static Consumer<byte[]> consumer;

	public static void main(String[] args) throws Exception {
		client = PulsarClient.builder()
		        .serviceUrl("pulsar://localhost:6650")   
		        .build();
		
		producer = client.newProducer()
		        .topic("my-topic")
		        .create();
		
		consumer = client.newConsumer()
				.topic("my-topic")    
				.subscriptionName("my-subscription")    
				.subscribe();
		
		startConsumer();
		startProducer();
		
	}
	
	private static void startProducer() throws Exception {
		
		// Send one message
		producer.send("My message".getBytes());   
		
		int key = 0;
		
		while (true) {
			producer.newMessage()
		    .key("some-key-" + key++)    
		    .value("my-message-".getBytes())     
		    .property("message timestamp", System.currentTimeMillis() + "")    
		    .property("property_2", "value_2")
		    .send();
			
			Thread.sleep(1000);
		}
	}
	
	private static void startConsumer() throws PulsarClientException {
		
		while (true) {
			// Wait for a message
			Message<byte[]> msg = consumer.receive();    

			try {
				System.out.printf("Message received: %s", new String(msg.getData()));    
				consumer.acknowledge(msg);    
			} catch (Exception e) {
				System.err.printf("Unable to consume message: %s", e.getMessage());    
			}
		}
	}
}

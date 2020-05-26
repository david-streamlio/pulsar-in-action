package com.manning.pulsar.chapter2;

import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;

public class PulsarConsumers {
	
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
				.ackTimeout(30, TimeUnit.SECONDS)
				.subscriptionName("my-subscription") 
				.subscriptionType(SubscriptionType.Shared)
				.deadLetterPolicy(DeadLetterPolicy.builder()
						.maxRedeliverCount(10)
						.deadLetterTopic("dl-topic-name")
						.build())
				.subscribe();
		
		new Thread(() -> { startConsumer(); }).start();
		startProducer();
		
	}
	
	private static void startProducer() throws Exception {
		while (true) {
			producer.newMessage()
		    .value("my-message-".getBytes())     
		    .send();
			
			Thread.sleep(1000);
		}
	}
	
	private static void startConsumer() {
		Message<byte[]> msg = null;
		try {
			while (true) {
				// Wait for a message
				msg = consumer.receive();
				System.out.println(String.format("Message received: %s", new String(msg.getData())));
				consumer.acknowledge(msg);    
			}
		} catch (Exception e) {
			System.err.println(String.format("Unable to consume message: %s", e.getMessage()));
			consumer.negativeAcknowledge(msg);
		}
	}
}

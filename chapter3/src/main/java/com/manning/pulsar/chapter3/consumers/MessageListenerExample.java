package com.manning.pulsar.chapter3.consumers;

import java.util.stream.IntStream;

import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;

public class MessageListenerExample {

	static final String PULSAR_SERVICE_URL = "pulsar://localhost:6650";
	static final String MY_TOPIC = "persistent://public/default/my-topic";
	static final String SUBSCRIPTION = "my-shared-sub";

	public static void main() throws PulsarClientException {
	
		PulsarClient client = PulsarClient.builder()
	        .serviceUrl(PULSAR_SERVICE_URL)
	        .build();

		ConsumerBuilder<byte[]> consumerBuilder = client.newConsumer()
	        .topic(MY_TOPIC)
	        .subscriptionName(SUBSCRIPTION)
	        .subscriptionType(SubscriptionType.Shared)
	        .messageListener((consumer, msg) -> {
	    		try {
	    			System.out.println("Message received: " +  
	                         new String(msg.getData()));
	    			consumer.acknowledge(msg);
				} catch (PulsarClientException e) {
					// TODO Auto-generated catch block
				}
	    	});
	
		IntStream.range(0, 4).forEach(i -> {
			String name = String.format("mq-consumer-%d", i);
			try {
				consumerBuilder
			    	.consumerName(name)
			    	.subscribe();
			} catch (PulsarClientException e) {
				e.printStackTrace();
			}
		});

		// Sleep ?

	}
}

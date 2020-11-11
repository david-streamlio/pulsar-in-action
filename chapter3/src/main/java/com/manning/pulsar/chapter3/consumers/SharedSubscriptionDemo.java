package com.manning.pulsar.chapter3.consumers;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;

public class SharedSubscriptionDemo extends PulsarConsumerDemoBase {

	public static void main(String[] args) throws Exception {
		SharedSubscriptionDemo demo = new SharedSubscriptionDemo();
		
		demo.startConsumer();
		demo.startProducer();
		demo.startConsumer();
	}
	
	@Override
	protected Consumer<byte[]> getConsumer() {
		try {
			return getClient().newConsumer()
						.topic(topic)
						.subscriptionName(subscriptionName) 
						.subscriptionType(SubscriptionType.Shared)
						.subscribe();
		} catch (PulsarClientException e) {
			return null;
		}
	}
	
}

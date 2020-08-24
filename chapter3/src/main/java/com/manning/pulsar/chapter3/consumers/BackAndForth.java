package com.manning.pulsar.chapter3.consumers;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.SubscriptionType;

public class BackAndForth extends PulsarConsumerDemoBase {

	public static void main(String[] args) throws Exception {
		BackAndForth sl = new BackAndForth();
		
		sl.startConsumer();
		sl.startProducer();
	}
	
	protected Consumer<byte[]> getConsumer() throws PulsarClientException {
		if (consumer == null) {
			consumer = getClient().newConsumer()
					.topic(topic)
					.subscriptionName(subscriptionName) 
					.subscriptionType(SubscriptionType.Shared)
					.subscribe();
		}
		return consumer;
	}

}

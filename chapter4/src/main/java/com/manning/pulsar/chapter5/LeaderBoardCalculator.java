package com.manning.pulsar.chapter5;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;

public class LeaderBoardCalculator {

	public static void main(String[] args) throws PulsarClientException {
		PulsarClient client = PulsarClient.builder()
				.serviceUrl("pulsar://127.0.0.1:6650/")
				.build();

		Consumer<String> consumer = client.newConsumer(Schema.STRING)
				.topic("persistent://manning/chapter02/GameScores")
				.subscriptionType(SubscriptionType.Exclusive)    
				.subscribe();

		Message<String> msg = null;
		while ((msg = consumer.receive()) != null) {
			calculate(msg);    
			consumer.acknowledge(msg);    
		}

	}

	private static void calculate(Message<String> msg) {
		// Parse the JSON and perform the calculation.
	}
}

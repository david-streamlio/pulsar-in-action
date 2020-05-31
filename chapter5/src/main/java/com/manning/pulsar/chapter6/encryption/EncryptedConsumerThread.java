package com.manning.pulsar.chapter6.encryption;

import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.ConsumerCryptoFailureAction;
import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;

public class EncryptedConsumerThread extends Thread {

	final CryptoKeyReader cryptoReader;
	final PulsarClient client;
	final String topicName;
	boolean stopped = false;
	
	public EncryptedConsumerThread(CryptoKeyReader crypto, PulsarClient client, String topic) {
		this.cryptoReader = crypto;
		this.client = client;
		this.topicName = topic;
	}
	
	public void run() {
		
		try {
			
			ConsumerBuilder<String> builder = client
					.newConsumer(Schema.STRING)
					.consumerName("my-consumer")
					.topic(topicName)
					.subscriptionName("my-sub")
					.subscriptionType(SubscriptionType.Exclusive);
			
			if (cryptoReader != null) {
				builder = builder.cryptoKeyReader(cryptoReader).
						          cryptoFailureAction(ConsumerCryptoFailureAction.FAIL);
			}
			
			Consumer<String> consumer = builder.subscribe();
			
			while (!stopped && !consumer.hasReachedEndOfTopic()) {
					Message<String> msg = consumer.receive(5, TimeUnit.SECONDS);
					if (msg != null) {
						System.out.println(String.format("Received message  msgId: %s -- content: '%s'\n",
								msg.getMessageId(), msg.getValue()));

						consumer.acknowledge(msg);
					} else {
						System.err.println("unable to consume messages");
					}
			}
			
			consumer.close();
			
		} catch (PulsarClientException e) {
			// TODO Auto-generated catch block
			
		} 
	}
	
	public void halt() {
		stopped = true;
	}
}

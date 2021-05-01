package com.manning.pulsar.chapter3.consumers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;

public class AsyncConsumerDemo {
	
	private static final LinkedBlockingQueue<CompletableFuture<Message<String>>> outstandingMessages = 
	  new LinkedBlockingQueue<CompletableFuture<Message<String>>>(1000);

	public static void main(String[] args) throws PulsarClientException, InterruptedException, ExecutionException {
		
		PulsarClient pulsarClient = PulsarClient.builder()
                .serviceUrl("pulsar://broker:6650")
                .build();

		Consumer<String> consumer = pulsarClient.newConsumer(Schema.STRING)
		            .subscriptionName("pulsarSubscriptionName")
		            .topic("pulsarTopicName")
		            .ackTimeout(240, TimeUnit.SECONDS)
		            .subscriptionType(SubscriptionType.Exclusive)
		            .subscribe();
		
		new Thread(() -> { // Lambda Expression
			CompletableFuture<Message<String>> future;
			
			while ( (future = consumer.receiveAsync()) != null) {
				outstandingMessages.add(future);
			}
	      }).start();
		
		new Thread(() -> { // Lambda Expression
			while(true) {
				try {
					CompletableFuture<Message<String>> future = outstandingMessages.take();
					Message<String> msg = future.get();
					
					// Process the message
					
					consumer.acknowledgeAsync(msg.getMessageId());
					
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
	      }).start();
	}
	
}

package com.manning.pulsar.chapter3.consumers;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;

import com.manning.pulsar.chapter3.PulsarDemoBase;

public abstract class PulsarConsumerDemoBase extends PulsarDemoBase {

	protected void startConsumer() {
		final Consumer<byte[]> consumer = getConsumer();
		startConsumer(consumer);
	}
	protected void startConsumer(final Consumer<byte[]> consumer) {
		
		Runnable run = () -> {	
			while (true) {
				Message<byte[]> msg = null;  
				// Wait for a message
				try {
					msg = getConsumer().receive();    
					System.out.printf("Message received: %s \n", new String(msg.getData()));    
					getConsumer().acknowledge(msg);    
					Thread.sleep(100);
				} catch (Exception e) {
					System.err.printf("Unable to consume message: %s \n", e.getMessage()); 
					consumer.negativeAcknowledge(msg);
				}
			}
		};
		new Thread(run).start();
	}
	
	protected abstract Consumer<byte[]> getConsumer();

}

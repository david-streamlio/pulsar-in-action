package com.manning.pulsar.chapter3.consumers;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;

import com.manning.pulsar.chapter3.PulsarDemoBase;

public abstract class PulsarConsumerDemoBase extends PulsarDemoBase {

	protected void startConsumer() {
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
	
	@Override
	protected Reader<byte[]> getReader() throws PulsarClientException {
		// TODO Auto-generated method stub
		return null;
	}

}

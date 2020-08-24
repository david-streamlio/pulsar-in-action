package com.manning.pulsar.chapter3.readers;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;

import com.manning.pulsar.chapter3.PulsarDemoBase;

public abstract class PulsarReaderDemoBase extends PulsarDemoBase {

	protected void startReader() throws PulsarClientException {
		Runnable run = () -> {
		  try {
		    do {
			  Message<byte[]> msg = getReader().readNext();
			  System.out.printf("Message read: %s \n", new String(msg.getData()));
		    } while (!getReader().hasReachedEndOfTopic());
		  } catch (Exception ex) { }
		};
		new Thread(run).start();
	}
	
	@Override
	protected Consumer<byte[]> getConsumer() throws PulsarClientException {
		// None
		return null;
	}

}

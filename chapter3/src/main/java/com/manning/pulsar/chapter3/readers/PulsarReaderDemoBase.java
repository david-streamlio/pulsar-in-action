package com.manning.pulsar.chapter3.readers;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;

import com.manning.pulsar.chapter3.PulsarDemoBase;

public abstract class PulsarReaderDemoBase extends PulsarDemoBase {

	protected void startReader() throws PulsarClientException {
		final Reader<byte[]> reader = getReader();
		
		Runnable run = () -> {
		  try {
		    do {
			  Message<byte[]> msg = reader.readNext();
			  System.out.printf("Message read: %s \n", new String(msg.getData()));
		    } while (!reader.hasReachedEndOfTopic());
		  } catch (Exception ex) { }
		};
		new Thread(run).start();
	}
	
	protected abstract Reader<byte[]> getReader();

}

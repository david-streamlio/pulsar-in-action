package com.manning.pulsar.chapter2;

import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;

public class PulsarReaders {
	
	private static PulsarClient client;
	private static Producer<byte[]> producer;
	private static Reader<byte[]> reader;

	public static void main(String[] args) throws Exception {
		
		client = PulsarClient.builder()
		        .serviceUrl("pulsar://localhost:6650")   
		        .build();
		
		producer = client.newProducer()
		        .topic("my-other-topic")
		        .create();
		
		reader = client.newReader()
				 .topic("my-other-topic")
				 .readerName("my-reader")
				 .startMessageId(MessageId.earliest)
				 .create();

		new Thread(() -> {startProducer();}).start();
		startReader();
		
	}
	
	private static void startProducer() {
		try {
			while (true) {
				producer.newMessage()
			    .value("my-message-".getBytes())
			    .send();
				Thread.sleep(1000);
			}
		} catch (Exception ex) {
			System.out.println("Exception during produce: " + ex.getMessage());
		}
	}
	
	private static void startReader() throws PulsarClientException {
		MessageId lastRead = null;
		do {
			Message<byte[]> msg = reader.readNext();
			System.out.println(String.format("Message read: %s", new String(msg.getData())));
			lastRead = msg.getMessageId();
		} while (!reader.hasReachedEndOfTopic());
	}

}

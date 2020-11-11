package com.manning.pulsar.chapter3.readers;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;

public class EarliestReader extends PulsarReaderDemoBase {

	public static void main(String[] args) throws Exception {
		EarliestReader er = new EarliestReader();
		er.startProducer();
		er.startReader();
	}
	
	@Override
	protected Reader<byte[]> getReader() {
		try {
			return getClient().newReader()
						.topic(topic)
						.readerName(readerName)
						.startMessageId(MessageId.earliest)
						.create();
		} catch (PulsarClientException e) {
			return null;
		}

	}
}

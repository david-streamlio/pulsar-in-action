package com.manning.pulsar.chapter3.readers;

import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;

public class LatestReader extends PulsarReaderDemoBase {

	public static void main(String[] args) throws Exception {
		LatestReader lr = new LatestReader();
		lr.startProducer();
		lr.startReader();
	}
	
	@Override
	protected Reader<byte[]> getReader() throws PulsarClientException {
		if (reader == null) {
			reader = getClient().newReader()
					.topic(topic)
					.readerName(readerName)
					.startMessageId(MessageId.latest)
					.create();
		}
		return reader;
	}

}

package com.manning.pulsar.chapter3;

import java.util.Date;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;

public abstract class PulsarDemoBase {

	private String serviceUrl;
	protected String topic;
	protected String subscriptionName;
	protected String readerName;

	private PulsarClient client;
	private Producer<byte[]> producer;
	protected Consumer<byte[]> consumer;
	protected Reader<byte[]> reader;
	
	public PulsarDemoBase() {
		this.serviceUrl = "pulsar://localhost:6650";
		this.topic = "persistent://public/default/my-topic";
		this.subscriptionName = "my-sub";
		this.readerName = "my-reader";
	}
	
	public PulsarDemoBase(String serviceUrl, String topic, String subName, String readerName) {
		this.serviceUrl = serviceUrl;
		this.topic = topic;
		this.subscriptionName = subName;
		this.readerName = readerName;
	}
	
	protected void startProducer() {
		Runnable run = () -> {
			int counter = 0;
			while (true) {
				try {
					getProducer().newMessage()
					.value(String.format("{id: %d, time: %tc}", ++counter, new Date()).getBytes())     
					.send();

					Thread.sleep(1000);
				} catch (final Exception ex) { }
			}
		};
		new Thread(run).start();
	}

	protected PulsarClient getClient() throws PulsarClientException {
		if (client == null) {
		  client = PulsarClient.builder()
			        .serviceUrl(serviceUrl)   
			        .build();
		}
		return client;
	}
	
	protected Producer<byte[]> getProducer() throws PulsarClientException {
		if (producer == null) {
			producer = getClient().newProducer()
			        .topic(topic)
			        .create();
		}
		return producer;
	}

	abstract protected Consumer<byte[]> getConsumer() throws PulsarClientException;
	
	abstract protected Reader<byte[]> getReader() throws PulsarClientException;

}

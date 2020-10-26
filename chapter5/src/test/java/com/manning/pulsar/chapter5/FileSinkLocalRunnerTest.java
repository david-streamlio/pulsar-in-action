package com.manning.pulsar.chapter5;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.io.SinkConfig;
import org.apache.pulsar.functions.LocalRunner;

public class FileSinkLocalRunnerTest {

	final static String BROKER_URL = "pulsar://localhost:6650";
	final static String IN = "persistent://public/default/raw-feed"; 
	
	private static LocalRunner localRunner;
	private static PulsarClient client;
	private static Producer<String> producer;
	private static boolean stopped = false;
	
	public static void main(String[] args) throws Exception {
		startLocalRunner();
		init();
		sendData();
		shutdown();
	}
	
	private static void startLocalRunner() throws Exception {
		localRunner = LocalRunner.builder()
				.brokerServiceUrl(BROKER_URL)
				.sinkConfig(getSinkConfig())
				.build();
		localRunner.start(false);
	}

	private static void init() throws PulsarClientException {
		client = PulsarClient.builder()
			    .serviceUrl(BROKER_URL)
			    .build();

		producer = client.newProducer(Schema.STRING).topic(IN).create();	
	}
	
	private static void sendData() throws IOException {
		while (!stopped) {
			producer.send(RandomStringUtils.randomAlphabetic(10));
		}
	}
	
	private static void shutdown() throws Exception {
		Thread.sleep(30000);
		stopped = true;
		localRunner.stop();
	    producer.close();
	}
	
	private static SinkConfig getSinkConfig() {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
		inputSpecs.put(IN, ConsumerConfig.builder()
				.schemaType(Schema.STRING.getSchemaInfo().getName())
				.build());

		Map<String, Object> configs = new HashMap<String, Object>();
		configs.put(LocalFileSink.FILENAME_PREFIX_CONFIG_KEY, "");
		configs.put(LocalFileSink.FILENAME_SUFFIX_CONFIG_KEY, "");
		
		return SinkConfig.builder()
				.className(LocalFileSink.class.getName())
				.configs(configs)
				.inputs(Collections.singleton(IN))
				.inputSpecs(inputSpecs)
				.name("file-sink")
				.tenant("public")
				.namespace("default")
				.build();
	}
}

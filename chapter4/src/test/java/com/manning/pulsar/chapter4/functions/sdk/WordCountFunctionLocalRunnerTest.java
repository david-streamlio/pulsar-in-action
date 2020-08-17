package com.manning.pulsar.chapter4.functions.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;

public class WordCountFunctionLocalRunnerTest {
	
	final static String BROKER_URL = "pulsar://localhost:6650";
	final static String STATE_STORE_URL = "bk://localhost:4181";
	final static String IN = "persistent://public/default/word-test"; 
	final static String OUTPUT = "persistent://public/default/word-test-out";
	
	private static LocalRunner localRunner;
	private static PulsarClient client;
	private static Producer<String> producer;

	private static void init() throws PulsarClientException {
		client = PulsarClient.builder()
			    .serviceUrl(BROKER_URL)
			    .build();

		producer = client.newProducer(Schema.STRING).topic(IN).create();	
	}
	
	private static void sendData() throws IOException {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data.txt");
		InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		for (String line; (line = reader.readLine()) != null;) {
			producer.send(line);
		}
	}
	
	private static FunctionConfig getFunctionConfig() {
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
		inputSpecs.put(IN, 
				ConsumerConfig.builder()
				.schemaType(Schema.STRING.getSchemaInfo().getName())
				.build());

		return FunctionConfig.builder()
				.className(WordCountFunction.class.getName())
				.inputs(Collections.singleton(IN))
				.inputSpecs(inputSpecs)
				.output(OUTPUT)
				.name("word-counter")
				.tenant("public")
				.namespace("default")
				.runtime(FunctionConfig.Runtime.JAVA)
				.subName("word-counter-sub")
				.parallelism(1)
		        .processingGuarantees(FunctionConfig.ProcessingGuarantees.EFFECTIVELY_ONCE)
		        .autoAck(true)
		        .cleanupSubscription(true)
				.build();
	}
	
	private static void startLocalRunner() throws Exception {
		localRunner = LocalRunner.builder()
				.brokerServiceUrl(BROKER_URL)
				.functionConfig(getFunctionConfig())
				.build();
		
		localRunner.start(false);
	}
	
	public static void main(String[] args) throws Exception {
		startLocalRunner();
		init();
	    sendData();
	}
}

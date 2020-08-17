package com.manning.pulsar.chapter4.functions.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;

public class KeywordFilterFunctionLocalRunnerTest {
	final static String BROKER_URL = "pulsar://localhost:6650";
	final static String IN = "persistent://public/default/raw-feed"; 
	final static String OUT = "persistent://public/default/filtered-feed";
	
	private static ExecutorService executor;
	private static LocalRunner localRunner;
	private static PulsarClient client;
	private static Producer<String> producer;
	private static Consumer<String> consumer;
	private static String keyword = "";
	
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			keyword = args[0];
		}
		startLocalRunner();
		init();
		startConsumer();
	    sendData();
	    shutdown();
	}
	
	private static void startLocalRunner() throws Exception {
		localRunner = LocalRunner.builder()
				.brokerServiceUrl(BROKER_URL)
				.functionConfig(getFunctionConfig())
				.build();
		localRunner.start(false);
	}

	private static void init() throws PulsarClientException {
		executor = Executors.newFixedThreadPool(2);
		client = PulsarClient.builder()
			    .serviceUrl(BROKER_URL)
			    .build();

		producer = client.newProducer(Schema.STRING).topic(IN).create();	
		consumer = client.newConsumer(Schema.STRING).topic(OUT).subscriptionName("validation-sub").subscribe();
	}
	
	private static FunctionConfig getFunctionConfig() {
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
		inputSpecs.put(IN, ConsumerConfig.builder()
				.schemaType(Schema.STRING.getSchemaInfo().getName())
				.build());

		Map<String, Object> userConfig = new HashMap<String, Object>();
		userConfig.put(KeywordFilterFunction.KEYWORD_CONFIG, keyword);
		userConfig.put(KeywordFilterFunction.IGNORE_CONFIG, true);
		
		return FunctionConfig.builder()
				.className(KeywordFilterFunction.class.getName())
				.inputs(Collections.singleton(IN))
				.inputSpecs(inputSpecs)
				.output(OUT)
				.name("keyword-filter")
				.tenant("public")
				.namespace("default")
				.runtime(FunctionConfig.Runtime.JAVA)
				.subName("keyword-filter-sub")
		        .userConfig(userConfig)
				.build();
	}
	
	private static void startConsumer() {
		Runnable runnableTask = () -> {
			while (true) {
			  Message<String> msg = null;
			  try {
			    msg = consumer.receive();
			    System.out.printf("Message received: %s \n", msg.getValue());
			    consumer.acknowledge(msg);
			  } catch (Exception e) {
			    consumer.negativeAcknowledge(msg);
			  }
			}
		};
		executor.execute(runnableTask);
	}
	
	private static void sendData() throws IOException {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-data.txt");
		InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		for (String line; (line = reader.readLine()) != null;) {
			producer.send(line);
		}
	}
	
	private static void shutdown() throws Exception {
		Thread.sleep(30000);
	    executor.shutdown();
	    localRunner.stop();
	    producer.close();
	    consumer.close();
	    System.exit(0);
	}
	
}

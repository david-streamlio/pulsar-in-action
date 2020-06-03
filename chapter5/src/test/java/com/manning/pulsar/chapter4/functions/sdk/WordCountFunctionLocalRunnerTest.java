package com.manning.pulsar.chapter4.functions.sdk;

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
	final static String TOPIC = "persistent://public/default/word-test"; 
	
	private static PulsarClient client;
	private static Producer<String> producer;

	private static void init() throws PulsarClientException {
		client = PulsarClient.builder()
			    .serviceUrl(BROKER_URL)
			    .build();

		 producer = 
		  client.newProducer(Schema.STRING).topic(TOPIC).create();
		 
	}
	
	private static void sendData() throws PulsarClientException {
		producer.send("Here is a message");
	}
	
	public static void main(String[] args) throws Exception {
		
		init();
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
	    inputSpecs.put(TOPIC, 
	    		ConsumerConfig.builder().schemaType(Schema.STRING.getSchemaInfo().getName()).build());
		
	    FunctionConfig functionConfig = 
	    	FunctionConfig.builder()
	    	.className(WordCountFunction.class.getName())
	    	.inputs(Collections.singleton(TOPIC))
	    	.inputSpecs(inputSpecs)
	    	.maxPendingAsyncRequests(Integer.valueOf(500))
	    	.name("word-counter")
	    	.runtime(FunctionConfig.Runtime.JAVA)
	    	.build();

	    LocalRunner localRunner = 
	    	LocalRunner.builder()
	    		.brokerServiceUrl(BROKER_URL)
	    		.stateStorageServiceUrl("bk://localhost:4181")
	    		.functionConfig(functionConfig)
	    		.build();
	    
	    localRunner.start(true);
	    sendData();
	    Thread.sleep(30 * 1000);
	    localRunner.stop();
	    System.exit(0);
	}
}

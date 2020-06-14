package com.manning.pulsar.iiot.analytics.quantiles;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.functions.ConsumerConfig;
import org.apache.pulsar.common.functions.FunctionConfig;
import org.apache.pulsar.functions.LocalRunner;

import com.manning.pulsar.iiot.engine.EngineSensor;

public class EngineSensorQuantilesFunctionTest {
	
	private static final String OUTPUT_TOPIC = "persistent://public/default/engine-sensor";
	private static final String SINK_TOPIC = "persistent://public/default/quants";
	private static Random rnd = new Random();

	public static void main(String[] args) throws Exception {
		
		Map<String, ConsumerConfig> inputSpecs = new HashMap<String, ConsumerConfig> ();
	    inputSpecs.put(OUTPUT_TOPIC, ConsumerConfig.builder().schemaType("avro").build());
		
	    Map<String, Object> userConfig = new HashMap<String, Object> ();
	    userConfig.put("quantDbSinkTopic", SINK_TOPIC);
	    
		FunctionConfig funcConfig = FunctionConfig.builder()
	    		.className(ClutchSensorQuantilesFunction.class.getName())
	    		.inputs(Collections.singleton(OUTPUT_TOPIC))
	    		.inputSpecs(inputSpecs)
	    		.name("clutch-sensor-func")
	    		.runtime(FunctionConfig.Runtime.JAVA)
	    		.userConfig(userConfig)
	    		.build();
	    
		LocalRunner localRunner = 
		    	LocalRunner.builder()
		    		.brokerServiceUrl("pulsar://localhost:6650")
		    		.functionConfig(funcConfig)
		    		.build();
		    
		    localRunner.start(false);
		    generateEvents();
		    Thread.sleep(30 * 1000);
		    localRunner.stop();
		    System.exit(0);
	}
	
	private static void generateEvents() throws PulsarClientException {
		PulsarClient client = PulsarClient.builder().serviceUrl("pulsar://localhost:6650").build(); 

		try (Producer<EngineSensor> producer = client.newProducer(Schema.AVRO(EngineSensor.class))
				.topic(OUTPUT_TOPIC).create()) {
			while (true) {
				producer.send(createRandomEngineSensor());
			}
		} catch (PulsarClientException e) {
			e.printStackTrace();
		} finally {
			client.close();
		}
	}
	
	private static EngineSensor createRandomEngineSensor() {
		return EngineSensor.newBuilder()
				.setAirTemp(rnd.nextDouble())
				.setCoolingAirTemp(rnd.nextDouble())
				.setIronTemp(rnd.nextDouble())
				.setMotorPowerSupplyCurrent(rnd.nextDouble())
				.setMotorSatorWindingTemp(rnd.nextDouble())
				.setSensorId(503)
				.build();
	}
}

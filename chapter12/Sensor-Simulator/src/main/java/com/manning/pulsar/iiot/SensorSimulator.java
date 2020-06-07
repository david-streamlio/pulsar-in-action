package com.manning.pulsar.iiot;

import java.net.URISyntaxException;

import org.apache.avro.message.BinaryMessageEncoder;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;

public abstract class SensorSimulator<T extends org.apache.avro.specific.SpecificRecordBase> {
	
	protected String mqttHost;
	protected int mqttPort;
	protected String topicName;
	protected MQTT mqtt;
	protected BlockingConnection connection;
	
	public SensorSimulator(String host, int port, String topic) {
		this.mqttHost = host;
		this.mqttPort = port;
		this.topicName = topic;
	}
	
	protected MQTT getMqtt() {
		if (mqtt == null) {
			mqtt = new MQTT();
			try {
				mqtt.setHost(mqttHost, mqttPort);
			} catch (URISyntaxException e) {
				mqtt = null;
				e.printStackTrace();
			}
		}
		return mqtt;
	}
	
	protected BlockingConnection getConnection() {
		if (connection == null) {
			connection = getMqtt().blockingConnection();
		}
		return connection;
	}
	
	public void emit() throws Exception {
		publish(generateEvent());
	}
	
	protected void publish(T event) throws Exception {
//		getConnection().publish(topicName, getEncoder().encode(event).array(), QoS.AT_LEAST_ONCE, false);
		getConnection().publish(topicName,  "Hello MOP!".getBytes(), QoS.AT_LEAST_ONCE, false);
	}
	
	protected abstract T generateEvent();
	
	protected abstract BinaryMessageEncoder<T> getEncoder();

}

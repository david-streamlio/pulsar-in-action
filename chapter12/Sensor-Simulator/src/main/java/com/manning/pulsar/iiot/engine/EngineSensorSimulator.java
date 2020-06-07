package com.manning.pulsar.iiot.engine;

import org.apache.avro.message.BinaryMessageEncoder;

import com.manning.pulsar.iiot.SensorSimulator;

public class EngineSensorSimulator extends SensorSimulator<EngineSensor> {

	public EngineSensorSimulator(String host, int port, String topic) {
		super(host, port, topic);
	}

	@Override
	protected EngineSensor generateEvent() {
		return EngineSensor.newBuilder()
				.setAirTemp(47.9f)
				.setCoolingAirTemp(34.8f)
				.setIronTemp(45.2f)
				.setMotorPowerSupplyCurrent(217)
				.setMotorSatorWindingTemp(45.0f)
				.setSensorId(3)
				.build();
	}

	@Override
	protected BinaryMessageEncoder<EngineSensor> getEncoder() {
		return EngineSensor.getEncoder();
	}

}

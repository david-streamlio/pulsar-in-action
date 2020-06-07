package com.manning.pulsar.iiot.oilcooler;

import org.apache.avro.message.BinaryMessageEncoder;

import com.manning.pulsar.iiot.SensorSimulator;

public class OilCoolerSensorSimulator extends SensorSimulator<OilCoolerSensor> {

	public OilCoolerSensorSimulator(String host, int port, String topic) {
		super(host, port, topic);
	}

	@Override
	protected OilCoolerSensor generateEvent() {
		return OilCoolerSensor.newBuilder()
				.setLubricatingOilPressure(0.56f)
				.setLubricatingOilTemp(81.1f)
				.setOilTempBack(84.3f)
				.setOilTempFront(111)
				.setSensorId(4)
				.build();
	}

	@Override
	protected BinaryMessageEncoder<OilCoolerSensor> getEncoder() {
		return OilCoolerSensor.getEncoder();
	}

}

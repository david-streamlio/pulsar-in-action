package com.manning.pulsar.iiot.analytics.quantiles;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.datasketches.quantiles.DoublesSketch;
import org.apache.datasketches.quantiles.UpdateDoublesSketch;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.manning.pulsar.iiot.db.SensorQuantile;

public abstract class AbstractSensorQuantilesFunction<T> implements Function<T, Void> {
	
	protected static final String FIELD_NAMES_PROPERTY_KEY = "field_names";
	protected static final String PUBLISH_INTERVAL_KEY = "publish_interval";
	protected static final String DB_SINK_PROPERTY_KEY = "quantDbSinkTopic";
	
	protected boolean initialized = false;
	protected long publishInterval;
	protected long lastPublishTime = System.currentTimeMillis();
	protected String quantDbSinkTopic;
	protected List<String> sensorFieldNames;
	protected Map<Integer,Map<String, UpdateDoublesSketch>> sketchMap = new HashMap<Integer, Map<String, UpdateDoublesSketch>> ();


	@Override
	abstract public Void process(T input, Context context) throws Exception;
	
	protected void init(Context ctx) {
		publishInterval = (long) ctx.getUserConfigValueOrDefault(PUBLISH_INTERVAL_KEY, Long.valueOf(30000l));
		quantDbSinkTopic = (String) ctx.getUserConfigValue(DB_SINK_PROPERTY_KEY).get();
		sensorFieldNames = new ArrayList<String> ();
	}
	
	protected boolean shouldPublish() {
		return (sketchMap != null && !sketchMap.isEmpty()) && 
			   (System.currentTimeMillis() - lastPublishTime) > publishInterval;
	}
	
	protected void publish(Context ctx) {
		
		long now = System.currentTimeMillis();
		
		sketchMap.keySet().parallelStream().forEach(sensorId -> {
			sensorFieldNames.parallelStream().forEach(field -> {
				try {
					SensorQuantile quant = SensorQuantile.newBuilder()
							.setSensorId(sensorId)
							.setSignalId(field)
							.setMin(sketchMap.get(sensorId).get(field).getMinValue())
							.setMax(sketchMap.get(sensorId).get(field).getMaxValue())
							.setIntervalStart(lastPublishTime)
							.setIntervalEnd(now)
							.setRetainedItems(sketchMap.get(sensorId).get(field).getRetainedItems())
							.setQuantile(ByteBuffer.wrap(sketchMap.get(sensorId).get(field).toByteArray()))
							.build();
					
					ctx.newOutputMessage(quantDbSinkTopic, Schema.AVRO(SensorQuantile.class))
						.value(quant)
						.send();
					
				} catch (PulsarClientException e) {
					ctx.getLogger().error("Unable to publish to ", e);
				}
			});
		});

		sketchMap.clear();
		lastPublishTime = System.currentTimeMillis();
		
	}

	protected UpdateDoublesSketch getSketch(Integer sensorId, String fieldName) {
		
		if (!sketchMap.containsKey(sensorId)) {
			sketchMap.put(sensorId, new HashMap<String, UpdateDoublesSketch> ());
		}
		
		if (!sketchMap.get(sensorId).containsKey(fieldName)) {
			sketchMap.get(sensorId).put(fieldName, DoublesSketch.builder().build());
		}
		return sketchMap.get(sensorId).get(fieldName);
	}

}

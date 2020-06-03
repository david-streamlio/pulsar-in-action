package com.manning.pulsar.chapter7.dataflow.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.inferred.freebuilder.shaded.org.apache.commons.lang3.StringUtils;

/**
 * 
 * @see https://www.enterpriseintegrationpatterns.com/patterns/messaging/Aggregator.html
 * 
 * Assumes response will be <driver-id, deliver-fee> tuple.
 */
public class AggregatorFunction implements Function<String, Void> {

	boolean initalized = false;
	String controlTopic;
	Map<Long, Map<String, String>> active = new HashMap<Long, Map<String, String>> ();
	Map<Long, List<Pair<Long, Float>>> bids = new HashMap<Long, List<Pair<Long, Float>>> ();
	
	@Override
	public Void process(String input, Context context) throws Exception {
		
		if (!initalized) {
			init(context);
		}
		
		Map<String, String> props = context.getCurrentRecord().getProperties();
		Long correlationId = Long.valueOf(props.get("correlation-id"));
		
		if (context.getCurrentRecord().getTopicName().get().equals(controlTopic)) {
			active.put(correlationId, props);
			bids.put(correlationId, new ArrayList<Pair<Long, Float>>());
		} else {
			String[] response = StringUtils.split(input);
			bids.get(correlationId).add(
					Pair.of(Long.valueOf(response[0]), Float.valueOf(response[1])));
			
			// See if we got all the bids back
			if (Integer.valueOf(active.get(correlationId).get("num-recipients"))
					.equals(Integer.valueOf(bids.get(correlationId).size()))) {
				
				// Select the winner, notify them
				String winner = selectWinner(correlationId);
				context.newOutputMessage(winner, Schema.INT64).value(correlationId);
				
				// Free up some memory
				active.remove(correlationId);
				bids.remove(correlationId);
			}
		}
		
		return null;
	}

	private void init(Context context) {
		controlTopic = context.getUserConfigValue("control-topic").toString();
		initalized = true;
	}
	
	/*
	 * Pick best bid and return the associated output topic
	 */
	private String selectWinner(Long correlationId) {
		bids.get(correlationId);
		return null;
	}

}

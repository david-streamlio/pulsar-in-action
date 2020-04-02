package com.gottaeat.services.geoencoding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.common.Address;

public class GeoEncodingAggregator implements Function<Address, Void> {

	private Map<UUID, List<Address>> responses = new HashMap<UUID, List<Address>> ();
	
	@Override
	public Void process(Address addr, Context ctx) throws Exception {
		
		UUID correlationId = UUID.fromString(ctx.getCurrentRecord().getProperties().get("correlation-ID"));
		Address best = null;
		
		if (addr == null) {
			// We received the "times up" message, so pick the best from what we have
			best = winner(correlationId);
		} else {
			best = addToResponses(ctx.getCurrentRecord().getProperties(), addr);
		}
		
		if (best != null) {
			ctx.newOutputMessage("", AvroSchema.of(Address.class))
				.value(best)
				.property("correlation-ID", correlationId.toString())
				.sendAsync();
		}
		
		return null;
	}
	
	private Address winner(UUID correlationId) {
		
		if (responses.containsKey(correlationId)) {
			return responses.get(correlationId).get(0);
		}
		return null;
	}

	private Address addToResponses(Map<String, String> map, Address addr) {
		UUID uuid = UUID.fromString(map.get("correlation-ID"));
		int expectedResponses = Integer.parseInt(map.get("requests-sent"));
		
		if (!responses.containsKey(uuid)) {
			responses.put(uuid, new ArrayList<Address>());
		}
		responses.get(uuid).add(addr);
		return (responses.get(uuid).size() >= expectedResponses) ? winner(uuid) : null;
	}

}

package com.gottaeat.services.geoencoding;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.apache.pulsar.functions.api.Record;

import com.gottaeat.domain.common.Address;
import com.gottaeat.domain.common.GeoEncodedAddress;

/**
 * In a this pattern we have a single point of entry that exposes a well defined API, 
 * and routes requests to downstream services based on endpoints, routes, methods, 
 * and client features.
 * 
 * Having an abstraction layer between clients and downstream services facilitates 
 * incremental updates, rolling releases, and parallel versioning. Downstream services 
 * can be owned by different teams, decoupling release cycles, reducing the need for 
 * cross-team coordination, and improving API lifecycle and evolvability.
 * 
 * This pattern is known as the Robust API, API Gateway or Gateway Router
 * 
 * @see https://www.enterpriseintegrationpatterns.com/patterns/messaging/BroadcastAggregate.html
 * @see https://medium.com/@eduardoromero/serverless-architectural-patterns-261d8743020#96a6
 *
 */
public class GeoEncodingService implements Function<Address, Void> {

	
	boolean initalized = false;
	String controlTopic;
	String gatherTopic;
	Map<String, CircuitStatus> statusMap = new HashMap<String, CircuitStatus> ();
	
	@Override
	public Void process(Address addr, Context context) throws Exception {
		if (!initalized) {
			init(context);
		}
		
		if (context.getCurrentRecord().getTopicName().get().equals(controlTopic)) {
			handleControlMsg(context.getCurrentRecord());
		} else {
			context.incrCounter("msg-counter", 1);
			boolean sendToHalfOpen = context.getCounter("msg-counter") % 2 == 0;
			String correlationId = UUID.randomUUID().toString();
			int num = calculateNumRequestsSent(sendToHalfOpen);
			
			if (num == 0) { // All the providers are offline.
				context.getCurrentRecord().fail();
			}
			
			// These all go to the CircuitBreakers then on to the Web services
			statusMap.forEach( (topic, status) -> {
				if ((status == CircuitStatus.OPEN) || 
					(status == CircuitStatus.HALF_OPEN && sendToHalfOpen)) {
					try {
						context.newOutputMessage(topic, AvroSchema.of(Address.class))
							.property("correlation-ID", correlationId)
							.property("requests-sent", num + "")
							.properties(context.getCurrentRecord().getProperties())
							.value(addr)
							.sendAsync();
					} catch (PulsarClientException e) {
						e.printStackTrace();
					}
				}
			});
			
			// Send a 'times up" message in case all requests timeout, etc.
			context.newOutputMessage("aggregator-topic", AvroSchema.of(GeoEncodedAddress.class))
				.value(null)
				.property("correlation-ID", correlationId)
				.deliverAfter(1, TimeUnit.MINUTES);
		}
		
		return null;
	}
	
	private void init(Context context) {
		controlTopic = context.getUserConfigValue("control-topic").toString();
		initalized = true;
	}
	
	private int calculateNumRequestsSent(boolean half) {
		
		Predicate<CircuitStatus> openPredicate = item -> item == CircuitStatus.OPEN;
		Predicate<CircuitStatus> halfOpenPredicate = item -> item == CircuitStatus.HALF_OPEN;
		
		return statusMap.values().stream().filter(openPredicate).collect(Collectors.toList()).size() + 
				(half ? statusMap.values().stream().filter(halfOpenPredicate).collect(Collectors.toList()).size(): 0);
	}
	
	// CircuitBreakers register with us, and notify us of any status changes
	private void handleControlMsg(Record<?> currentRecord) {
		String destination = currentRecord.getProperties().get("circuit-breaker-topic");
		CircuitStatus status = CircuitStatus.valueOf(currentRecord.getProperties().get("circuit-breaker-status"));
		statusMap.put(destination, status);
	}

}

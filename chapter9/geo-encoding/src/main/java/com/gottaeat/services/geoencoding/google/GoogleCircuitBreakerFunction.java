package com.gottaeat.services.geoencoding.google;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.common.Address;
import com.gottaeat.services.geoencoding.CircuitStatus;

/**
 * The consumer should invoke a remote service via a proxy that behaves in a 
 * similar fashion to an electrical circuit breaker. When the number of consecutive 
 * failures crosses a threshold, the circuit breaker trips, and for the duration of 
 * a timeout period, all attempts to invoke the remote service will fail immediately. 
 * After the timeout expires the circuit breaker allows a limited number of test 
 * requests to pass through. If those requests succeed, the circuit breaker resumes 
 * normal operation. Otherwise, if there is a failure, the timeout period begins again. 
 * 
 * This pattern is suited to, prevent an application from trying to invoke a remote 
 * service or access a shared resource if this operation is highly likely to fail.
 *
 */
public class GoogleCircuitBreakerFunction implements Function<Address, Void> {

	static final BigInteger ZERO = new BigInteger("0");
	static final BigInteger ONE = new BigInteger("1");
	
	CircuitStatus status = CircuitStatus.CLOSED;
	int threshold = Integer.MAX_VALUE;
	int resetTimer = 2; // How long to remain in closed state (in minutes)
	int resetThreshold = 10; // How many success messages require to transition from HALF_OPEN to OPEN
	boolean initalized = false;
	String sourceControlTopic; // To talk to the upstream Scatter function
	String failureNotificationTopic; // To receive failures from service
	
	@Override
	public Void process(Address addr, Context context) throws Exception {
		
		if (!initalized) {
			init(context);
		}
		
		if (context.getCurrentRecord().getTopicName().get().equals(failureNotificationTopic)) {
			handleFailureMsg(context);
		} else { 
			if (status == CircuitStatus.HALF_OPEN) {
				handleHalfOpenMsg(context);
			}
			// Forward Address to Geo-Encoding service
			context.newOutputMessage(context.getInputTopics().iterator().next(), 
					AvroSchema.of(Address.class))
			.properties(context.getCurrentRecord().getProperties()) // Pass gather-addr, correlationId
			.send();
		}
		
		return null;
	}

	private void handleFailureMsg(Context context) throws PulsarClientException {
		BigInteger count = new BigInteger(context.getState("consecutive-failures").array()).add(ONE);
		context.putState("consecutive-failures", ByteBuffer.wrap(count.toByteArray()));
		context.putState("consecutive-successes", ByteBuffer.wrap(ZERO.toByteArray()));
		
		if (count.intValue() >= threshold) {
			closeCircuit(context);
		}
	}

	private void handleHalfOpenMsg(Context context) throws PulsarClientException {
		BigInteger count = new BigInteger(context.getState("consecutive-successes").array()).add(ONE);
		context.putState("consecutive-successes", ByteBuffer.wrap(count.toByteArray()));
		if (count.intValue() >= resetThreshold) {
			openCircuit(context);
		}
	}
	
	private void init(Context context) throws PulsarClientException {
		resetTimer = Integer.parseInt(context.getUserConfigValue("reset-timer").toString());
		threshold = Integer.parseInt(context.getUserConfigValue("failure-threshold").toString());
		sourceControlTopic = context.getUserConfigValue("source-control-topic").toString();
		failureNotificationTopic = context.getUserConfigValue("failure-notification-topic").toString();
		
		context.putState("consecutive-failures", ByteBuffer.wrap(ZERO.toByteArray()));
		context.putState("consecutive-successes", ByteBuffer.wrap(ZERO.toByteArray()));
		openCircuit(context);
		initalized = true;
	}
	
	private void openCircuit(Context context) throws PulsarClientException {
		context.newOutputMessage(sourceControlTopic, AvroSchema.of(Address.class))
		.property("circuit-breaker-topic", context.getInputTopics().iterator().next())
		.property("circuit-breaker-status", CircuitStatus.OPEN.toString())
		.send();
		status = CircuitStatus.OPEN;
	}

	private void closeCircuit(Context context) throws PulsarClientException {
		context.newOutputMessage(sourceControlTopic, AvroSchema.of(Address.class))
		.property("circuit-breaker-topic", context.getInputTopics().iterator().next())
		.property("circuit-breaker-status", CircuitStatus.CLOSED.toString())
		.send();
		
		status = CircuitStatus.CLOSED;
		
		// Send a delayed message for HALF_OPEN
		context.newOutputMessage(sourceControlTopic, AvroSchema.of(Address.class))
		.property("circuit-breaker-topic", context.getInputTopics().iterator().next())
		.property("circuit-breaker-status", CircuitStatus.HALF_OPEN.toString())
		.deliverAfter(resetTimer, TimeUnit.MINUTES);
		
		context.putState("consecutive-failures", ByteBuffer.wrap(ZERO.toByteArray()));
	}
}

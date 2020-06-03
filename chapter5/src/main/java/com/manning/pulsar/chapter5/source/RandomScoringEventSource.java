package com.manning.pulsar.chapter5.source;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.pulsar.io.core.Source;
import org.apache.pulsar.io.core.SourceContext;

public class RandomScoringEventSource implements Source<String> {

	private static long startTime;
	private static Random rand = new Random();
	private SourceContext context;
	private AtomicLong counter = new AtomicLong();
	
	public void close() throws Exception {
		// Nothing to close
	}

	public void open(Map<String, Object> config, SourceContext ctx) throws Exception {
		context = ctx;
		startTime = System.currentTimeMillis(); 
	}

	public ScoringEventRecord read() throws Exception {
		context.recordMetric("RandomScoringEvent", counter.incrementAndGet());
		return new ScoringEventRecord(generateScoringEvent());   
	}

	private static ScoringEvent generateScoringEvent() {
		return new ScoringEvent(UUID.randomUUID(), 
				startTime + rand.nextInt(90000), 
				rand.nextBoolean() ? 
						rand.nextInt(50) : rand.nextInt(50) * -1);
	} 
}

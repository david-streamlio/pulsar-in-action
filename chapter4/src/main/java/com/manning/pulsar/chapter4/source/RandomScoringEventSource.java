package com.manning.pulsar.chapter4;

import java.util.Map;
import java.util.Random;

import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.core.Source;
import org.apache.pulsar.io.core.SourceContext;

public class RandomIntSource implements Source<Integer> {

	private Random rnd;
	private int maxValue;
	
	public void close() throws Exception {
		// Nothing to close
	}

	public void open(Map<String, Object> config, SourceContext context) throws Exception {
		rnd = new Random();
		config.getOrDefault("maxValue", new Integer(100));
	}

	public Record<Integer> read() throws Exception {
		return new RandomIntRecord(rnd.nextInt(maxValue));
	}
}

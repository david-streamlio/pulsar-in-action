package com.manning.pulsar.chapter4;

import java.util.Optional;
import org.apache.pulsar.functions.api.Record;

public class RandomIntRecord implements Record<Integer> {
	
	private Integer value;
	private Long time;
	
	public RandomIntRecord(Integer value) {
		this.value = value;
		this.time = System.currentTimeMillis();
	}

	public Integer getValue() {
		return value;
	}
	
	public Optional<Long> getEventTime() {
		return Optional.ofNullable(this.time);
	}
}

package com.manning.pulsar.chapter4.source;

import java.io.IOException;

import org.apache.pulsar.functions.api.Record;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScoringEventRecord implements Record<String> {
	
	private static final ObjectMapper mapper = new ObjectMapper();
	private ScoringEvent event;
	
	public ScoringEventRecord(ScoringEvent event) {
		this.event = event;
	}
	
	public ScoringEventRecord(String s) {
		try {
			this.event = mapper.readValue(s, ScoringEvent.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getValue() {
		try {
			return mapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}

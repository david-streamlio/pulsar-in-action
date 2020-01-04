package com.manning.pulsar.chapter4.types;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.pulsar.functions.api.SerDe;

public class ScoringEventSerDe implements SerDe<ScoringEvent> {
	
	@Override
	public ScoringEvent deserialize(byte[] input) {
		String s = new String(input);
	      String[] fields = s.split(Pattern.quote("|"));
	      return new ScoringEvent(Long.valueOf(fields[0]),
	                              LocalDateTime.parse(fields[1],
	                                  DateTimeFormatter.ISO_INSTANT),
	                              Integer.valueOf(fields[2]));
	}

	@Override
	public byte[] serialize(ScoringEvent input) {
		return String.format(Locale.US, "%d|%s|%d",
		         input.getPlayerId(), 
		         input.getTs().format(DateTimeFormatter.ISO_INSTANT), 
		            input.getAdjustment()).getBytes();
	}
}

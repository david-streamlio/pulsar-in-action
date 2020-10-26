package com.manning.pulsar.chapter5.source;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.pulsar.functions.api.Record;

public class FileRecord implements Record<String> {

	public static final String SOURCE = "Source";
	public static final String LINE = "Line-no";
	private String content;
	private Map<String, String> props;
		
	public FileRecord(String content, String src, int lineNumber) {
		this.content = content;
		this.props = new HashMap<String, String>();
		this.props.put(SOURCE, src);
		this.props.put(LINE, lineNumber + "");
	}

	@Override
	public Optional<String> getKey() {
		return Optional.ofNullable(props.get(SOURCE));
	}

	@Override
	public Map<String, String> getProperties() {
		return props;
	}

	@Override
	public String getValue() {
		return content;
	}
}

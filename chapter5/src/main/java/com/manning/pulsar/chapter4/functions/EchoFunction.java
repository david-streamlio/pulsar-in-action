package com.manning.pulsar.chapter4.functions;

import java.util.function.Function;

public class EchoFunction implements Function<String, String> {

	public String apply(String input) {
		return input;
	}
}

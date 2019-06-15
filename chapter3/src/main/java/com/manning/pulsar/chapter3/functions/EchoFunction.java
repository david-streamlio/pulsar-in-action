package com.manning.pulsar.chapter3.functions;

import java.util.function.Function;

public class EchoFunction implements Function<String, String> {

	public String apply(String input) {
		return input;
	}
}

package com.manning.pulsar.chapter4.functions.sdk;

import java.util.Optional;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;
import org.slf4j.Logger;

public class UserConfigFunction implements Function<String, Void> {

	@Override
	public Void process(String input, Context ctx) throws Exception {
		Logger LOG = ctx.getLogger();
        Optional<Object> wotd = ctx.getUserConfigValue("word-of-the-day");
        if (wotd.isPresent()) {
            LOG.info("The word of the day is {}", wotd);
        } else {
            LOG.warn("No word of the day provided");
        }
        return null;
	}

}

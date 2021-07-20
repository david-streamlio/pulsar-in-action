/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.manning.pulsar.loadgen;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

/**
 * This function simulates a function that takes a long time to process,
 * such as making a call to an external API or system. This allows us to
 * generate back-pressure conditions due to the function taking a long 
 * time to process each message.
 *
 */
public class SleepingFunction implements Function<Long, Void> {

	static final String SLEEP_DURATION = "sleep-duration";
	static final String DEFAULT = "5000";
	private boolean init = false;
	private long napTime = 0;
	
	@Override
	public Void process(Long input, Context ctx) throws Exception {
		if (!init) {
			init(ctx);
		}
		
		try {
			Thread.sleep(napTime);
		} catch (InterruptedException e) {
			// Ignore
		}
		
		return null;
	}

	private void init(Context ctx) {
		if (ctx != null) {
			napTime = Long.parseLong((String) ctx.getUserConfigValueOrDefault(SLEEP_DURATION, DEFAULT));
			if (napTime < 0) {
				napTime = 0;
			}
			init = true;
		}
	}

}

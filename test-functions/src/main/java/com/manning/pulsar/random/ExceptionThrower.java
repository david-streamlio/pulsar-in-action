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
package com.manning.pulsar.random;

import java.util.Random;

import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.manning.pulsar.AbstractTestFunction;

public class ExceptionThrower extends AbstractTestFunction implements Function<String, Void> {

	static final String PROBABILITY = "probability";
	static final String SNOOZE = "snooze";
	
	private static final Random rnd = new Random();
	
	private boolean initialized = false;
	private double exceptionProbability = 0.0;
	private long snooze = 0;
	
	@Override
	public Void process(String input, Context ctx) throws Exception {
       if (!initialized) {
          init(ctx);   
       }
      
       if (rnd.nextDouble() > exceptionProbability) {
    	  if (snooze > 0) {
    		 try { Thread.sleep(snooze); } catch (final Exception ex) {}
    	  }
    	  log("Throwing a runtime exception");
    	  throw new RuntimeException("Random fatal exception occurred");
       }
       
       return null;
	}

	protected void init(Context ctx) {
	   super.init(ctx);
       if (ctx != null) {
    	   exceptionProbability = Double.parseDouble(
    		(String) ctx.getUserConfigValueOrDefault(PROBABILITY, "0.5"));
    	   
    	   snooze = Long.parseLong((String) ctx.getUserConfigValueOrDefault(SNOOZE, "1000"));
       }
	}

}

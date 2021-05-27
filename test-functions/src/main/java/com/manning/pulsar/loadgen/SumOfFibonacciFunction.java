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

import com.manning.pulsar.AbstractTestFunction;

/**
 * This function computes the sum of the first N Fibonacci numbers,
 * where the value of N is provided as the input message. 
 * 
 * Since computing the Fibonacci sequence for large values is very 
 * CPU intensive, this function can be used for testing the cluster
 * under heavy CPU load conditions.
 *
 */
public class SumOfFibonacciFunction extends AbstractTestFunction implements Function<String, Long> {

	static final String MAX = "max";
	private boolean init = false;
	private int max = 0;
	
	@Override
	public Long process(String input, Context ctx) throws Exception {
		
		long start = System.currentTimeMillis();
		
		if (!init) {
		  init(ctx);
		}
		
		long sum = 0;
		for (int idx = 1; idx < max; idx++) {
			long f = fib(idx);
			log(String.format("Calculated fib(%d) = %d", idx, f));
			sum += f;
		}
		
		log(String.format("Took %d seconds to calculate the first %d Fibonacci numbers", 
			((System.currentTimeMillis() - start)/1000), max));
		
		return sum;
	}
	
	protected void init(Context ctx) {
      super.init(ctx);
	  if (ctx != null) {
		  max = Integer.parseInt((String) ctx.getUserConfigValueOrDefault(MAX, "46"));
		  init = true;
	  }
	}

	private static final long fib(long n) {
      if ((n == 0) || (n == 1))
       return n;
      else
        return fib(n - 1) + fib(n - 2);
	}
	
}

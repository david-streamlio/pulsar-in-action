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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.apache.pulsar.functions.api.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class SumOfFibonacciFunctionTests {

	@Mock
	private Context mockContext;
	
	@Mock 
	private Logger mockLogger;
	
	private SumOfFibonacciFunction func = new SumOfFibonacciFunction();
	
	@Before
	public final void init() {
		when(mockContext.getLogger()).thenReturn(mockLogger);
		when(mockContext.getFunctionName()).thenReturn("SumOfFibonacciFunction");
		when(mockContext.getFunctionId()).thenReturn("1");
		when(mockContext.getInstanceId()).thenReturn(1);
		when(mockContext.getNumInstances()).thenReturn(1);
	}
	
	@Test
	public final void NegativeNumberTest() throws Exception {
		when(mockContext.getUserConfigValueOrDefault(SumOfFibonacciFunction.MAX, "50")).thenReturn("-1");
		Long result = func.process("", mockContext);
		assertEquals(new Long(0L), result);
	}
	
	@Test
	public final void First10Fibonacci() throws Exception {
		when(mockContext.getUserConfigValueOrDefault(SumOfFibonacciFunction.MAX, "50")).thenReturn("10");
		Long result = func.process("", mockContext);
		assertEquals(new Long(88), result);
	}
	
	@Test
	public final void First40Fibonacci() throws Exception {
		when(mockContext.getUserConfigValueOrDefault(SumOfFibonacciFunction.MAX, "50")).thenReturn("40");
		Long result = func.process("", mockContext);
		assertEquals(new Long(165580140), result);
	}
	
	@Test
	public final void First50Fibonacci() throws Exception {  // Takes 72 seconds
		when(mockContext.getUserConfigValueOrDefault(SumOfFibonacciFunction.MAX, "50")).thenReturn("50");
		Long result = func.process("", mockContext);
		assertEquals(new Long(20365011073L), result);
	}
	
}

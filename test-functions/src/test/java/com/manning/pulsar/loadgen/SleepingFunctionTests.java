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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import org.apache.pulsar.functions.api.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SleepingFunctionTests {

	@Mock
	private Context mockContext;
	
	private SleepingFunction func = new SleepingFunction();
	
	@Test
	public final void negativeNumberTest() throws Exception {
		when(mockContext.getUserConfigValueOrDefault(SleepingFunction.SLEEP_DURATION, "5000")).thenReturn("-1");
		long startTime = System.currentTimeMillis();
		func.process(1L, mockContext);
		assertTrue(System.currentTimeMillis() - startTime < 1000);
	}
	
	@Test
	public final void OneSecondTest() throws Exception {
		when(mockContext.getUserConfigValueOrDefault(SleepingFunction.SLEEP_DURATION, "5000")).thenReturn("1000");
		long startTime = System.currentTimeMillis();
		func.process(1L, mockContext);
		long finish = System.currentTimeMillis();
		
		assertTrue(finish - startTime >= 1000);
		assertTrue(finish - startTime < 1500);
	}
	
	@Test
	public final void TenSecondTest() throws Exception {
		when(mockContext.getUserConfigValueOrDefault(SleepingFunction.SLEEP_DURATION, "5000")).thenReturn("10000");
		long startTime = System.currentTimeMillis();
		func.process(1L, mockContext);
		long finish = System.currentTimeMillis();
		
		assertTrue(finish - startTime >= 10000);
		assertTrue(finish - startTime < 10500);
	}
}

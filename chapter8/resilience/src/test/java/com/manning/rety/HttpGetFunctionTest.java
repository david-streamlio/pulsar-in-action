package com.manning.rety;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.manning.retry.HttpGetFunction;

public class HttpGetFunctionTest {

	private HttpGetFunction fn;
	
	@Before
	public final void init() {
		fn = new HttpGetFunction();
	}
	
	@Test
	public final void simpleTest() throws Exception {
		String s = fn.process("Pulsar", null);
		assertNotNull(s);
	}
	
}

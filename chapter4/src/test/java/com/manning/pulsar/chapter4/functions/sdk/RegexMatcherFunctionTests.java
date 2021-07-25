package com.manning.pulsar.chapter4.functions.sdk;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.pulsar.functions.api.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RegexMatcherFunctionTests {

	private static final String ZIP_CODE_REGEX = "^[0-9]{5}(?:-[0-9]{4})?$";
	private static final String FUNCTION_NAME = "my-regex-function";
	
	private RegexMatcherFunction function = new RegexMatcherFunction();
	
	@Mock
	private Context mockedContext;
	
	@Before
	public final void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	

	@Test
	public final void matchesTest() throws Exception {
		when(mockedContext.getUserConfigValue(RegexMatcherFunction.REGEX_CONFIG))
		  .thenReturn(Optional.of(ZIP_CODE_REGEX));
		when(mockedContext.getFunctionName()).thenReturn(FUNCTION_NAME);
		
		String word = "90210";
		String result = function.process(word, mockedContext);
		assertNull(result);
		verify(mockedContext, times(1)).getFunctionName();
	}
	
	@Test
	public final void doesNotMatchTest() throws Exception {
		when(mockedContext.getUserConfigValue(RegexMatcherFunction.REGEX_CONFIG))
		   .thenReturn(Optional.of(ZIP_CODE_REGEX));
		String bad = "123456-78901";
		String result = function.process(bad, mockedContext);
		assertNull(result);
		verify(mockedContext, times(0)).getFunctionName();
	}
}

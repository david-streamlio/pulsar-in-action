package com.manning.pulsar.chapter4.functions.sdk;

import static org.mockito.Mockito.*;

import java.util.Optional;

import static org.junit.Assert.*;

import org.apache.pulsar.functions.api.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class KeywordFilterFunctionTests {
	
	private KeywordFilterFunction function = new KeywordFilterFunction();
	
	@Mock
	private Context mockedContext;
	
	@Before
	public final void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	

	@Test
	public final void containsKeywordTest() throws Exception {
		when(mockedContext.getUserConfigValue(KeywordFilterFunction.KEYWORD_CONFIG)).thenReturn(Optional.of("dog"));
		String sentence = "The brown fox jumped over the lazy dog";
		String result = function.process(sentence, mockedContext);
		assertNotNull(result);
		assertEquals(sentence, result);
	}
	
	@Test
	public final void doesNotContainKeywordTest() throws Exception {
		when(mockedContext.getUserConfigValue(KeywordFilterFunction.KEYWORD_CONFIG)).thenReturn(Optional.of("cat"));
		String sentence = "It was the best of times, it was the worst of times";
		String result = function.process(sentence, mockedContext);
		assertNull(result);
	}
	
	@Test
	public final void ignoreCaseTest() {
		when(mockedContext.getUserConfigValue(KeywordFilterFunction.KEYWORD_CONFIG)).thenReturn(Optional.of("RED"));
		when(mockedContext.getUserConfigValue(KeywordFilterFunction.IGNORE_CONFIG)).thenReturn(Optional.of(Boolean.TRUE));
		String sentence = "Everyone watched the red sports car drive off.";
		String result = function.process(sentence, mockedContext);
		assertNotNull(result);
		assertEquals(sentence, result);
	}

}

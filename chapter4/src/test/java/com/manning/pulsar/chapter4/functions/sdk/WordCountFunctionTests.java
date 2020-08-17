package com.manning.pulsar.chapter4.functions.sdk;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.apache.pulsar.functions.api.Context;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WordCountFunctionTests {
	
	private WordCountFunction function = new WordCountFunction();
	
	@Mock
	private Context mockedContext;
	
	@Before
	public final void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	

	@Test
	public final void singleSentenceTest() throws Exception {
		String sentence = "The brown fox jumped over the lazy dog";
		Integer result = function.process(sentence , mockedContext);
		
		assertEquals(8, result.intValue());
		verify(mockedContext, times(1)).incrCounter("The", 1);
		verify(mockedContext, times(1)).incrCounter("brown", 1);
		verify(mockedContext, times(1)).incrCounter("fox", 1);
		verify(mockedContext, times(1)).incrCounter("jumped", 1);
		verify(mockedContext, times(1)).incrCounter("over", 1);
		verify(mockedContext, times(1)).incrCounter("the", 1);
		verify(mockedContext, times(1)).incrCounter("lazy", 1);
		verify(mockedContext, times(1)).incrCounter("dog", 1);
	}
	
	@Test
	public final void multipleSentencesTest() throws Exception {
		String[] sentences = new String[]{
		  "The brown fox jumped over the lazy dog", 
		  "The cat and the fiddle", 
		  "The cow jumped over the moon",
		  "The little dog laughed to see such sport",
		  "And the dish ran away with the spoon"};
		
		Integer total = new Integer(0);
		for (String s: sentences) {
			total += function.process(s , mockedContext);
		}
		assertEquals(35, total.intValue());
		verify(mockedContext, times(4)).incrCounter("The", 1);
		verify(mockedContext, times(5)).incrCounter("the", 1);
		verify(mockedContext, times(2)).incrCounter("dog", 1);
	}
}

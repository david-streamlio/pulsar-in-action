package com.manning.pulsar.chapter3;

import static org.junit.Assert.assertNotNull;

import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.core.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.manning.pulsar.chapter3.source.RandomScoringEventSource;

public class RandomScoringEventSourceTest {

	@Mock
	private SourceContext mockSourceContext;
	private RandomScoringEventSource source;
	
	@Before
	public final void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		source = new RandomScoringEventSource();
		source.open(null, mockSourceContext);
	}
	
	@Test
	public final void readTest() throws Exception {
		Record<String> event = source.read();
		assertNotNull(event.getValue());
		System.out.println(event.getValue());
	}
}

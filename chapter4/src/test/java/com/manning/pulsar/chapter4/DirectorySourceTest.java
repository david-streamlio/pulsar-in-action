package com.manning.pulsar.chapter4;

import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.io.core.SourceContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.manning.pulsar.chapter4.source.DirectorySource;

public class DirectorySourceTest {
	
	@Mock
	private SourceContext mockSourceContext;
	private DirectorySource source;
	private Map<String, Object> map;
	
	@Before
	public final void init() throws Exception {
		MockitoAnnotations.initMocks(this);
		source = new DirectorySource();
	}
	
	@Test(expected = RuntimeException.class)
	public final void badInputDirTest() throws Exception {
		map = new HashMap<String, Object> ();
		map.put("inputDir", "");
		source.open(map, mockSourceContext);
	}
	
	@Test
	public final void anotherTest() throws Exception {
		map = new HashMap<String, Object> ();
		map.put("inputDir", "/tmp/events");
		source.open(map, mockSourceContext);
	}

}

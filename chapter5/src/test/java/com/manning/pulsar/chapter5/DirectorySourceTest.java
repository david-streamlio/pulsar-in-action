package com.manning.pulsar.chapter5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.pulsar.io.core.SourceContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import com.manning.pulsar.chapter5.source.DirectorySource;
import com.manning.pulsar.chapter5.source.FileRecord;

public class DirectorySourceTest {

	final static Path SOURCE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "source");
	final static Path PROCESSED_DIR = Paths.get(System.getProperty("java.io.tmpdir"),"processed");
	
	private Path srcPath;
	private Path processedPath;
	
	private DirectorySource spySource;
	
	@Mock
	private SourceContext mockedContext;
	
	@Mock
	private Logger mockedLogger;
	
	@Captor
	private ArgumentCaptor<FileRecord> captor;
	
	@Before
	public final void init() throws IOException {
	  MockitoAnnotations.initMocks(this);
	  when(mockedContext.getLogger()).thenReturn(mockedLogger);
	  
	  FileUtils.deleteDirectory(SOURCE_DIR.toFile());
	  FileUtils.deleteDirectory(PROCESSED_DIR.toFile());
	  srcPath = Files.createDirectory(SOURCE_DIR, 
	     PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
	  processedPath = Files.createDirectory(PROCESSED_DIR,
         PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
		 
	  spySource = spy(new DirectorySource());
	}
	
	@After
	public final void cleanUp() throws IOException {
  	  FileUtils.deleteDirectory(SOURCE_DIR.toFile());
	  FileUtils.deleteDirectory(PROCESSED_DIR.toFile());
	}
	
	@Test
	public final void oneLineTest() throws Exception {
		Files.copy(getFile("single-line.txt"),Paths.get(srcPath.toString(), "single-line.txt"), COPY_ATTRIBUTES);
		Map<String, Object> configs = new HashMap<String, Object>();
		configs.put("inputDir", srcPath.toFile().getAbsolutePath());
		configs.put("processedDir", processedPath.toFile().getAbsolutePath());
		
		spySource.open(configs, mockedContext);
		Thread.sleep(3000);
		
		Mockito.verify(spySource).consume(captor.capture());
		FileRecord captured = captor.getValue();
		assertNotNull(captured);
		assertEquals("It was the best of times; it was the worst of times", captured.getValue());
		assertEquals("1", captured.getProperties().get(FileRecord.LINE));
		assertTrue(captured.getProperties().get(FileRecord.SOURCE).contains("single-line.txt"));
	}
	
	@Test
	public final void multiLineTest() throws Exception {
		Files.copy(getFile("example-1.txt"),Paths.get(srcPath.toString(), "example-1.txt"), COPY_ATTRIBUTES);
		Map<String, Object> configs = new HashMap<String, Object>();
		configs.put("inputDir", srcPath.toFile().getAbsolutePath());
		configs.put("processedDir", processedPath.toFile().getAbsolutePath());
		
		spySource.open(configs, mockedContext);
		Thread.sleep(3000);
		
		Mockito.verify(spySource, times(113)).consume(captor.capture());
		
		final AtomicInteger counter = new AtomicInteger(0);
		captor.getAllValues().forEach(rec -> {
		  assertNotNull(rec.getValue());
		  assertEquals(counter.incrementAndGet() + "", rec.getProperties().get(FileRecord.LINE));
		  assertTrue(rec.getProperties().get(FileRecord.SOURCE).contains("example-1.txt"));
		});
	}
	
	private static Path getFile(String fileName) throws IOException {
        ClassLoader classLoader = DirectorySourceLocalRunnerTest.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
         
        if (resource == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return new File(resource.getFile()).toPath();
        }
    }

}

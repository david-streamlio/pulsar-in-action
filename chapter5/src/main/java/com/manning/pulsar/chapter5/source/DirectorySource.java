package com.manning.pulsar.chapter5.source;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.io.core.PushSource;
import org.apache.pulsar.io.core.SourceContext;
import org.slf4j.Logger;

public class DirectorySource extends PushSource<String> {
	
	private final ScheduledExecutorService scheduler =
		     Executors.newScheduledThreadPool(1);
	
	private DirectoryConsumerThread scanner;
	
	private Logger log;
	
	@Override
	public void open(Map<String, Object> config, SourceContext context) throws Exception {
		log = context.getLogger();
		String inputDir = (String) config.getOrDefault("inputDir", ".");
		String processedDir = (String) config.getOrDefault("processedDir", ".");
		String frequency = (String) config.getOrDefault("frequency", "10");
		
		scanner = new DirectoryConsumerThread(this, inputDir, processedDir, log);
		scheduler.scheduleAtFixedRate(scanner, 0, Long.parseLong(frequency), TimeUnit.MINUTES);
	    log.info(String.format("Scheduled to run every %s minutes", frequency));
	}
	
	@Override
	public void close() throws Exception {
		log.info("Closing connector");
		scheduler.shutdownNow();
	}
}

package com.manning.pulsar.chapter3.source;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.pulsar.io.core.PushSource;
import org.apache.pulsar.io.core.SourceContext;

public class DirectorySource extends PushSource<String> {
	
	private ExecutorService executor;
	
	@Override
	public void open(Map<String, Object> config, SourceContext context) throws Exception {
		String inputDir = (String) config.getOrDefault("inputDir", ".");
		File[] files = new File(inputDir).listFiles();
		if (files == null || files.length < 1) {
			throw new RuntimeException("Input Directory doesn't contain any files");
		}
		executor = Executors.newFixedThreadPool(1);
		executor.execute(new DirectoryConsumerThread(this, files));
	}
	
	@Override
	public void close() throws Exception {
		executor.shutdown();
        try {
            if (!executor.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
	}
}

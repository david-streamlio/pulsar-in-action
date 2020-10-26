package com.manning.pulsar.chapter5.source;

import static java.nio.file.StandardCopyOption.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.pulsar.io.core.PushSource;
import org.slf4j.Logger;

public class DirectoryConsumerThread extends Thread {

	private final PushSource<String> source;
	private final String baseDir;
	private final String processedDir;
	private final Logger log;
	
	public DirectoryConsumerThread(PushSource<String> source, String base, String processed, Logger log) {
		this.source = source;
		this.baseDir = base;
		this.processedDir = processed;
		this.log = log;
	}
	
	public void run() {
		log.info("Scanning for files.....");
		File[] files = new File(baseDir).listFiles();
		for (int idx = 0; idx < files.length; idx++) {
			log.info(String.format("Processing file %s", files[idx].getName()));
			consumeFile(files[idx]);
		}
	}
	
    private void consumeFile(File file) {
    	log.info(String.format("Consuming file %s", file.getName()));
        try (Stream<String> lines = getLines(file)) {
        	 AtomicInteger counter = new AtomicInteger(0);
             lines.forEach(line -> process(line, file.getPath(), counter.incrementAndGet()));
             
             log.info(String.format("Processed %d lines from %s", counter.get(), file.getName()));
             Files.move(file.toPath(), Paths.get(processedDir).resolve(file.toPath().getFileName()), REPLACE_EXISTING);
             log.info(String.format("Moved file %s to %s", file.toPath().toString(), processedDir));
        } catch (Exception e) {
            log.error("Unable to move processed file", e);
        } 
    }

    private Stream<String> getLines(File file) throws IOException {
        if (file == null) {
            return null;
        } else {
            return Files.lines(Paths.get(file.getAbsolutePath()), Charset.defaultCharset());
        }
    }

    private void process(String line, String src, int lineNum) {
        source.consume(new FileRecord(line, src, lineNum));
    }
}

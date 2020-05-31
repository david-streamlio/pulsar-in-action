package com.manning.pulsar.chapter4.source;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.pulsar.io.core.PushSource;

public class DirectoryConsumerThread extends Thread {

	private final PushSource<String> source;
	private final File[] files;
	
	public DirectoryConsumerThread(PushSource<String> source, File[] files) {
		this.source = source;
		this.files = files;
	}
	
	public void run() {
		for (int idx = 0; idx < files.length; idx++) {
			consumeFile(files[idx]);
		}
	}
	
    private void consumeFile(File file) {
        try (Stream<String> lines = getLines(file)) {
             lines.forEachOrdered(line -> process(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stream<String> getLines(File file) throws IOException {
        if (file == null) {
            return null;
        } else {
            return Files.lines(Paths.get(file.getAbsolutePath()), Charset.defaultCharset());
        }
    }

    private void process(String line) {
        source.consume(new ScoringEventRecord(line));
    }
}

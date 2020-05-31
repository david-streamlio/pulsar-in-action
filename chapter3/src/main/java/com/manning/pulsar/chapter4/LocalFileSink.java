package com.manning.pulsar.chapter4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.core.Sink;
import org.apache.pulsar.io.core.SinkContext;

public class LocalFileSink implements Sink<String> {
	
	private String filenamePrefix;
	private String filenameSuffix;
	private BufferedWriter bw = null;
	private FileWriter fw = null;

	public void close() throws Exception {
		try {
			if (bw != null)
				bw.close();
			if (fw != null)
				fw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void open(Map<String, Object> config, SinkContext sinkContext) throws Exception {
		filenamePrefix = (String) config.getOrDefault("filenamePrefix", "test-out");
		filenameSuffix = (String) config.getOrDefault("filenameSuffix", ".tmp");
		File file = File.createTempFile(filenamePrefix, filenameSuffix);
		fw = new FileWriter(file.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);
	}

	public void write(Record<String> record) throws Exception {
		try {
			bw.write(record.getValue());
			bw.flush();
			record.ack();
		} catch (IOException e) {
			record.fail();
			throw new RuntimeException(e);
		}
	}
}

package com.manning.pulsar.chapter5;

import static java.nio.file.StandardCopyOption.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.common.io.SourceConfig;
import org.apache.pulsar.functions.LocalRunner;


import com.manning.pulsar.chapter5.source.DirectorySource;

public class DirectorySourceLocalRunnerTest {
	
	final static String BROKER_URL = "pulsar://localhost:6650";
	final static String OUT = "persistent://public/default/directory-scan"; 
	final static Path SOURCE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "source");
	final static Path PROCESSED_DIR = Paths.get(System.getProperty("java.io.tmpdir"),"processed");
	
	private static LocalRunner localRunner;
	private static Path srcPath;
	private static Path processedPath;
	
	public static void main(String[] args) throws Exception {
		init();
		startLocalRunner();
		shutdown();
	}
	
	private static void startLocalRunner() throws Exception {
		localRunner = LocalRunner.builder()
				.brokerServiceUrl(BROKER_URL)
				.sourceConfig(getSourceConfig())
				.build();
		localRunner.start(false);
	}
	
	private static void init() throws IOException {
	  Files.deleteIfExists(SOURCE_DIR);
	  Files.deleteIfExists(PROCESSED_DIR);
	  srcPath = Files.createDirectory(SOURCE_DIR, 
	     PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
	  processedPath = Files.createDirectory(PROCESSED_DIR,
         PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxrwx")));
	  
	  Files.copy(getFile("example-1.txt"),Paths.get(srcPath.toString(), "example-1.txt"), COPY_ATTRIBUTES);
	}
	
	private static void shutdown() throws Exception {
		Thread.sleep(30000);
		localRunner.stop();
		System.exit(0);
	}

	private static SourceConfig getSourceConfig() {
		
		Map<String, Object> configs = new HashMap<String, Object>();
		configs.put("inputDir", srcPath.toFile().getAbsolutePath());
		configs.put("processedDir", processedPath.toFile().getAbsolutePath());
		
		return SourceConfig.builder()
				.className(DirectorySource.class.getName())
				.configs(configs)
				.name("directory-source")
				.tenant("public")
				.namespace("default")
				.topicName(OUT)
				.build();
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

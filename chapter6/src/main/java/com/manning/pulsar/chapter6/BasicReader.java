package com.manning.pulsar.chapter6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Reader;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.shade.com.google.common.base.Charsets;

public class BasicReader {
	
	private static PulsarClient client;
	private static Reader<byte[]> reader;
	private static String topicName;
	private static String hostname;
	private static String tokenFilePath;
	private static String certFilePath;
	private static String clientCertPath;
	private static String keyFilePath;

	public static final void main(String[] args) throws IOException {
		
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine cmd = parser.parse(generateOptions(), args);
			topicName = cmd.getArgs()[0];
			
			if (cmd.hasOption('j')) {
				tokenFilePath = cmd.getOptionValue('j');
			} else if (cmd.hasOption("token")) {
				tokenFilePath = cmd.getOptionValue("token");
			}
			
			if (cmd.hasOption('t')) {
				certFilePath = cmd.getOptionValue('t');
			} else if (cmd.hasOption("tlsCerts")) {
				certFilePath = cmd.getOptionValue("tlsCerts");
			}
			
			if (cmd.hasOption('c')) {
				clientCertPath = cmd.getOptionValue('c');
			} else if (cmd.hasOption("client-cert")) {
				clientCertPath = cmd.getOptionValue("client-cert");
			}
			
			if (cmd.hasOption('k')) {
				keyFilePath = cmd.getOptionValue('k');
			} else if (cmd.hasOption("key")) {
				keyFilePath = cmd.getOptionValue("key");
			}
			
			if (cmd.hasOption('h')) {
				hostname = cmd.getOptionValue('h');
			} else if (cmd.hasOption("hostname")) {
				hostname = cmd.getOptionValue("hostname");
			}
			
			read();
		} catch (ParseException e) {
			usage();
		}
		
	}
	
	private static void read() throws PulsarClientException {
		while (getReader().hasMessageAvailable()) {
			Message<byte[]> msg = getReader().readNext();
			System.out.println(String.format("Received message  msgId: %s -- content: '%s'\n",
					msg.getMessageId(), msg.getValue()));
		}
	}
	
	private static PulsarClient getPulsarClient() throws PulsarClientException {
		
		if (client == null) {
			ClientBuilder builder = PulsarClient.builder();
			
			if (tokenFilePath != null) {
				builder = builder
						.authentication(
						        AuthenticationFactory.token(() -> {
									try {
										return new String(Files.readAllBytes(Paths.get(tokenFilePath)), Charsets.UTF_8).trim();
									} catch (IOException e) {
										return "";
									}
								}));
			} else if (clientCertPath != null) {
				builder = builder.authentication(AuthenticationFactory.TLS(clientCertPath, keyFilePath));
			}
			
			if (certFilePath == null) {
				builder = builder.serviceUrl("pulsar://" + hostname + ":6650");
			} else {
				builder = builder.serviceUrl("pulsar+ssl://" + hostname + ":6651")
							.tlsTrustCertsFilePath(certFilePath);
			}
			
			client = builder.build();
		}
		return client;
	}
	
	private static Reader<byte[]> getReader() throws PulsarClientException {
		if (reader == null) {
			reader =
				getPulsarClient()
					.newReader(Schema.BYTES)
				    .topic(topicName)
				    .startMessageId(MessageId.earliest)
				    .create();
		}
		
		return reader;
	}
	
	/**
	 * "Definition" stage of command-line parsing with Apache Commons CLI.
	 * @return Definition of command-line options.
	 */
	private static Options generateOptions() {
		final Option tokenOption = Option.builder("j")
				  .longOpt("token")
				  .hasArg()
				  .desc("Full pathname of JWT token file to use for authentication")
				  .build();  
		
		final Option clientCertOption = Option.builder("c")
				  .longOpt("client-cert")
				  .hasArg()
				  .desc("The full pathname to the client certificate for authentication ")
				  .build();  
		
		final Option privateKeyOption = Option.builder("k")
				  .longOpt("key")
				  .hasArg()
				  .desc("The full pathname to the private key associated with the client certificate")
				  .build();  
		
		final Option hostOption = Option.builder("h")
				  .longOpt("hostname")
				  .hasArg()
				  .desc("Full DNS name of the Pulsar host")
				  .build();  
		
		final Option certOption = Option.builder("t")
				  .longOpt("tlsCerts")
				  .hasArg()
				  .desc("Full pathname of trust certificate file to use for TLS wire encryption")
				  .build(); 
		
		final Options options = new Options();
		options.addOption(tokenOption);
		options.addOption(hostOption);
		options.addOption(certOption);
		options.addOption(clientCertOption);
		options.addOption(privateKeyOption);
		return options;
	}
	
	private static final void usage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Reader <topic name>", generateOptions(), true);
	}
}

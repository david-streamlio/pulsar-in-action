package com.manning.pulsar.chapter3.encryption;

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
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.com.google.common.base.Charsets;


public class EncryptedDemo {
	
	private static PulsarClient client;
	private static String pulsarHostname = "localhost";
	private static String topicName = "persistent://orders/inbound/encrypted-topic";
	private static String tokenFilePath;
	private static String publicKeyPath;
	private static String privateKeyPath;
	private static EncryptedProducerThread producer;
	private static EncryptedConsumerThread consumer;

	public static void main(String[] args) throws PulsarClientException {
	
		CommandLineParser parser = new DefaultParser();
		boolean decrypt = false;
		
		try {
			CommandLine cmd = parser.parse(generateOptions(), args);
			topicName = cmd.getArgs()[0];
			
			if (cmd.hasOption('t')) {
				tokenFilePath = cmd.getOptionValue('t');
			} else if (cmd.hasOption("token")) {
				tokenFilePath = cmd.getOptionValue("token");
			}
			
			if (cmd.hasOption('h')) {
				pulsarHostname = cmd.getOptionValue('h');
			} else if (cmd.hasOption("hostname")) {
				pulsarHostname = cmd.getOptionValue("hostname");
			}
			
			if (cmd.hasOption('p')) {
				publicKeyPath = cmd.getOptionValue('p');
			} else if (cmd.hasOption("publicKey")) {
				publicKeyPath = cmd.getOptionValue("token");
			}
			
			if (cmd.hasOption('s')) {
				privateKeyPath = cmd.getOptionValue('s');
			} else if (cmd.hasOption("privateKey")) {
				privateKeyPath = cmd.getOptionValue("privateKey");
			}
			
			if (cmd.hasOption('d')) {
				decrypt = true;
			} else if (cmd.hasOption("decrypt")) {
				decrypt = true;
			}
			
			producer = new EncryptedProducerThread(
					new RawFileKeyReader(publicKeyPath, null), // Only need the public key to produce
					getPulsarClient(), topicName);
			
			producer.start();
			
			if (decrypt) {
				consumer = new EncryptedConsumerThread(
							new RawFileKeyReader(null, privateKeyPath), // Only need the private key to consume
							getPulsarClient(), topicName);
			} else {
				// Create a consumer that cannot decrypt the messages
				consumer = new EncryptedConsumerThread(null, getPulsarClient(), topicName);
			}
			
			consumer.start();
			
			Thread.sleep(30 * 1000L);
			producer.halt();
			consumer.halt();
			
		} catch (ParseException e) {
			usage();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getPulsarClient().close();
		System.exit(0);
	}
	
	private static PulsarClient getPulsarClient() throws PulsarClientException {
		
		if (client == null) {
			ClientBuilder builder = PulsarClient.builder()
					.serviceUrl("pulsar://" + pulsarHostname + ":6650");
			
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
			}
			
			client = builder.build();
		}
		return client;
	}
	
	
	/**
	 * "Definition" stage of command-line parsing with Apache Commons CLI.
	 * @return Definition of command-line options.
	 */
	private static Options generateOptions() {
		final Option tokenOption = Option.builder("t")
				.longOpt("token")
				.hasArg()
				.desc("Full pathname of JWT token file to use for authentication")
				.build();  

		final Option hostOption = Option.builder("h")
				.longOpt("hostname")
				.hasArg()
				.desc("Full DNS name of the Pulsar host")
				.build();  

		final Option pubKeyOption = Option.builder("p")
				.required()
				.longOpt("publicKey")
				.hasArg()
				.desc("Full pathname of public key file to use for data encryption")
				.build(); 
		
		final Option privKeyOption = Option.builder("s")
				.required()
				.longOpt("privateKey")
				.hasArg()
				.desc("Full pathname of private key file to use for data encryption")
				.build();
		
		final Option decryptOption = Option.builder("d")
				.longOpt("decrypt")
				.desc("Create a consumer that can decrypt the messages")
				.build();  
		
		final Options options = new Options();
		options.addOption(tokenOption);
		options.addOption(hostOption);
		options.addOption(pubKeyOption);
		options.addOption(privKeyOption);
		options.addOption(decryptOption);
		return options;
	}
	
	private static final void usage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Reader <topic name>", generateOptions(), true);
	}

}

package com.manning.pulsar.chapter3;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.avro.file.DataFileReader;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.pulsar.client.api.AuthenticationFactory;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.shade.com.google.common.base.Charsets;

import com.gottaeat.customer.Order;

public class DataPublisher {

	private static PulsarClient client;
	private static Producer<Order> producer;
	private static DataGenerator<Order> generator = new FoodOrderGenerator();
	private static String topicName;
	private static String tokenFilePath;
	
	public static final void main(String[] args) throws IOException {
		
		int num = 1;
		float rate = 0.0f;
		String filename = null;
		CommandLineParser parser = new DefaultParser();
		
		try {
			CommandLine cmd = parser.parse(generateOptions(), args);
			topicName = cmd.getArgs()[0];
			
			if (cmd.hasOption('n')) {
				num = Integer.parseInt(cmd.getOptionValue('n'));
			} else if (cmd.hasOption("num-produce")) {
				num = Integer.parseInt(cmd.getOptionValue("num-produce"));
			}
			
			if (cmd.hasOption('r')) {
				rate = Float.parseFloat(cmd.getOptionValue('r'));
			} else if (cmd.hasOption("rate")) {
				rate = Float.parseFloat(cmd.getOptionValue("rate"));
			}
			
			if (cmd.hasOption('f')) {
				filename = cmd.getOptionValue('f');
			} else if (cmd.hasOption("file")) {
				filename = cmd.getOptionValue("file");
			}
			
			if (cmd.hasOption('t')) {
				tokenFilePath = cmd.getOptionValue('t');
			} else if (cmd.hasOption("token")) {
				tokenFilePath = cmd.getOptionValue("token");
			}
			
			if (filename != null) {
				generate(filename);
			} else {
				generate(num, rate);
			}
			
			System.exit(0);
			
		} catch (ParseException e) {
			usage();
			System.exit(-1);
		} 
	}
	
	public static void generate(int num, float rate) throws PulsarClientException {
		for (int idx = 0; idx < num; idx++) {
			getProducer().send(generator.generate());
		}
	}
	
	public static void generate(String filename) throws IOException {
		DatumReader<Order> orderDatumReader = new SpecificDatumReader<Order>(Order.class);
		DataFileReader<Order> dataFileReader = new DataFileReader<Order>(new File(filename), orderDatumReader);
		
		Order order = null;
		
		while (dataFileReader.hasNext()) {
			order = dataFileReader.next(order);
			getProducer().send(order);
		}
		
		dataFileReader.close();
	}

	/**
	 * "Definition" stage of command-line parsing with Apache Commons CLI.
	 * @return Definition of command-line options.
	 */
	private static Options generateOptions() {
	   final Option produceOption = Option.builder("n")
		  .longOpt("num-produce")
	      .hasArg()
	      .desc("The number of times to send the message(s).")
	      .build();
	   
	   final Option fileOption = Option.builder("f")
		  .longOpt("files")
		  .hasArg()
		  .desc("Comma-separated file paths to send")
		  .build();   
	   
	   final Option tokenOption = Option.builder("t")
		  .longOpt("token")
		  .hasArg()
		  .desc("Full pathname of JWT token file to use for authentication")
		  .build();   
	   
	   final Option rateOption = Option.builder("r")
		  .longOpt("rate")
		  .hasArg()
		  .desc("Rate (in messages per second) at which to produce")
		  .build();
	   
	   final Option props = Option.builder("D").hasArgs()
          .valueSeparator('=')
          .build();
	   
	   final Options options = new Options();
	   options.addOption(produceOption);
	   options.addOption(fileOption);
	   options.addOption(rateOption);
	   options.addOption(tokenOption);
	   options.addOption(props);
	   return options;
	}
	
	private static final void usage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("schema-tool <command>", generateOptions(), true);
	}
	
	private static Producer<Order> getProducer() throws PulsarClientException {
		if (producer == null) {
			producer =
				getPulsarClient()
					.newProducer(AvroSchema.of(Order.class))
				    .topic(topicName)
				    .create();
		}
		
		return producer;
	}

	private static PulsarClient getPulsarClient() throws PulsarClientException {
	
		if (client == null) {
			client = PulsarClient.builder()
					.serviceUrl("pulsar://localhost:6650")
					.authentication(
					        AuthenticationFactory.token(() -> {
								try {
									return new String(Files.readAllBytes(Paths.get(tokenFilePath)), Charsets.UTF_8).trim();
								} catch (IOException e) {
									return "";
								}
							}))
					
					.build();
		}
		return client;
	}

}

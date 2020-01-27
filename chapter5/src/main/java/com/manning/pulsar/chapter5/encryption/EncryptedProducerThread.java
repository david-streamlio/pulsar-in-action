package com.manning.pulsar.chapter5.encryption;

import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.ProducerCryptoFailureAction;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;

public class EncryptedProducerThread extends Thread {

	final CryptoKeyReader cryptoReader;
	final PulsarClient client;
	final String topicName;
	final String publicKeyPath;
	boolean stopped = false;
	
	public EncryptedProducerThread(CryptoKeyReader crypto, PulsarClient client, String topic, String publicKeyPath) {
		this.cryptoReader = crypto;
		this.client = client;
		this.topicName = topic;
		this.publicKeyPath = publicKeyPath;
	}
	
	public void run() {
		
		int msgCount = 0;
		Producer<String> producer;
		
		try {
			producer = client
					.newProducer(Schema.STRING)
					.cryptoKeyReader(cryptoReader)
					.cryptoFailureAction(ProducerCryptoFailureAction.FAIL)
					.addEncryptionKey(publicKeyPath)
					.topic(topicName)
					.create();
			
			do {
				producer.send("my-message-" + msgCount++);
				
				if ((msgCount % 100) == 0) {
					// Pause 0.1 second after 100 messages
					Thread.sleep(100);
				}
				
			} while (msgCount < 99999 && !stopped);
			
			producer.close();
			
		} catch (PulsarClientException | InterruptedException e) {
			// TODO Auto-generated catch block
			stopped = true;
			e.printStackTrace();
		}

	}
	
	public void halt() {
		stopped = true;
	}
}

package com.manning.pulsar.chapter3.encryption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.EncryptionKeyInfo;

public class RawFileKeyReader implements CryptoKeyReader {

	private static final long serialVersionUID = 1L;
	
    EncryptionKeyInfo publicKeyInfo;
    EncryptionKeyInfo privateKeyInfo;

    RawFileKeyReader(String pubKeyFile, String privKeyFile) {
        
        if (pubKeyFile != null) {
        	publicKeyInfo = new EncryptionKeyInfo();
        	try {
        		publicKeyInfo.setKey(Files.readAllBytes(Paths.get(pubKeyFile)));
            } catch (IOException e) {
                System.out.println("ERROR: Failed to read public key from file " + pubKeyFile);
                e.printStackTrace();
            }
        }
        
        if (privKeyFile != null) {
        	privateKeyInfo = new EncryptionKeyInfo();
        	try {
        		privateKeyInfo.setKey(Files.readAllBytes(Paths.get(privKeyFile)));
            } catch (IOException e) {
                System.out.println("ERROR: Failed to read public key from file " + privKeyFile);
                e.printStackTrace();
            }
        }
    }

    @Override
    public EncryptionKeyInfo getPublicKey(String keyName, Map<String, String> keyMeta) {
        return publicKeyInfo;
    }

    @Override
    public EncryptionKeyInfo getPrivateKey(String keyName, Map<String, String> keyMeta) {
        return privateKeyInfo;
    }
}

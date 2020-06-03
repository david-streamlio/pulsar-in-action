package com.manning.pulsar.chapter5.vault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;

public class VaultClient {
	
	private static final String VAULT_TOKEN = "myroot";
	private static final String PK_SECRET = "secret/data/encryption";
	
	private static Vault vault;

	public static void main(String[] args) throws VaultException, NoSuchAlgorithmException, IOException {
		
		/* TODO Move this to its own class, compile it as an executable jar, 
		 *      and then run it from the Docker script to write all the info to
		 *      
		 *      /secret/data/pki/TrustCerts
		 *      /secret/data/pki/TlsCert
		 *      /secret/data/pki/TlsKey
		 *      
		 *      # Option 1: Pass all the data in at init time. Client uses it to connect
		 *      /secret/data/AppRoles/<ROLE>/init/{"role_id": , "secret_id", "
		 *      
		 *      # Option 2: Place data in secrets at known locations, and grant access via policy
		 *      /secret/data/AppRoles/<ROLE>/tls/TrustCerts  <---
		 *      /secret/data/AppRoles/<ROLE>/authen
		 *      
		 *      Option 3: Pass AppRole authentication data ONLY at init time, use 
		 *      known locations for the rest.
		 *      
		 *      create policies? 
		 *      
		 *      write a file containing the role_id and secret_id for the AppRole
		 *      to a know location on the docker image that the client can access
		 *      as startup  (See configure-vault.sh)
		 * 
		 */
		publishKeyPair("/Users/david/exchange/ecdsa_pubkey.pem", "/Users/david/exchange/ecdsa_privkey.pem");
		
		PulsarClient client = PulsarClient.builder()
			    .serviceUrl("pulsar+ssl://localhost:6651/")
			    .tlsTrustCertsFilePath("/Users/david/exchange/ca.cert.pem")
			    .enableTlsHostnameVerification(false) // false by default, in any case
			    .allowTlsInsecureConnection(false) // false by default, in any case
			    .build();

		Producer<byte[]> producer = 
				client.newProducer()
				        .topic("persistent://orders/inbound/encrypted-topic")
				        .create();
		
		producer.send("Hello".getBytes());
		System.exit(0);
	}
	
	private static final Vault getVault() throws VaultException {
		if (vault == null) {
			final VaultConfig config = new VaultConfig()
	                .address("http://127.0.0.1:8200")
	                .token(VAULT_TOKEN)
	                .build();
			
			vault = new Vault(config);
		}
		return vault;
	}
	
	private static void publishKeyPair(String pubKeyPath, String privKeyPath) throws IOException, VaultException {

		// Public Key
		byte[] pub = Files.readAllBytes(Paths.get(pubKeyPath));
		
		Map<String, Object> secrets = new HashMap<String, Object>();
		secrets.put("key", new String(pub));
		secrets.put("format", "pem");
		secrets.put("algorithm", "RSA");
		
		getVault().logical().write(PK_SECRET + "/public", secrets);
		
		// Private Key
		byte[] pri = Files.readAllBytes(Paths.get(privKeyPath));
		
		secrets = new HashMap<String, Object>();
		secrets.put("key", new String(pri));
		secrets.put("format", "pem");
		secrets.put("algorithm", "RSA");
		
		getVault().logical().write(PK_SECRET + "/private", secrets);
	}
	
}

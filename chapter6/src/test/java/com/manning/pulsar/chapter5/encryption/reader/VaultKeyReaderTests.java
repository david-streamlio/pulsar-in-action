package com.manning.pulsar.chapter5.encryption.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.pulsar.client.api.EncryptionKeyInfo;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.manning.pulsar.chapter6.encryption.reader.VaultKeyReader;

import org.junit.*;

public class VaultKeyReaderTests {

	private static final String VAULT_TOKEN = "myroot";
	private static final String PK_SECRET = "secret/data/encryption";
	
	private static Vault vault;
	private static AuthResponse auth;
	

	@BeforeClass
	public static final void setUp() throws NoSuchAlgorithmException, IOException, VaultException {
		publishKeyPair("/Users/david/exchange/ecdsa_pubkey.pem", "/Users/david/exchange/ecdsa_privkey.pem");
	}
	
	@AfterClass
	public static final void tearDown() throws VaultException {
//		deleteKeyPair();
	}

	@Test
	public final void simpleTest() throws VaultException {
		VaultKeyReader keyReader = new VaultKeyReader("http://127.0.0.1:8200", getAuth(),
				PK_SECRET + "/private");
		
		assertNotNull(keyReader);
		
		EncryptionKeyInfo info = keyReader.getPublicKey(PK_SECRET + "/public", null);
		assertNotNull(info);
		
		info = keyReader.getPrivateKey(PK_SECRET + "/private", null);
		assertNotNull(info);
		
	}
	
	@Test
	public final void badAuthTest() throws VaultException {
		
		VaultException exception = assertThrows(
				VaultException.class, 
			    () -> { new VaultKeyReader("http://127.0.0.1:8200", 
						getVault().auth().loginByUserPass("bad user", "bad password"),
						PK_SECRET + "/private"); }
			  );

		assertEquals("Vault responded with HTTP status code: 400\n" + 
		               "Response body: 400 Bad Request", exception.getMessage());
		
	}
	
	@Test
	public final void expiredCredentialsTest() throws VaultException {
		VaultKeyReader keyReader = new VaultKeyReader("http://127.0.0.1:8200", getAuth(),
				PK_SECRET + "/private");
		
		// Verify that we can retrieve the key with the given token
		EncryptionKeyInfo info = keyReader.getPublicKey(PK_SECRET + "/public", null);
		assertNotNull(info);
		
		// Wait for the token to expire
		try {
			Thread.sleep(61 * 1000);
			
			// Try to get the key with an expired token
			info = keyReader.getPublicKey(PK_SECRET + "/public", null);
			assertNotNull(info);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	
	private static final AuthResponse getAuth() throws VaultException {				
		auth = getVault().auth().loginByAppRole(
				"e663ccec-7b82-fe21-5c73-addb087f0397", // Role ID
				"0e79c4f4-6d40-230d-3ec6-386699b54e88"); // Secret ID
	
		return auth;
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
	
	private static void deleteKeyPair() throws VaultException {
		getVault().logical().delete(PK_SECRET + "/public");
		getVault().logical().delete(PK_SECRET + "/private");
	}
}

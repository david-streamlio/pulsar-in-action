package com.manning.pulsar.chapter6.encryption.reader;

import java.util.Map;

import org.apache.pulsar.client.api.CryptoKeyReader;
import org.apache.pulsar.client.api.EncryptionKeyInfo;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.bettercloud.vault.response.LogicalResponse;

public class VaultKeyReader implements CryptoKeyReader {

	private static final long serialVersionUID = 1L;
	
	private String vaultAddress;
	private AuthResponse vaultAuth;
	private String privKeyPath;
	private final Vault vault;
	
	public VaultKeyReader(String addr, AuthResponse authentication, String privKeyPath) throws VaultException {
		
		this.vaultAddress = addr;
		this.vaultAuth = authentication;
		this.privKeyPath = privKeyPath;
		
		final VaultConfig config = new VaultConfig()
                .address(vaultAddress)
                .token(vaultAuth.getAuthClientToken())
                .build();
		
		vault = new Vault(config);
	}
	
	@Override
	public EncryptionKeyInfo getPrivateKey(String vaultKeyPath, Map<String, String> keyMeta) {
		return getKey(privKeyPath, keyMeta);
	}

	@Override
	public EncryptionKeyInfo getPublicKey(String vaultKeyPath, Map<String, String> keyMeta) {
		return getKey(vaultKeyPath, keyMeta);
	}
	
	private EncryptionKeyInfo getKey(String vaultKeyPath, Map<String, String> keyMeta) {
		String keyLookupKey = "contents";
		
		if (keyMeta != null && !keyMeta.isEmpty() && keyMeta.containsKey("keyMeta") ) {
			keyLookupKey = keyMeta.get(("keyMeta"));
		}
		
		try {
			EncryptionKeyInfo keyInfo = new EncryptionKeyInfo(); 
			keyInfo.setMetadata(keyMeta);
			
			LogicalResponse readResponse = vault.withRetries(5, 1000).logical()
					.read(vaultKeyPath);
			
			if (readResponse.getRestResponse().getStatus() == 403) {
				System.out.println(new String(readResponse.getRestResponse().getBody()));
				renewLease();
				
				// Try again with refreshed token
				readResponse = vault.withRetries(5, 1000).logical().read(vaultKeyPath);
			}
			
			if (readResponse.getData() == null || !readResponse.getData().containsKey(keyLookupKey)) {
				throw new RuntimeException("Unable to locate the key in Vault");
			}
			
			keyInfo.setKey(readResponse
							.getData()
							.get(keyLookupKey)
							.getBytes());
			
			return keyInfo;
			
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	private void renewLease() throws VaultException {
		if (vaultAuth != null && vaultAuth.isAuthRenewable()) {
			vaultAuth = vault.auth().renewSelf();
		} 
	}

}

package com.manning.pulsar.chapter6.vault;

import java.util.ArrayList;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.PulsarClientException.UnsupportedAuthenticationException;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.pki.Credential;
import com.bettercloud.vault.api.pki.CredentialFormat;
import com.bettercloud.vault.api.pki.Pki;
import com.bettercloud.vault.api.pki.RoleOptions;
import com.bettercloud.vault.response.PkiResponse;

public class VaultPki {

	private static final String VAULT_TOKEN = "myroot";
	private static Pki pki;
	
	public static final void main(String[] args) throws VaultException, UnsupportedAuthenticationException, PulsarClientException {
//		createRole("pulsar-client");
		Credential creds = credentials("producer");
		System.out.println(creds);
		
		PulsarClient client = PulsarClient.builder()
			    .serviceUrl("pulsar+ssl://localhost:6651/")
			    .tlsTrustCertsFilePath("/Users/david/exchange/ca.cert.pem")
			    .authentication("org.apache.pulsar.client.impl.auth.AuthenticationTls",
	                    "tlsCertFile:/Users/david/exchange/admin.cert.pem,tlsKeyFile:/Users/david/exchange/admin-pk8.pem")
			    .enableTlsHostnameVerification(false) // false by default, in any case
			    .allowTlsInsecureConnection(false) // false by default, in any case
			    .build();

		Producer<byte[]> producer = 
				client.newProducer()
				        .topic("persistent://public/default/test-topic")
				        .create();
		
		for (int idx = 0; idx < 100; idx++) {
			producer.send("Hello".getBytes());
		}
	}
	
	private static final Pki getPki() throws VaultException {
		if (pki == null) {
	        VaultConfig config = new VaultConfig().address("http://127.0.0.1:8200").token(VAULT_TOKEN).build();
	        Vault vault = new Vault(config);
	        pki = vault.pki();
		}
		return pki;
	}
	
	// This is an activity that needs to be done once or twice.
	private static void createRole(String role) throws VaultException {
		
		final RoleOptions options = new RoleOptions()
                .allowedDomains(new ArrayList<String>(){{ add("gottaeat.com"); }})
                .allowSubdomains(true)
                .maxTtl("72h");
		
		PkiResponse response = getPki().createOrUpdateRole(role, options);
		response.getRoleOptions();
	}
	
	// Request my own certificate and Private Key
	public static Credential credentials(final String application) throws VaultException {

		 PkiResponse resp = 
				 getPki()
				   .issue("pulsar-client",  // Role Name
				       "admin", // CN <--- This maps to a role in Pulsar
				       null, 
				       null, null, 
				       CredentialFormat.PEM);
		 
		return resp.getCredential();
				
	}
}

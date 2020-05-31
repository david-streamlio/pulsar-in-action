package com.manning.pulsar.chapter6.encryption;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.shade.com.google.gson.JsonObject;
import org.apache.pulsar.shade.com.google.gson.JsonParser;
import org.apache.pulsar.shade.org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.pulsar.shade.org.apache.http.client.methods.HttpGet;
import org.apache.pulsar.shade.org.apache.http.client.methods.HttpPost;
import org.apache.pulsar.shade.org.apache.http.impl.client.CloseableHttpClient;
import org.apache.pulsar.shade.org.apache.http.impl.client.HttpClients;
import org.apache.pulsar.shade.org.apache.http.util.EntityUtils;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.response.AuthResponse;
import com.bettercloud.vault.response.LogicalResponse;
import com.manning.pulsar.chapter6.encryption.reader.VaultKeyReader;


public class EncryptedDemo {
	
	private static final String SECRET_PATH = "secret/data/encryption";
	
	private static PulsarClient client;
	private static String vaultAddress = "http://127.0.0.1:8200";
	private static String topicName = "persistent://orders/inbound/encrypted-topic";
	private static EncryptedProducerThread producer;
	private static EncryptedConsumerThread consumer;
	
	private static String wrapToken;
	private static String roleId;
	private static String secretId;
	private static String trustCertsSecret;
	
	// encryption key locations, tlsTrustFile location, authentication data (JWT, TLS cert, etc)
	

	public static void main(String[] args) throws PulsarClientException, VaultException {
	
		CommandLineParser parser = new DefaultParser();
		boolean decrypt = false;
		
		try {
			CommandLine cmd = parser.parse(generateOptions(), args);
			vaultAddress = cmd.getArgs() [0];
			topicName = cmd.getArgs()[1];
			
			if (cmd.hasOption('d')) {
				decrypt = true;
			} else if (cmd.hasOption("decrypt")) {
				decrypt = true;
			}
			
			if (cmd.hasOption('t')) {
				wrapToken = cmd.getOptionValue('t');
			} else if (cmd.hasOption("token")) {
				wrapToken = cmd.getOptionValue("token");
			}
			
			if (wrapToken != null) {
				try {
					unwrap(wrapToken);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// Get the role and session ids we will need to authenticate
				bootstrap();
			}
			
			producer = new EncryptedProducerThread(
					new VaultKeyReader(vaultAddress, getAuth(), 
							SECRET_PATH + "/private"),
					getPulsarClient(), topicName, SECRET_PATH + "/public");
			
			producer.start();
			
			if (decrypt) {
				consumer = new EncryptedConsumerThread(
						    new VaultKeyReader(vaultAddress, getAuth(), 
									SECRET_PATH + "/private"),
							getPulsarClient(), topicName);
			} else {
				// Create a consumer that cannot decrypt the messages
				consumer = new EncryptedConsumerThread(null, getPulsarClient(), topicName);
			}
			
			consumer.start();
			
			Thread.sleep(3000 * 1000L);
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
	
	private static final void bootstrap() throws VaultException {
		/* Log in using user credentials, such as user/pass, etc.
		 * Any of the Authentication methods supported by Vault are
		 * an option, including: 
		 * 
		 *    AWS, Azure, Google Cloud, LDAP, K8s, Okta, etc.
		 *    
		 * This allows you to implement this pattern on any environment
		 * that you choose.
		 */
		final VaultConfig authConfig = new VaultConfig().address(vaultAddress).build();
        final Vault authVault = new Vault(authConfig);
        final AuthResponse authResp = authVault.auth().loginByUserPass("vault_user", "vault_pass");
        final String token = authResp.getAuthClientToken();

        // Use our "user" token to connect to the Vault
        VaultConfig config = new VaultConfig().address(vaultAddress)
        		.token(token)
        		.build();
        
        Vault vault = new Vault(config);
     
        /* Read the initialization data stored in the vault in a known
         * location that is protected by a policy that permits us to
         * access this information.
         */
        Map<String, String> initializationSecrets = 
        		vault.logical()
        		.read("secret/app/encrypted-demo/init")
        		.getData();
        
        // Get the credentials for logging in as the "message-producer" role.
        roleId = initializationSecrets.get("role_id");
        secretId = initializationSecrets.get("secret_id");
        trustCertsSecret = initializationSecrets.get("trust_cert_secret");
        
        if (trustCertsSecret != null) {
       
           try {
        	   writeSecretContentsToFile(vault, trustCertsSecret, "/tmp/certs/ca.cert.pem");
           } catch (IOException e) {
        	   // TODO Auto-generated catch block
        	   e.printStackTrace();
           }
        }
	}
	
	private static final void unwrap(String wrapToken) throws VaultException, IOException {
		String json = null;
        HttpPost post = new HttpPost(vaultAddress + "/v1/sys/wrapping/unwrap");
        post.addHeader("X-Vault-Token", wrapToken);
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(post)) {
        	json = EntityUtils.toString(response.getEntity());
            System.out.println(json);
        }
        
        if (json != null) {
        	JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        	JsonObject auth = jsonObject.getAsJsonObject("auth");
        	String client_token = auth.get("client_token").getAsString();
        	
        	HttpGet get = new HttpGet(vaultAddress + "/v1/secret/data/app/encrypted-demo/init");
        	get.addHeader("X-Vault-Token", client_token);
        	
        	try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(get)) {
             	json = EntityUtils.toString(response.getEntity());
                System.out.println(json);
            }
        	
        	JsonObject data = jsonObject.getAsJsonObject("data");
        	JsonObject nestedData = data.getAsJsonObject("data");
        	roleId = nestedData.get("role_id").getAsString();
        	secretId = nestedData.get("secret_id").getAsString();
        	trustCertsSecret = nestedData.get("trust_cert_secret").getAsString();
        }
	}
	
	private static PulsarClient getPulsarClient() throws PulsarClientException {
		
		if (client == null) {
			ClientBuilder builder = PulsarClient.builder()
				    .serviceUrl("pulsar+ssl://localhost:6651/");
				    
			if (trustCertsSecret != null) {
				builder = builder.tlsTrustCertsFilePath("/tmp/certs/ca.cert.pem")
					    .enableTlsHostnameVerification(false) // false by default, in any case
					    .allowTlsInsecureConnection(false);
			}
				    
			client = builder.build();
		}
		return client;
	}
	
	private static final AuthResponse getAuth() throws VaultException {	
		final VaultConfig authConfig = new VaultConfig().address(vaultAddress).build();
        final Vault authVault = new Vault(authConfig);
		return authVault.auth().loginByAppRole(roleId, secretId); 
	}
	
	private static final void writeSecretContentsToFile(Vault vault, String vaultPath, String filePath) throws VaultException, IOException {
	   LogicalResponse resp = vault.logical().read(vaultPath);
	   String contents = resp.getData().get("contents");
 	   if (contents != null) {
 		   Files.write(Paths.get(filePath), contents.getBytes());
 	   } else {
 		   throw new RuntimeException("Secret contents were null");
 	   }
	}
	
	
	/**
	 * "Definition" stage of command-line parsing with Apache Commons CLI.
	 * @return Definition of command-line options.
	 */
	private static Options generateOptions() {
		final Option tokenOption = Option.builder("t")
				.longOpt("token")
				.hasArg()
				.desc("Wrap token to use to unwrap the init data inside vault")
				.build();  

		final Option decryptOption = Option.builder("d")
				.longOpt("decrypt")
				.desc("Create a consumer that can decrypt the messages")
				.build();  
		
		final Options options = new Options();
		options.addOption(tokenOption);
		options.addOption(decryptOption);
		return options;
	}
	
	private static final void usage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("EncryptedDemo <topic name>", generateOptions(), true);
	}

}

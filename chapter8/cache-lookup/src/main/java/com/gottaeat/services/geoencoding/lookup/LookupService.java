package com.gottaeat.services.geoencoding.lookup;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.pulsar.client.impl.schema.AvroSchema;
import org.apache.pulsar.functions.api.Context;
import org.apache.pulsar.functions.api.Function;

import com.gottaeat.domain.common.Address;

import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;

public class LookupService implements Function<Address, Address> {
	
	private TimeLimiter timeLimiter;
	private IgniteCache<Address, Address> cache;
	private boolean initalized = false;
	private String bypassTopic;

	@Override
	public Address process(Address addr, Context ctx) throws Exception {
		
		if (!initalized) {
			init(ctx);
		}
		
		Address geoEncodedAddr = timeLimiter.executeFutureSupplier(
			() -> CompletableFuture.supplyAsync(() -> 
			  { return cache.get(addr); }
			));
		
		if (geoEncodedAddr != null) {
			ctx.newOutputMessage(bypassTopic, AvroSchema.of(Address.class))
			.properties(ctx.getCurrentRecord().getProperties())
			.value(geoEncodedAddr)
			.send();
		}
		
		return addr;
	}

	private void init(Context ctx) {
		bypassTopic = ctx.getUserConfigValue("bypassTopic").get().toString();
		
		TimeLimiterConfig config = TimeLimiterConfig.custom()
				   .cancelRunningFuture(true)
				   .timeoutDuration(Duration.ofMillis(500))
				   .build();
		
		TimeLimiterRegistry registry = TimeLimiterRegistry.of(config);
		timeLimiter = registry.timeLimiter("my-time-limiter");
		
		IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setPeerClassLoadingEnabled(true);

        // Setting up an IP Finder to ensure the client can locate the servers.
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        ipFinder.setAddresses(Collections.singletonList("127.0.0.1:47500..47509"));
        cfg.setDiscoverySpi(new TcpDiscoverySpi().setIpFinder(ipFinder));

        // Starting the node
        Ignite ignite = Ignition.start(cfg);
        cache = ignite.getOrCreateCache("myCache");
	}
}

package com.manning.pulsar.chapter3.admin;

import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.pulsar.client.admin.PulsarAdmin;
import org.apache.pulsar.common.policies.data.TenantInfo;

public class CreateTopic {

	public static void main(String[] args) throws Exception {
		PulsarAdmin admin = PulsarAdmin.builder()
				.serviceHttpUrl("http://localhost:8080")
				.build();
		
		System.out.println("List of clusters");
		admin.clusters().getClusters().forEach(s -> {
			System.out.println(s);
		});
		
		System.out.println("List of tenants");
		admin.tenants().getTenants().forEach(ten -> {
			System.out.println(ten);
		});
		
		TenantInfo config = new TenantInfo(
			Stream.of("admin").collect(Collectors.toCollection(HashSet::new)),
			Stream.of("standalone").collect(Collectors.toCollection(HashSet::new)));
		
		admin.tenants().createTenant("manning", config );
		
		System.out.println("List of tenants");
		admin.tenants().getTenants().forEach(ten -> {
			System.out.println(ten);
		});
		
		admin.namespaces().createNamespace("manning/chapter03");
		
		System.out.println("List of namespaces under manning tenant");
		admin.namespaces().getNamespaces("manning").forEach(s -> {
			System.out.println(s);
		});
		
		admin.topics().createNonPartitionedTopic("persistent://manning/chapter03/example-topic");
		
		System.out.println("List of topics in the manning/chapter03 namespace");
		admin.topics().getList("manning/chapter03").forEach(s -> {
			System.out.println(s);
		});
	}
}

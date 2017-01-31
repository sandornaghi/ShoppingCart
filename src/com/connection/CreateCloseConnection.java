package com.connection;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class CreateCloseConnection {

	@Produces
	private TransportClient createConnection() {
		TransportClient client = null;
		try {
			client = TransportClient.builder().build()
					        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return client;
	}
	
	@SuppressWarnings("unused")
	private void closeConnection(@Disposes TransportClient client) {
		if (client != null) {
			client.close();
		}
	}
}

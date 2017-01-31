///**
// *  This class is used to make Client login.
// *  
// *  @author sandor.naghi
// */
//
//package com.dao;
//
//import javax.inject.Inject;
//
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.transport.TransportClient;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//
//import com.beans.Client;
//import com.encrypt.EncryptPassword;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//
//public class LoginDao {
//
//	/**
//	 * Get the user from the DB upon the username, and password.
//	 * @param username	Username of the Client.
//	 * @param pass		Password of the Client.
//	 * @return	The Client object if it exists, or null if not.
//	 */
//	
//	@Inject
//	TransportClient transportClient;
//	
//	
//	public Client getClientWithUsername(String username, String pass) {
//		
//		Client client = null;
//		
//		EncryptPassword encrypt = new EncryptPassword();
//		
//		String password = encrypt.encryptpasswordMD5(pass);
//		
//		try {
//			SearchResponse response = transportClient.prepareSearch("shoppingcart")
//					.setTypes("client")
//					.setQuery(QueryBuilders.termQuery("username", username))
//					.execute().actionGet();
//			
//			SearchHit[] hit = response.getHits().getHits();
//			if (hit.length != 0) {
//				String s = hit[0].getSourceAsString();
//				
//				ObjectMapper mapper = new ObjectMapper();
//				client = mapper.readValue(s, Client.class);
//				client.setId(hit[0].getId());
//				
//				if (!password.equals(client.getPassword())) {
//					return null;
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			}
//		
//		return client;
//	}
//	
//}

/**
 *  This class is used to make Client login.
 *  
 *  @author sandor.naghi
 */

package com.dao;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.beans.Client;
import com.encrypt.EncryptPassword;

public class LoginDao {

	/**
	 * Get the user from the DB based upon the username, and password.
	 * @param transportClient	Connection to the DB.
	 * @param username	Username of the Client.
	 * @param pass		Password of the Client.
	 * @return	The Client object if it exists, or null if not.
	 */
	
	public Client getClientWithUsername(TransportClient transportClient, String username, String pass) {
		Client client = null;
		
		EncryptPassword encrypt = new EncryptPassword();
		
		String password = encrypt.encryptpasswordMD5(pass);
		
		try {
			SearchResponse response = transportClient.prepareSearch("shoppingcart")
					.setTypes("client")
					.setQuery(QueryBuilders.termQuery("username", username))
					.execute().actionGet();
			
			
			SearchHit[] hit = response.getHits().getHits();
			if (hit.length != 0) {
				String s = hit[0].getSourceAsString();
				
				ObjectMapper mapper = new ObjectMapper();
				client = mapper.readValue(s, Client.class);
				client.setId(hit[0].getId());
				
				if (!password.equals(client.getPassword())) {
					return null;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			}
		
		return client;
	}
	
}

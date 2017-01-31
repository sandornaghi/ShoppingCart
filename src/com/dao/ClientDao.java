/**
 * This class is used to make Client verifications, inserts, and queries in the Elasticsearch DB.
 * 
 * @author sandor.naghi
 */

package com.dao;


import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import com.beans.Client;
import com.encrypt.EncryptPassword;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.service.MailService;

public class ClientDao {
	
	/**
	 * Validate the clients data: email address, password and username.
	 * @param client	Client that will be validated.
	 * @return	true if the data is valid, and false if at least one of the data is not correct.
	 */
	public boolean validateClient(Client client) {
		if (isValidEmail(client.getEmail()) && !client.getPassword().equals("") && !client.getEmail().equals("") && !client.getUsername().equals("")) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Validate the email address of the Client.
	 * @param email	The email address.
	 * @return	true if it is a valid email address, false if it's not valid.
	 */
	private boolean isValidEmail(String email) {
		
		Pattern pattern  = Pattern.compile("[a-zA-Z0-9_.]*@[a-zA-Z]*.[a-zA-Z]*");
		Matcher matcher = pattern.matcher(email);
		boolean result = matcher.matches();
		
		return result;
	}
	
	/**
	 * Insert the Clients data in the DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param client	Client need to be inserted.
	 * @return	The clients id if the insertion is success, null if it heas'nt.
	 */
	public String createClient(TransportClient transportClient, Client client){
		// create a hash(md5) for the password, that will be saved in DB
		EncryptPassword encrypt = new EncryptPassword();
		client.setPassword(encrypt.encryptpasswordMD5(client.getPassword()));
		
		String id = null;
		
		IndexResponse response = transportClient.prepareIndex("shoppingcart", "client")
				.setSource(new Gson().toJson(client))
				.get();
		
		id = response.getId();
			
		return id;
	}
	
	/**
	 * Get the client from the DB identified by his id.
	 * @param transportClient	The connection to the DataBase.
	 * @param id	The id of the client.
	 * @return	The client if it exists, or null if it does'nt.
	 */
	public Client readClient(TransportClient transportClient, String id) {
		
		Client client = null;
		
		GetResponse response = transportClient.prepareGet("shoppingcart", "client", id).get();
		
		if (!response.isExists()) {			// if the user with the id don't exists, return null
			return null;
		} else {
			ObjectMapper mapper = new ObjectMapper();
			try {
				client = mapper.readValue(response.getSourceAsString(), Client.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (client != null) {
			client.setId(id);
		}
		
		return client;
	}
	
	/**
	 * Activate or disable a Client.
	 * @param transportClient	The connection to the DataBase.
	 * @param client	The Client needed to activate, or disabled.
	 * @param isactive	true if the Client is activated, or false if disabled.
	 */
	public void clientActivation(TransportClient transportClient, Client client, boolean isactive) {

		UpdateRequest updateRequest = new UpdateRequest("shoppingcart", "client", client.getId());
		
		try {
			updateRequest.doc(jsonBuilder().startObject().field("isactive", isactive).endObject());
			transportClient.update(updateRequest).get();
		} catch (IOException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Reset the password of the Client in DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param client	Client that needs resetting the password.
	 */
	public void resetPassword(TransportClient transportClient, Client client) {

		EncryptPassword encrypt = new EncryptPassword();
		client.setPassword(encrypt.encryptpasswordMD5(client.getPassword()));				// encrypt the password
		
		UpdateRequest updateRequest = new UpdateRequest("shoppingcart", "client", client.getId());
		
		try {
			updateRequest.doc(jsonBuilder().startObject().field("password", client.getPassword()).endObject());
			transportClient.update(updateRequest).get();
		} catch (IOException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * Check if a Client exists in the DB or not.
	 * @param transportClient	The connection to the DataBase.
	 * @param newClient	The Client that is checked.
	 * @return	true if the Client already exists, or false if it does'nt.
	 */
	public boolean clientExists(TransportClient transportClient, Client newClient) {

		Client client = null;

		SearchResponse response = transportClient.prepareSearch("shoppingcart")
				.setTypes("client")
				.setQuery(QueryBuilders.termQuery("username", newClient.getUsername()))
				.execute().actionGet();
		
		SearchHit[] hit = response.getHits().getHits();

		if (hit.length != 0) {
			String s = hit[0].getSourceAsString();
		
			ObjectMapper mapper = new ObjectMapper();
			try {
				client = mapper.readValue(s, Client.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (client != null) {
			return true;
		}
		
		return false;
	}

	/**
	 * Send a registration email to the Client with his username, activation code, and activation link.
	 * @param client	Client who will get the email.
	 */
	public void sendRegistrationEmail(Client client) {
		MailService mail = new MailService();

		String subject = "Dear " + client.getName() 
					+ "\nYou been registered to Shopping Cart."
					+ "\nYour username is " + client.getUsername() + "."
					+ "\nThe activation code is: " + client.getActivationcode()
					+ "\nThe activation link is: " + "http://localhost:8080/ShoppingCart/user/" + client.getId() + "/activate/" + client.getActivationcode();
		
		mail.sendMail(client.getEmail(), subject);
		//mail.sendMailSendGrid(client.getEmail(), subject);
	}
	
	/**
	 * Send an email with the new password for the Client.
	 * @param emailAddress	Email address of the Client.
	 * @param subject	New password of Client.
	 */
	public void sendResetPasswordEmail(String emailAddress, String subject) {
		MailService mail = new MailService();
		
		mail.sendMail(emailAddress, subject);
		//mail.sendMailSendGrid(emailAddress, subject);
	}
	
	/**
	 * Get a list of all Clients in the application, active or not.
	 * @param transportClient	The connection to the DataBase.
	 * @return	The list of Clients.
	 */
	public List<String> getUsersList(TransportClient transportClient) {
		List<String> list = new ArrayList<>();
		
		SearchResponse response = transportClient.prepareSearch("shoppingcart")
				.setTypes("client")
				.setQuery(QueryBuilders.matchAllQuery())
				.execute().actionGet();
		
		SearchHit[] hits = response.getHits().getHits();
		if (hits.length != 0) {
			for (SearchHit hit : hits) {
				ObjectMapper mapper = new ObjectMapper();
				Client client = null;
				try {
					client = mapper.readValue(hit.getSourceAsString(), Client.class);
				} catch (IOException e) {
					e.printStackTrace();
				}
				client.setId(hit.getId());
				String userInfo = "{\"username\":\"" + client.getUsername() + "\",\n\"email\":\"" + client.getEmail() + "\",\n\"id\":\"" + client.getId() + "\",\n\"isactive\":\"" + client.isIsactive() + "\"}";
				list.add(userInfo);
			}
		}
			
		return list;
	}
}

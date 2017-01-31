/**
 * This class is used to communicate with the front end of the application, through Json string objects.
 * It is used on the login.
 * The Path of the Class is "user/login"
 * 
 * @author sandor.naghi
 */
package com.service;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.elasticsearch.client.transport.TransportClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.annotations.NotEmpty;
import com.beans.Client;
import com.dao.LoginDao;
import com.encrypt.CodeDecodeTokens;
import com.encrypt.MessageCreator;

@Path("/user/login")
public class LoginService {

	private LoginDao loginDao = new LoginDao();
	private MessageCreator mc = new MessageCreator();
	
	@Inject
	private TransportClient transportClient;
	
	/**
	 * Identifying the Client, based upon username and password.
	 * @param input	Data from the front end, with username and password.
	 * @return	Message of Success or Failure.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String login(@NotEmpty String input) {
		String result = null;
		
		// Creating a JSon object, from which retrieve the login information.
		JSONObject jsonRequest = null;
		String username = null;
		String password = null;
		try {
			jsonRequest = new JSONObject(input);
			username = jsonRequest.getString("username");
			password = jsonRequest.getString("password");
		} catch (JSONException e) {
			result = mc.setMessage("Failed", "Invalid data.");
			return result;
		}
		
		// Identifying the Client.
		Client client = loginDao.getClientWithUsername(transportClient, username, password);
		
		if (client != null && client.isIsactive()) {
			
			// generate the token from the userid, username, and password
			CodeDecodeTokens cdt = new CodeDecodeTokens();
			String token = cdt.generateToken(client.getId(), client.getUsername(), client.getPassword());
			
			result = mc.setMessage("Success", token);
			return result;
		}
		
		result = mc.setMessage("Failed", "Bad username, password, or user inactive.");
		return result;
	}
}

/**
 * This class is one of the Bean classes of the application.
 * It contains the field of the client:
 * 		email, name, username, password, address	- information about the client, 
 * 		the activation code		- for activating the client,
 * 		isactive boolean field		- to let us know if the client is active
 * 		isadmin boolean field		- to let us know if the client has administrator rights.
 * 
 * 	@author sandor.naghi
 */

package com.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Client {
	
	private String id;
	
	@JsonProperty("email")
	private String email;
	
	@JsonProperty("username")
	private String username;
	
	@JsonProperty("password")
	private String password;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("isactive")
	private boolean isactive;
	
	@JsonProperty("isadmin")
	private boolean isadmin;
	
	@JsonProperty("activationcode")
	private String activationcode;
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	private Address address;

	/**
	 * Default constructor.
	 */
	public Client() {
	}

	/**
	 * Constructor with arguments of the class.
	 * @param email	Email address of the client.
	 * @param username	Username of the client, used to log in.
	 * @param password	Password of the client.
	 * @param name	Name of the client.
	 * @param isactive	Used to activate or disabled a client.
	 * @param isadmin	Information about the rights of the client.
	 * @param activationcode	Used to activate the client in the application.
	 * @param address	Address of the client.
	 */
	public Client(String email, String username, String password, String name,
			boolean isactive, boolean isadmin, String activationcode, Address address) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.name = name;
		this.isactive = isactive;
		this.isadmin = isadmin;
		this.activationcode = activationcode;
		this.address = address;
	}

	/**
	 * Public getters and setters.
	 */
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isIsactive() {
		return isactive;
	}

	public void setIsactive(boolean isactive) {
		this.isactive = isactive;
	}

	public boolean isIsadmin() {
		return isadmin;
	}

	public void setIsadmin(boolean isadmin) {
		this.isadmin = isadmin;
	}

	public String getActivationcode() {
		return activationcode;
	}

	public void setActivationcode(String activationcode) {
		this.activationcode = activationcode;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	/**
	 * The overridden hashcode() method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activationcode == null) ? 0 : activationcode.hashCode());
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isactive ? 1231 : 1237);
		result = prime * result + (isadmin ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	/**
	 * The overridden equals() method.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Client other = (Client) obj;
		if (activationcode == null) {
			if (other.activationcode != null)
				return false;
		} else if (!activationcode.equals(other.activationcode))
			return false;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isactive != other.isactive)
			return false;
		if (isadmin != other.isadmin)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
}

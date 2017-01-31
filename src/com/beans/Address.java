/**
 * This class is one of the Bean classes of the application.
 * It contains the fields of an address of a client
 * 	street, town, county and zipcode.
 * 
 * @author sandor.naghi
 */

package com.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {

	@JsonProperty("street")
	private String street;
	
	@JsonProperty("town")
	private String town;
	
	@JsonProperty("county")
	private String county;
	
	@JsonProperty("zip")
	private int zip;

	/**
	 * Default Constructor of the class.
	 */
	public Address() {
	}
	
	/**
	 * Constructor with arguments of the class.
	 * @param street	Street of the client.
	 * @param town		Town of the client.
	 * @param county	County of the client.
	 * @param zip		Zipcode of the client.
	 */
	public Address(String street, String town, String county, int zip) {
		this.street = street;
		this.town = town;
		this.county = county;
		this.zip = zip;
	}

	/**
	 * Public getters and setters.
	 */
	
	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getTown() {
		return town;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public int getZip() {
		return zip;
	}

	public void setZip(int zip) {
		this.zip = zip;
	}

	/**
	 * The overridden hashcode() method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((county == null) ? 0 : county.hashCode());
		result = prime * result + ((street == null) ? 0 : street.hashCode());
		result = prime * result + ((town == null) ? 0 : town.hashCode());
		result = prime * result + zip;
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
		Address other = (Address) obj;
		if (county == null) {
			if (other.county != null)
				return false;
		} else if (!county.equals(other.county))
			return false;
		if (street == null) {
			if (other.street != null)
				return false;
		} else if (!street.equals(other.street))
			return false;
		if (town == null) {
			if (other.town != null)
				return false;
		} else if (!town.equals(other.town))
			return false;
		if (zip != other.zip)
			return false;
		return true;
	}


}

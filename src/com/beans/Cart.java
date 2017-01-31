/**
 * This class contains information about the clients Cart.
 * 	userid			- The id of the Client;
 *  totalquantity	- The quantity of object inside the Cart.
 *  totalcost		- Total cost of items in the Cart.
 *  items			- List of id's and quantities of products in the Cart.
 *  
 *  @author sandor.naghi
 */

package com.beans;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Cart {

	@JsonProperty("userid")
	private String userid;
	
	@JsonProperty("totalquantity")
	private int totalquantity;
	
	@JsonProperty("totalcost")
	private long totalcost;
	
	@JsonProperty("items")
	private List<String> items;
	
	/**
	 * Default constructor.
	 */
	public Cart() {
	}

	/**
	 * Constructor of the class with arguments.
	 * @param totalquantity	- Total quantity of products in the Cart.
	 * @param totalcost	- Total cost of products in the Cart.
	 * @param items	- List of id's and quantities of products from the Cart.
	 */
	public Cart(int totalquantity, long totalcost, List<String> items) {
		this.totalquantity = totalquantity;
		this.totalcost = totalcost;
		this.items = items;
	}

	/**
	 * Public getters and setters.
	 */
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public int getTotalquantity() {
		return totalquantity;
	}

	public void setTotalquantity(int totalquantity) {
		this.totalquantity = totalquantity;
	}

	public long getTotalcost() {
		return totalcost;
	}

	public void setTotalcost(long totalcost) {
		this.totalcost = totalcost;
	}

	public List<String> getItems() {
		return items;
	}

	public void setItems(List<String> items) {
		this.items = items;
	}

	/**
	 * The overridden hashcode() method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + (int) (totalcost ^ (totalcost >>> 32));
		result = prime * result + totalquantity;
		result = prime * result + ((userid == null) ? 0 : userid.hashCode());
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
		Cart other = (Cart) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		if (totalcost != other.totalcost)
			return false;
		if (totalquantity != other.totalquantity)
			return false;
		if (userid == null) {
			if (other.userid != null)
				return false;
		} else if (!userid.equals(other.userid))
			return false;
		return true;
	}	
}

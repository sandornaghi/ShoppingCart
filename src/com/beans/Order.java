/**
 * This Class contains information fields about the Clients Order.
 * 	userid	- Id of Client.
 * 	items	- List of id's and quantities of products in the Order.
 * 	totalquantity	- Total quantity of products from the Order.
 * 	totalcost	- Total cost of products from the Order.
 * 	ordernumber	- Number of the Order.
 * 	date	- Order creations Date.
 * 	confirmed	- Boolean field to conform or reject an order.
 * 	completed	- Boolean field if the Order is completed. 
 */

package com.beans;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

	@JsonProperty("userid")
	private String userid;
	
	@JsonProperty("items")
	private List<String> items;
	
	@JsonProperty("totalquantity")
	private int totalquantity;
	
	@JsonProperty("totalcost")
	private long totalcost;
	
	@JsonProperty("ordernumber")
	private int ordernumber;
	
	@JsonProperty("date")
	private String date;
	
	@JsonProperty("confirmed")
	private boolean confirmed;
	
	@JsonProperty("completed")
	private boolean completed;

	/**
	 * Default Constructor.
	 */
	public Order() {

	}
	
	/**
	 * Constructor of the Class with arguments.
	 * @param userid	The Clients id.
	 * @param items		List of id's and quantities of products in the Order.
	 * @param totalquantity	Total number of products in the Order.
	 * @param totalcost	Total cost of products in the Order.
	 * @param ordernumber	Number of the Order.
	 * @param date	Order creations Date.
	 * @param confirmed	Boolean value, if the Order is confirmed or not, only the administrator can change it's value.
	 * @param completed	Boolean value if the Order is confirmed or not, only the administrator can change it's value.
	 */
	public Order(String userid, List<String> items, int totalquantity, long totalcost, int ordernumber, String date, boolean confirmed, boolean completed) {
		this.userid = userid;
		this.items = items;
		this.totalquantity = totalquantity;
		this.totalcost = totalcost;
		this.ordernumber = ordernumber;
		this.date = date;
		this.confirmed = confirmed;
		this.completed = completed;
	}

	/*
	 * Public getters and setters.
	 */
	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public List<String> getItems() {
		return items;
	}

	public void setItems(List<String> items) {
		this.items = items;
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

	public int getOrdernumber() {
		return ordernumber;
	}

	public void setOrdernumber(int ordernumber) {
		this.ordernumber = ordernumber;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	/**
	 * The overridden hashCode() method.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (completed ? 1231 : 1237);
		result = prime * result + (confirmed ? 1231 : 1237);
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + ordernumber;
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
		Order other = (Order) obj;
		if (completed != other.completed)
			return false;
		if (confirmed != other.confirmed)
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		if (ordernumber != other.ordernumber)
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

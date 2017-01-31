/**
 * This class is insert, update and delete the Orders in the DB, after processing the data.
 */

package com.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.beans.Order;
import com.beans.Product;
import com.encrypt.MessageCreator;
import com.google.gson.Gson;

public class OrderDao {
	
	private ProductDao productDao = new ProductDao();
	private MessageCreator mc = new MessageCreator();
	
	/**
	 * Create the new Order in the DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param order	The Order, that will be inserted in the DB.
	 * @return	Id of the Order.
	 */
	public String createOrder(TransportClient transportClient, Order order) {
		String result = null;
		
		IndexResponse response = transportClient.prepareIndex("shoppingcart", "order")
				 .setSource(new Gson().toJson(order))
			     .get();
		result = response.getId();
		
		return result;
	}
	
	/**
	 * Read the Order from the DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param orderid	The Order id.
	 * @return	The Order from the DB.
	 */
	public Order getOrderById(TransportClient transportClient, String orderid) {
		Order order = null;
		
		GetResponse response = transportClient.prepareGet("shoppingcart", "order", orderid).get();
		
		if (response.isExists()) {
			ObjectMapper mapper = new ObjectMapper();
			
			try {
				order = mapper.readValue(response.getSourceAsString(), Order.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return order;
	}
	
	/**
	 * Update the Order.
	 * @param transportClient	The connection to the DataBase.
	 * @param order	Order that will be updated.
	 * @param orderid	Id of Order.
	 */
	private void updateOrder(TransportClient transportClient, Order order, String orderid) {
		deleteOrder(transportClient, orderid);
		
		transportClient.prepareIndex("shoppingcart", "order", orderid)
				 .setSource(new Gson().toJson(order))
			     .get();
	}
	
	/**
	 * Delete the Order.
	 * @param transportClient	The connection to the DataBase.
	 * @param orderid	Id of the Order.
	 */
	private void deleteOrder(TransportClient transportClient, String orderid) {

		transportClient.prepareDelete("shoppingcart", "order", orderid).get();
			
	}
	
	/**
	 * Display the Client Order.
	 * @param transportClient	The connection to the DataBase.
	 * @param userid	The Client id.
	 * @return	Information of the Order.
	 */
	public String displayOrders(TransportClient transportClient, String userid) {
		List<Order> orders = getOrdersByUserid(transportClient, userid);
		
		// change the product id from the list with the product name....
		orders = changeProductIdToName(transportClient, orders);
		JSONArray jsonArray = new JSONArray(orders);
		
		return jsonArray.toString();
	}
	
	/**
	 * Change the id from the list with the name of the product.
	 * @param transportClient	The connection to the DataBase.
	 * @param orders	List of Orders with id's.
	 * @return	List of Orders with product names.
	 */
	private List<Order> changeProductIdToName(TransportClient transportClient, List<Order> orders) {
		for (Order order : orders) {
			List<String> newProductList = new ArrayList<>();
			
			List<String> productList = order.getItems();
			for (String s : productList) {
				if (!NumberUtils.isDigits(s)) {
					Product product = productDao.readProductById(transportClient, s);
					newProductList.add(product.getProductname());
				} else {
					newProductList.add(s);
				}
			}
			order.setItems(newProductList);
		}
		return orders;
	}
	
	/**
	 * If the user is an administrator, not a simple Client, can see all of Orders.
	 * @param transportClient	The connection to the DataBase.
	 * @return	A list of all orders.
	 */
	public String getAllOrders(TransportClient transportClient) {
		List<Order> list = new ArrayList<>();
		
		SearchResponse response = transportClient.prepareSearch("shoppingcart")
				.setTypes("order")
				.setQuery(QueryBuilders.matchAllQuery())
				.execute().actionGet();
		
		SearchHit[] hits = response.getHits().getHits();
		if (hits.length != 0) {
			for (SearchHit hit : hits) {
				ObjectMapper mapper = new ObjectMapper();
				Order order;
				try {
					order = mapper.readValue(hit.getSourceAsString(), Order.class);
					list.add(order);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			list = changeProductIdToName(transportClient, list);
			JSONArray jsonArray = new JSONArray(list);
			
			return jsonArray.toString();
		}
		
		return mc.setMessage("Success", "No orders.");
	}
	
	/**
	 * Display information in detail about the order.
	 * @param transportClient	The connection to the DataBase.
	 * @param order	Order that will be processed.
	 * @return	Information in detail, about the Order.
	 */
	public String displayOrderDetail(TransportClient transportClient, Order order) {
		String result = null;
		
		JSONObject json = new JSONObject(order);
		json.remove("items");
		
		JSONArray jsonArray = new JSONArray();
		jsonArray.put(0, json);
		
		int index = 1;
		for (String productIdOrQuantity : order.getItems()) {
			if (!NumberUtils.isDigits(productIdOrQuantity)) {
				Product product  = productDao.readProductById(transportClient, productIdOrQuantity);
				json = new JSONObject(product);
			} else {
				json.put("quantity", new Integer(productIdOrQuantity));
				jsonArray.put(index, json);
				index++;
			}
		}
		result = jsonArray.toString();
		return result;
	}
	
	// for users
	/**
	 * Display all information about the Client Orders.
	 * @param transportClient	The connection to the DataBase.
	 * @param userid	Id of Client.
	 * @return	List of Orders, that the Client has.
	 */
	private List<Order> getOrdersByUserid(TransportClient transportClient, String userid) {
		List<Order> orders = new ArrayList<>();
		
		SearchResponse response = transportClient.prepareSearch("shoppingcart")
				.setTypes("order")
				.setQuery(QueryBuilders.termQuery("userid", userid))
				.execute().actionGet();
		
		SearchHit[] hits = response.getHits().getHits();
		if (hits.length != 0) {
			for (SearchHit hit : hits) {
				ObjectMapper mapper = new ObjectMapper();
				Order order;
				try {
					order = mapper.readValue(hit.getSourceAsString(), Order.class);
					orders.add(order);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			return null;
		}

		return orders;
	}
	
	/**
	 * Update the Order.
	 * @param transportClient	The connection to the DataBase.
	 * @param orderid	Id of Order.
	 * @param input	Information about the update.
	 * @return
	 */
	public String updateOrder(TransportClient transportClient, String orderid, String input) {
		String result = null;
		Product product = null;
		
		// check if order exists
		Order order = getOrderById(transportClient, orderid);
		if (order == null) {
			result = mc.setMessage("Failed", "Inexistent orderid.");
		} else {
			JSONObject json = null;
			try {
				// create a json from the input, if it's valid, ask for the product 
				json = new JSONObject(input);
				product = productDao.readProductById(transportClient, json.getString("productid"));
			} catch (JSONException e) {
				e.printStackTrace();
				result = mc.setMessage("Failed", "Invalid data.");
				return result;
			}
			
			if (product == null) {
				result = mc.setMessage("Failed", "Inexistent product.");
				return result;
			}
			
			// validate the quantity from the input
			int quantity = json.getInt("quantity");
			if (quantity == 0 || quantity > product.getInstock()) {
				result = mc.setMessage("Failed", "Invalid quantity.");
			} else {
				result = addRemoveProductsInOrder(transportClient, order, product, quantity, orderid);
			}
		}
		return result;
	}
	
	/**
	 * Add or remove Products from an Order, only the Administrator can do it.
	 * @param transportClient	The connection to the DataBase.
	 * @param order	Order we will modify. 
	 * @param product	Product added or removed from the Order.
	 * @param quantity	Number of Products added or removed.
	 * @param orderid	Id of order.
	 * @return	Success or Fail.
	 */
	private String addRemoveProductsInOrder(TransportClient transportClient, Order order, Product product, int quantity, String orderid) {
		String result = null;
		
		List<String> productList = order.getItems();
		// a lot of ugly calculation....
		if (productList.contains(product.getId())) {
			int index = productList.indexOf(product.getId()) + 1;
			
			int oldQuantity = Integer.parseInt(productList.get(index));
			int newQuantity = oldQuantity + quantity;
			
			if (newQuantity < 0) {
				result = mc.setMessage("Failed", "Quantity to much to remove.");
				return result;
			} else if (newQuantity == 0) {
				productList.remove(index);
				productList.remove(product.getId());
			} else {
				productList.set(index, String.valueOf(newQuantity));
			}
		} else {
			if (quantity <= 0) {
				result = mc.setMessage("Failed", "Can't add 0 or less quantity.");
				return result;
			}
			productList.add(product.getId());
			productList.add(String.valueOf(quantity));
			order.setItems(productList);
		}	
		
		int newTotalQuantity = order.getTotalquantity() + quantity;
		long newTotalCost = order.getTotalcost() + (product.getPrice() * quantity);
		order.setTotalcost(newTotalCost);
		order.setTotalquantity(newTotalQuantity);
		
		updateOrder(transportClient, order, orderid);
		
		result = mc.setMessage("Success", "Order updated.");
		return result;		
	}
	
	/**
	 * Confirm or reject an Order. Only the admin has the rights to do it.
	 * @param transportClient	The connection to the DataBase.
	 * @param orderid	Id of the Order.
	 * @param state	true if it's confirmed, false if it's rejected.
	 * @return	Success or fail.
	 */
	public String confirmRejectOrder(TransportClient transportClient, String orderid, boolean state) {
		String result = null;
		
		Order order = getOrderById(transportClient, orderid);
		if (order == null) {
			result = mc.setMessage("Failed", "Inexistent orderid.");
			return result;
		} else {
			order.setConfirmed(state);
			updateOrder(transportClient, order, orderid);
			
			result = mc.setMessage("Success", "Order confirmed/rejected.");
			return result;
		}
	}
	
	/**
	 * Complete the Order, only the admin has the rights to do it.
	 * @param transportClient	The connection to the DataBase.
	 * @param orderid	Id of the Order.
	 * @return	Success or fail.
	 */
	public String completeOrder(TransportClient transportClient, String orderid) {
		String result = null;
		
		Order order = getOrderById(transportClient, orderid);
		
		if (order == null) {
			result = mc.setMessage("Failed", "Inexistent orderid.");
		} else if (!order.isConfirmed()) {
			result = mc.setMessage("Failed", "Cannot complete a rejected order.");
		} else {
			order.setCompleted(true);
			updateOrder(transportClient, order, orderid);
			result = mc.setMessage("Success", "Order completed");
		}
		return result;
	}
}

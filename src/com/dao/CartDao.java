/**
 * This class inserts, updates or deletes products from the DB.
 * 
 * @author sandor.naghi
 */

package com.dao;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.beans.Cart;
import com.beans.Order;
import com.beans.Product;
import com.encrypt.MessageCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

public class CartDao {

	private ProductDao productDao = new ProductDao();
	private MessageCreator mc = new MessageCreator();

	/**
	 * Insert the Cart in DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param cart	Cart that will be inserted in DB.
	 * @return	The Cart that is inserted.
	 */
	private Cart createCart(TransportClient client, Cart cart) {
		
		IndexResponse response = client.prepareIndex("shoppingcart", "cart", cart.getUserid())
				 .setSource(new Gson().toJson(cart))
			     .get();
		
		// check if the response is the same as the userid
		if (cart.getUserid().equals(response.getId())) {
			return cart;
		}
		
		return null;
	}
	
	/**
	 * Check if a Client has, and read the data from the DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param userid	The users id.
	 * @return	The Cart if exists, or null if not exists.
	 */
	private Cart readCart(TransportClient client, String userid) {
		Cart cart = null;
		
		GetResponse response = client.prepareGet("shoppingcart", "cart", userid).get();

		// reading the object from the response
		if (response.isExists()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				cart = mapper.readValue(response.getSourceAsString(), Cart.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return cart;
	}
	
	/**
	 * Update the Cart.
	 * @param transportClient	The connection to the DataBase.
	 * @param cart	Cart that will be updated.
	 */
	private void updateCart(TransportClient client, Cart cart) {

		UpdateRequest updateRequest = new UpdateRequest("shoppingcart", "cart", cart.getUserid());
				
		try {
			updateRequest.doc(jsonBuilder().startObject()
					.field("totalquantity", cart.getTotalquantity())
					.field("totalcost", cart.getTotalcost())
					.field("items", cart.getItems())
					.endObject());
			client.update(updateRequest).get();
		} catch (IOException | InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Delete a Cart from DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param cart	Cart that will be deleted.
	 */
	private void deleteCart(TransportClient client, Cart cart) {

		client.prepareDelete("shoppingcart", "cart", cart.getUserid()).get();
		
	}
	
	/**
	 * Show the products from the Cart, based on the Clients id.
	 * @param transportClient	The connection to the DataBase.
	 * @param userid	The Client id.
	 * @return	A JSONArray object with all the information from the Cart.
	 */
	public JSONArray getClientProducts(TransportClient transportClient, String userid) {
		
		Cart cart = readCart(transportClient, userid);
		// if the cart for the client is empty return null;
		
		JSONArray jsonArray = new JSONArray();
		
		if (cart == null) {
			return jsonArray;
		}
		
		// create a json object, and add totalquantity, and totalcost
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("totalquantity", cart.getTotalquantity());
		jsonObject.put("totalcost", cart.getTotalcost());
		
		jsonArray.put(0, jsonObject);
		
		// this is not nice at all.....
		
		//create a json Object with the product and the quantity
		JSONObject json = null;
		int index = 1;
		for (String productIdOrQuantity : cart.getItems()) {
			if (!NumberUtils.isDigits(productIdOrQuantity)) {
				Product product  = productDao.readProductById(transportClient, productIdOrQuantity);
				json = new JSONObject(product);
			} else {
				json.put("quantity", new Integer(productIdOrQuantity));
				jsonArray.put(index, json);
				index++;
			}
		}
		return jsonArray;
	}
	
	/**
	 * Add a Product to a Cart. Check if the Client has a Cart, if not creates it, and then add the Products.
	 * @param transportClient	The connection to the DataBase.
	 * @param userid	The Clients id.
	 * @param productid	The Products id.
	 * @param quantity	The number of products added to Cart.
	 * @return	Success or failed, depending on the values of the product.
	 */
	public String addProductToCart(TransportClient transportClient, String userid, String productid, int quantity) {
		String result = null;
		
		// if product not exists return fail
		Product product = productDao.readProductById(transportClient, productid);
		if (product == null) {
			result = mc.setMessage("Failed", "Inexistent product id.");
			return result;
		}
		
		Cart cart = readCart(transportClient, userid);
		if (cart == null) {
			// if user don't have Cart create one with the existing data
			result = insertProductToCart(transportClient, userid, product, quantity);
			
			product.setInstock(product.getInstock() - quantity);
			JSONObject json = new JSONObject(product);
			
			productDao.updateProduct(transportClient, product, json);
		} else {
			// else check if enough products are available
			if (quantity > product.getInstock()) {
				result  = mc.setMessage("Failed", "Not enough product in stock.");
			} else {
				result = updateClientCart(transportClient, cart, product, quantity);
				
				product.setInstock(product.getInstock() - quantity);
				JSONObject json = new JSONObject(product);
				
				productDao.updateProduct(transportClient, product, json);
			}
		}
		return result;
	}
	
	/**
	 * Update the Clients Cart.
	 * @param transportClient	The connection to the DataBase.
	 * @param cart	The Cart of the Client, that will be updated.
	 * @param product	The product that will be added.
	 * @param quantity	The number of products.
	 * @return Success or Fail.
	 */
	private String updateClientCart(TransportClient transportClient, Cart cart, Product product, int quantity) {
		String result = null;
		
		// calculating the totalquantity and totalcost, after adding the new product
		int totalquantity = cart.getTotalquantity() + quantity;
		long totalcost = cart.getTotalcost() + (product.getPrice() * quantity);
		
		List<String> list = cart.getItems();
		
		if (list.contains(product.getId())) {
			// if the List of items contains the product, updating the number of products
			int index = list.indexOf(product.getId()) + 1;
			
			int newQuantity = Integer.parseInt(list.get(index)) + quantity;

			list.remove(index);
			list.add(index, String.valueOf(newQuantity));
		} else {
			// else just adding the product, and the qunatity
			list.add(product.getId());
			list.add(String.valueOf(quantity));
		}
		
		// set the cart new totalquantity, totalcost, and product id's with quantity
		cart.setTotalquantity(totalquantity);
		cart.setTotalcost(totalcost);
		cart.setItems(list);
		// update the Cart in DB
		updateCart(transportClient, cart);
		
		result = mc.setMessage("Success", "Product in cart updated.");
		return result;
	}
	
	/**
	 * Create a new Cart for a Client if don't have one, and adding the products to it.
	 * @param transportClient	The connection to the DataBase.
	 * @param userid	Id of Client.
	 * @param product	Product that will be added to the Cart.
	 * @param quantity	Number of Products.
	 * @return	Success or Fail.
	 */
	private String insertProductToCart(TransportClient transportClient, String userid, Product product, int quantity) {
		String result = null;
		
		// create List of product id, and qunatity
		List<String> list = new ArrayList<>();
		list.add(product.getId());
		list.add(String.valueOf(quantity));
		
		// calculate totalcost
		long totalCost = product.getPrice() * quantity;
		
		// create the new Cart
		Cart cart = new Cart(quantity, totalCost, list);
		cart.setUserid(userid);
		
		// if method returns the cart, then insertion is ok, return success
		if (createCart(transportClient, cart) != null) {
			result = mc.setMessage("Success", "Product added to Cart.");
			return result;
		}
		
		result = mc.setMessage("Failed", "Internal error...");
		return result;
	}
	
	/**
	 * Remove a product, or products from the Cart.
	 * @param transportClient	The connection to the DataBase.
	 * @param userid	Id of Client.
	 * @param productid	Id of Product.
	 * @param quantity	Number of Products.
	 * @return	Success or Fail.
	 */
	public String removeProductFromCart(TransportClient transportClient, String userid, String productid, int quantity) {
		String result = null;
		
		// check if product exists
		Product product = productDao.readProductById(transportClient, productid);
		if (product == null) {
			result = mc.setMessage("Failed", "Inexistent product id.");
		}
		// checks if cart exists, if yes, making some ugly calculation....
		Cart cart = readCart(transportClient, userid);
		if (cart == null) {
			result = mc.setMessage("Failed.", "Client Cart empty.");
		} else {
			List<String> list = cart.getItems();
			if (!list.contains(productid)) {
				result = mc.setMessage("Failed", "User don't have this Product.");
				return result;
			} else {
				int index = list.indexOf(product.getId()) + 1;
				
				int newQuantity = Integer.parseInt(list.get(index)) - quantity;
				
				if (newQuantity < 0) {
					result = mc.setMessage("Failed", "Not enough quantity to remove.");
					return result;
				} else if (newQuantity == 0) {
					list.remove(index);
					list.remove(index - 1);
				} else {
					list.remove(index);
					list.add(index, String.valueOf(newQuantity));
				}
				int totalQuantity = cart.getTotalquantity() - quantity;
				long totalCost = cart.getTotalcost() - (product.getPrice() * quantity);
				
				product.setInstock(product.getInstock() - quantity);
				JSONObject json = new JSONObject(product);
				
				productDao.updateProduct(transportClient, product, json);
				
				cart.setTotalquantity(totalQuantity);
				cart.setTotalcost(totalCost);
				cart.setItems(list);
				
				updateCart(transportClient, cart);
				
				result = mc.setMessage("Success", "Product in cart updated.");
			}
		}
		return result;
	}
	
	/**
	 * Checkout Cart, creating an Order.
	 * @param transportClient	The connection to the DataBase.
	 * @param userid	Id of Client.
	 * @return	Success or Fail.
	 */
	public String checkoutCart(TransportClient transportClient,String userid) {
		String result = null;
		
		// check if cart exists...
		Cart cart = readCart(transportClient, userid);
		if (cart == null) {
			result = mc.setMessage("Failed", "Inexistent userid, or user has empty Cart.");
			return result;
		} else {
			// create random order number
			int orderNumber = Integer.parseInt(RandomStringUtils.random(5, "0123456789"));
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date date = new Date();
			
			// create Order and insert it to DB..
			Order order = new Order(cart.getUserid(), cart.getItems(), cart.getTotalquantity(), cart.getTotalcost(), orderNumber, dateFormat.format(date), false, false);
			
			OrderDao orderDao = new OrderDao();
			String id = orderDao.createOrder(transportClient, order);
			
			result = mc.setMessage("Success", "Id: " + id);
			
			deleteCart(transportClient, cart);		
		}
		return result;
	}
}
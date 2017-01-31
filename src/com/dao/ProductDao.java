/**
 *  This class is used to make Product verifications, inserts, and queries in the Elasticsearch DB.
 *  
 *  @author sandor.naghi
 */

package com.dao;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONException;
import org.json.JSONObject;

import com.beans.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;


public class ProductDao {
	
	/**
	 * Insert a new product in the DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param input	The data of the new Product.
	 * @return	The id of the new inserted Product, or null if the insertion was not successful.
	 */
	public String createProduct(TransportClient transportClient, String input){
		Product product = null;
		String id = null;
		
		try {
			ObjectMapper mapper = new ObjectMapper();
			product = mapper.readValue(input, Product.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (product != null && !productExists(transportClient, product)) {
		
			IndexResponse response = transportClient.prepareIndex("shoppingcart", "product")
			        .setSource(new Gson().toJson(product))
			        .get();
				
			id = response.getId();
					
		}
		
		return id;
	}
	
	/**
	 * Get a product from DB, identified by the id.
	 * @param transportClient	The connection to the DataBase.
	 * @param id	The id of the Product.
	 * @return	The product, if exists, or null if not. 
	 */
	public Product readProductById(TransportClient transportClient, String id) {

		Product product = null;
						
		GetResponse response = transportClient.prepareGet("shoppingcart", "product", id).get();
			
		if (response.isExists()) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				product = mapper.readValue(response.getSourceAsString(), Product.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
			product.setId(id);
		}

		return product;
	}
	
	/**
	 * Update a product, the price, the number in the stock, or the description.
	 * @param transportClient	The connection to the DataBase.
	 * @param product	The Product need to be updated.
	 * @param json	A Json object with the information that updates the Product.
	 */
	public void updateProduct(TransportClient transportClient, Product product, JSONObject json) {

		UpdateRequest updateRequest = new UpdateRequest("shoppingcart", "product", product.getId());
		
		try {
			updateRequest.doc(jsonBuilder().startObject().field("productname", json.get("productname"))
					.field("instock", json.get("instock"))
					.field("price", json.get("price"))
					.field("description", json.get("description"))
					.field("imageURL", json.get("imageURL"))
					.endObject());
			
			transportClient.update(updateRequest).get();
		} catch (JSONException | IOException | InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete a Product from the DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param id	The id of the Product.
	 * @return	true if the deletion was successful, or false if not.
	 */
	public boolean deleteProduct(TransportClient transportClient, String id) {
		Product product = readProductById(transportClient, id);
		
		if (product != null) {
		
			DeleteResponse response = transportClient.prepareDelete("shoppingcart", "product", id).get();
			
			if (!response.getId().equals(id)) {			// to be shore that product is deleted
				return false;
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Get a List of all the products from the DB.
	 * @param transportClient	The connection to the DataBase.
	 * @return	The list of products existing in DB, or null if it's empty.
	 */
	public List<String> getProductList(TransportClient transportClient) {

		List<String> list = new ArrayList<>();

		SearchResponse response = transportClient.prepareSearch("shoppingcart")
				.setTypes("product")
				.setQuery(QueryBuilders.matchAllQuery())
				.execute().actionGet();
		
		SearchHit[] hits = response.getHits().getHits();
		if (hits.length != 0) {
			for (SearchHit hit : hits) {
				
				JSONObject json = new JSONObject(hit.getSourceAsString());
				json.remove("description");
				
				list.add(json.toString());
			}
		}
		
		return list;
	}
	
	/**
	 * Check if a Product exists in the DB.
	 * @param transportClient	The connection to the DataBase.
	 * @param newProduct	The product needed to be checked.
	 * @return	true if exists, false if not.
	 */
	public boolean productExists(TransportClient transportClient, Product newProduct) {

		Product product = null;
				
		SearchResponse response = transportClient.prepareSearch("shoppingcart")
				.setTypes("product")
				.setQuery(QueryBuilders.termQuery("productname", newProduct.getProductname()))
				.execute().actionGet();
		
		SearchHit[] hit = response.getHits().getHits();

		if (hit.length != 0) {
			String s = hit[0].getSourceAsString();
		
			ObjectMapper mapper = new ObjectMapper();
			try {
				product = mapper.readValue(s, Product.class);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
		
		if (product == null) {
			return false;
		}

		return true;
	}
	
	/**
	 * Verify if the data from the Request, for product, is valid.
	 * @param input	The data from the Request.
	 * @return	true if the data is valid, false if not.
	 */
	public boolean productDataExists(String input) {
		if (input.equals("")){
			return false;
		}
		
		JSONObject json = null;
		try {
			json = new JSONObject(input);
			if (json.get("productname").equals("") || json.get("description").equals("") || json.getInt("instock") <= 0 || json.getLong("price") <= 0 || json.get("imageURL").equals("")) {
				return false;
			}
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}
}

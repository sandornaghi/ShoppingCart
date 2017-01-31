/**
 * This class format the message in a JSon format, that will be returned in the response.
 * 
 * @author sandor.naghi
 */

package com.encrypt;

public class MessageCreator {

	/**
	 * Set the message that will be send as a Response.
	 * @param status	The status of the Response, Success, or Failed.
	 * @param message	Additional information.
	 * @return	A String with above information, in Json format.
	 */
	public String setMessage(String status, String message) {
		String result = "{\"Status\":\"" + status + "\",\n\"Message\":\"" + message + "\"}";
		return result;
	}

}

/**
 * This class is used to encrypt the password of the Client before insert to DB..
 * 
 * @author sandor.naghi
 */

package com.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptPassword {

	/**
	 * Encrypt the password.
	 * @param password	The Clients password.
	 * @return	The encrypted password.
	 */
	public String encryptpasswordMD5(String password) {
		
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		byte[] newPass = md.digest(password.getBytes());
		String newpassword = new String(newPass);
		
		return newpassword;
	}
}

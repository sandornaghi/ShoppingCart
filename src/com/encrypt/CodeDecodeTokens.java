/**
 * This Class is used to generate and decode tokens, to identify the Clients.
 * Checks if user is administrator, or simple Client.
 * 
 * @author sandor.naghi
 */

package com.encrypt;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.elasticsearch.client.transport.TransportClient;

import com.beans.Client;
import com.dao.ClientDao;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;

public class CodeDecodeTokens {
	
	private ClientDao clientDao = new ClientDao();
	
	// same shit, doesn't work here 
//	@Inject	
//	private TransportClient transportClient;
	
	/**
	 * Generate a JavaWebToken, based upon the Clients id, username, and password.
	 * @param id	Id of the Client.
	 * @param subject	The username of the Client.
	 * @param issuer	The password of the Client.
	 * @return	The generated JavaWebtoken.
	 */
	//public String generateToken(String id, String subject, String issuer, long ttlMillis) {
	public String generateToken(String id, String subject, String issuer) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(("Shopping cart"));
		Key signingkey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		
		JwtBuilder builder = Jwts.builder().setId(id)
							.setIssuedAt(now)
							.setSubject(subject)
							.setIssuer(issuer)
							.signWith(signatureAlgorithm, signingkey);
		
		// add the expiration date
//		if (ttlMillis >0 ){
//			long expMillis = nowMillis + ttlMillis;
//			Date exp = new Date(expMillis);
//			builder.setExpiration(exp);
//		}
		
		return builder.compact();
	}
	
	/**
	 * Decode the token.
	 * @param token	The token from the front end. 
	 * @return	A Claims object with the identification data of the Client.
	 */
	public Claims decodeToken(String token) {
		Claims claims = null;
		
		try {
			claims = Jwts.parser()
				.setSigningKey(DatatypeConverter.parseBase64Binary("Shopping cart"))
				.parseClaimsJws(token).getBody();
		} catch (ArrayIndexOutOfBoundsException | MalformedJwtException | IllegalArgumentException e) {
			
		}
		
		return claims;
	}
	
	/**
	 * Checking if the user has administrator rights.
	 * @param transportClient	The connection to the DataBase.
	 * @param token	JavaWebToken that verify the user.
	 * @return	true if the user has administrator rights, false if has not.
	 */
	public boolean userIsAdmin(TransportClient transClient, String token) {
		Claims claims = decodeToken(token);
		Client admin = null;
		
		if (claims != null) {
			admin = clientDao.readClient(transClient, claims.getId());
			if (admin.isIsadmin() && admin.getUsername().equals(claims.getSubject()) && admin.getPassword().equals(claims.getIssuer())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if the Client has the rights for respective operations.
	 * @param transportClient	The connection to the DataBase.
	 * @param token	JavaWebToken that identify's the Client.
	 * @return	true if the Client has rights for the operation, false if not.
	 */
	public boolean clientHasRights(TransportClient transportClient, String token, String userid) {
		Claims claims = decodeToken(token);
		Client client = null;
		
		if (claims != null) {
			client = clientDao.readClient(transportClient, claims.getId());
			if (client.getUsername().equals(claims.getSubject()) && client.getPassword().equals(claims.getIssuer()) && client.isIsactive() && claims.getId().equals(userid)) {
				return true;
			}
		}
		return false;
	}
	
	public String clientIsValid(String token) {
		Claims claims = decodeToken(token);
		
		if (claims != null) {
			return claims.getId();
		}
		return null;
	}
}

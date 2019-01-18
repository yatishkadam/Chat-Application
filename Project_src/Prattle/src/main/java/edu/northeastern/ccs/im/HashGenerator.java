package edu.northeastern.ccs.im;
import java.math.BigInteger; 
import java.security.MessageDigest; 
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger; 
/*
 * The class HashGenerator creates a SHA256 Hash of the input string.
 * @author : Mahima Singh
 */
public class HashGenerator {
	
	private HashGenerator()
	{		
	}
	
	private static final Logger logger = LogManager.getLogger(HashGenerator.class);
	/*
	 * @returns : A Hex Value of SHA256 Hash of input string
	 * @param : Input message to be encrypted
	 */
	public static String getSHA256(String input) throws NoSuchAlgorithmException {
		
			if(input.length() <= 0) {
				logger.error("Please give a valid password. Given password is empty ");
				return null;
			}
			  
            // Static getInstance method is called with hashing SHA 
            MessageDigest md = MessageDigest.getInstance("SHA-256"); 
  
            /*digest() method called  to calculate message digest of an input  and return array of byte */
            byte[] messageDigest = md.digest(input.getBytes()); 
  
            // Convert byte array into signum representation 
            BigInteger no = new BigInteger(1, messageDigest); 
  
            // Convert message digest into hex value 
            StringBuilder hashtext = new StringBuilder();
            hashtext.append(no.toString(16)); 
  
            while (hashtext.length() < 32) { 
                hashtext.insert(0, "0"); 
            } 
            return hashtext.toString(); 
	}

}

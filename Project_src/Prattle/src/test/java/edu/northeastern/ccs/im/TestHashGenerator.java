package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
/*
 * This class tests HashGenerator class
 * @author : Mahima Singh
 */
class TestHashGenerator {

	@Test
	void testSHA256Hashing() throws NoSuchAlgorithmException {
		assertEquals("e8603a7d4ab3a883f347cf716b9111e07f2c10876e71a0e712e0f6483ef1161c",HashGenerator.getSHA256("Secret Message from Illuminati"));
	}
	@Test
	void testSHA256HashingWithANull() throws NoSuchAlgorithmException {
		assertEquals(null,HashGenerator.getSHA256(""));
	}

}


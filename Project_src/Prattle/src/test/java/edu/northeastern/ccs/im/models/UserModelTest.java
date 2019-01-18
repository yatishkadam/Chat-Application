package edu.northeastern.ccs.im.models;

import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.HashGenerator;

class UserModelTest {

	@Test
	void testUserModel() throws NoSuchAlgorithmException {
		User alice = new User();
		User bob = new User("bob", "password");
		User charlie = new User("Charlie", "Singh", "c", "123");
		User dan = new User(5, "dan", "password");
		User tom = new User(8, "Tommy", "Darko", "tom", "1234");

		tom.setProfaneWords("fuck");
		assertEquals("fuck", tom.getProfaneWords());

		dan.tapUser();
		assertFalse(dan.setAsWiretapper());
		assertTrue(charlie.setAsWiretapper());
		assertFalse(bob.isParentalControl());
		bob.setParentalControl(true);
		assertTrue(bob.getParentalControl());
		alice.setParentalControl(true);
		assertFalse(alice.isUserTapped());
		assertTrue(alice.isParentalControl());
		tom.setWiretapper(true);
		assertTrue(tom.isWireTapper());

		charlie.setFirstName("C");
		charlie.setLastName("Brown");
		assertEquals("C", charlie.getFirstName());
		assertEquals("Brown", charlie.getLastName());
		charlie.setUsername("charles");
		charlie.setPassword("hidden");
		assertEquals("charles", charlie.getUsername());
		assertEquals(HashGenerator.getSHA256("hidden"), charlie.getPassword());
		alice.setUserId(-2);
		alice.setWiretappee(false);
		assertEquals(-2, alice.getUserId());
		alice.appendWordsTOProfane("fuck");
		alice.appendWordsTOProfane("bitch");
		assertEquals("fuck bitch", alice.getProfaneWords());
		alice.removeWordFromFilteredList("fuck");
		assertEquals(" bitch", alice.getProfaneWords());
		assertEquals("User [username=null, firstName=null, lastName=null]", alice.toString()); 
	}

}

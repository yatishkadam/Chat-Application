package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IMConnectionTest {

	@Test
	void testConnectionActive() {
		IMConnection imconnect = new IMConnection(null, 0000, null);
		
		assertFalse(imconnect.connectionActive());
	}

	@Test
	void testSendMessage() {
		IMConnection imconnect = new IMConnection(null, 0000, null);
		Assertions.assertThrows(IllegalOperationException.class, () -> imconnect.sendMessage("Failure happends"));
	}
	
	@Test
	void logoutTest() {
		IMConnection imconnect = new IMConnection(null, 0000, null);
		imconnect.loggedOut();
		assertFalse(imconnect.connectionActive());
	}
	
	@Test
	void getUserName() {
		IMConnection imconnect = new IMConnection(null, 0000, null);
		assertEquals("TooDumbToEnterRealUsername", imconnect.getUserName());
	}
	
	@Test
	void emptyUser()
	{
		IMConnection imconnect = new IMConnection(null, 0000, "");
		assertEquals("TooDumbToEnterRealUsername", imconnect.getUserName());
		Assertions.assertThrows(Exception.class , () -> imconnect.connect());
	}
	
	@Test
	void numbersAsUserName()
	{
		IMConnection imconnect = new IMConnection(null, 0000, "@!");
		Assertions.assertThrows(IllegalNameException.class , () -> imconnect.connect());
	}
	
	@Test
	void numberUserInput()
	{
		IMConnection imconnect = new IMConnection(null, 0000, "a@");
		Assertions.assertThrows(IllegalNameException.class , () -> imconnect.connect());
	}
	
	@Test
	void numberGibberishInput()
	{
		IMConnection imconnect = new IMConnection(null, 0000, "@a");
		Assertions.assertThrows(IllegalNameException.class , () -> imconnect.connect());
	}
	
	@Test
	void userAlpahaNumInput()
	{
		IMConnection imconnect = new IMConnection(null, 0000, "a1b2");
		Assertions.assertThrows(Exception.class , () -> imconnect.connect());
	}
	
	@Test
	void loginFailure()
	{
		IMConnection imconnect = new IMConnection("localhost", 0101, "host");
		assertFalse(imconnect.login());
	}
	@Test
	void testHostNameGetter() {
		IMConnection imconnect = new IMConnection("localhost", 0101, "host");
		assertEquals("localhost",imconnect.getHostName());
	}
	@Test
	void testPortNumGetter() {
		IMConnection imconnect = new IMConnection("localhost", 8080, "host");
		assertEquals(8080,imconnect.getPortNum());
	}
	@Test
	void testPortNumValid() {
		IMConnection imconnect = new IMConnection("localhost", 8080, "host");
		assertTrue(imconnect.testPortNumValid());
		IMConnection test2 = new IMConnection("localhost", -1, "host");
		assertFalse(test2.testPortNumValid());
		IMConnection test3 = new IMConnection("localhost", 65536, "host");
		assertFalse(test3.testPortNumValid());
	}
}

package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.Message;
/*
 * @author : Mahima Singh
 * The class tests the public and private methods of Message Class
 */
class MessageTest {

	@Test
	void testMakeQuitMessage() {
		Message quitMessage = Message.makeQuitMessage("This is a quit message");
		assertNotNull(quitMessage);
		assertTrue(quitMessage instanceof Message);
	}
	
	@Test
	void testMakeBroadcastMessage() {
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		assertNotNull(broadcast);
		assertTrue(broadcast instanceof Message);
	}
	
	@Test
	void testmakeNoAcknowledgeMessage() {
		Message message = Message.makeNoAcknowledgeMessage();
		assertTrue(message instanceof Message);
		assertNotNull(message);
	}
	@Test
	void testmakeAcknowledgeMessage() {
		Message message = Message.makeAcknowledgeMessage("This is a acknowldege message");
		assertNotNull(message);
		assertTrue(message instanceof Message);
	}
	
	@Test
	void  testMakeLoginMessage() {
		Message message = Message.makeSimpleLoginMessage("This is a simple login message","This is a text");
		assertNotNull(message);
		assertTrue(message instanceof Message);
	}
	@Test
	void testGetName() {
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		String name = broadcast.getName();
		assertEquals("user",name);
	}
	
	@Test
	void testGetText() {
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		String text = broadcast.getText();
		assertEquals("This is a broadcast message",text);
	}
	
	@Test
	void testAcknowledge() {
		Message message = Message.makeAcknowledgeMessage("This is a acknowldege message");
		assertTrue(message.isAcknowledge());
	}
	
	@Test
	void testAcknowledgeFailure() {
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		assertFalse(broadcast.isAcknowledge());
	}
	@Test
	void testBroadcast() {
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		assertTrue(broadcast.isBroadcastMessage());
	}
	@Test
	void testBroadcastFailure() {
		Message message = Message.makeAcknowledgeMessage("This is a acknowldege message");
		assertFalse(message.isBroadcastMessage());
	}
	@Test
	void testDisplay() {
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		assertTrue(broadcast.isDisplayMessage());
	}
	@Test
	void testDisplayMessageFailure() {
		Message message = Message.makeAcknowledgeMessage("This is a acknowldege message");
		assertFalse(message.isDisplayMessage());
	}
	
	@Test
	void testIntialiazationFailure() {
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		assertFalse(broadcast.isInitialization());
	}
	
	@Test
	void testInitialization() {
		Message helloMessage = Message.makeSimpleLoginMessage("This is a simple hello message","This is a text");
		assertTrue(helloMessage.isInitialization());
		
	}
	
	@Test
	void testTerminateFailure() {
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		assertFalse(broadcast.terminate());
	}
	
	@Test
	void testTerminateSuccess() {
		Message quitMessage = Message.makeQuitMessage("This is a quit message");
		assertTrue(quitMessage.terminate());
	}
	
	@Test
	void testHelloMessage() {
		Message hello = Message.makeHelloMessage("This is a test hello message");
		assertTrue( hello instanceof Message);
	}
	
	@Test
	void testMakeMessage() throws IOException {
		Message test1 = Message.makeMessage("BYE", "Alice", "Alice Saying bye");
		assertTrue(test1 instanceof Message);
		Message test2 = Message.makeMessage("NAK", "Alice", "No Acknowledgement");
		assertTrue(test2 instanceof Message);
		Message test3 = Message.makeMessage("ACK", "Alice", "Acknowledgement from Alice");
		assertTrue(test3 instanceof Message);
		Message test4 = Message.makeMessage("HLO", "Alice", "Hello Message");
		assertTrue(test4 instanceof Message);
		Message test5 = Message.makeMessage("BCT", "Alice", "This is a broadcast message");
		assertTrue(test5 instanceof Message);
		Message test6 = Message.makeMessage("Garbage", "Alice", "This is a garbage");
		assertNull(test6);
	}
	
	@Test
	void testToString() throws IOException {
		Message test1 = Message.makeMessage("BYE", "Alice", "Alice Saying bye");
		assertEquals("BYE 5 Alice 2 --",test1.toString());
	}
	
	@Test
	void testToStringNullUser() throws IOException {
		Message test1 = Message.makeMessage("BYE", null, "Alice Saying bye");
		assertEquals("BYE 2 -- 2 --",test1.toString());
	}
	
	@Test
	void testToStringNullMessage() throws IOException {
		Message test1 = Message.makeMessage("BYE", "Alice", null);
		assertEquals("BYE 5 Alice 2 --",test1.toString());
	}

	@Test
	void testGetRecName() {
		String messageText = "@alice what's up";
		
		Message testMsg = Message.makePrivateMessage("bob", messageText);
		assertEquals("alice", testMsg.getRecName());		
	}
	
	@Test 
	void testMakeCommandMessage()
	{
		Message testCommandMsg = Message.makeCommandMessage("alice", "/getfirstname");
		assertTrue(testCommandMsg instanceof Message);
		assertTrue(testCommandMsg.isCommandMessage());
	}
	
	@Test
	void testMakePrivateMessage() 	{
		Message testPrivateMessage = Message.makePrivateMessage("ash", "@pikachu pika pika!");
		assertEquals("ash", testPrivateMessage.getName());
		assertTrue(testPrivateMessage.isPrivateMessage());
			
	}
}

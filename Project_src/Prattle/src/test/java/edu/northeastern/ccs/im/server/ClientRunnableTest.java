package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


import org.junit.jupiter.api.*;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.server.ClientRunnable;

class ClientRunnableTest {

	private static SocketChannel clientSocket;
	private static ClientRunnable clientThread;

	private Queue<Message> messages;

	@Test
	void testEnqueueMessage() throws IOException {
		clientSocket = SocketChannel.open();

		clientThread = new ClientRunnable(clientSocket);
		// Thread newClient = new Thread(clientThread);
		// test message
		String sender = "client1";
		String text = "Hello this is the test message";
		Message testMsg = Message.makeBroadcastMessage(sender, text);
		clientThread.enqueueMessage(testMsg);

		assertEquals(testMsg.getName(), "client1");
		assertEquals(testMsg.getText(), "Hello this is the test message");
	}

	@Test
	void testBroadcastMessageIsSpecial() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, NoSuchMethodException, SecurityException {
		
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);
		
		Method isMessageSpecial = ClientRunnable.class.getDeclaredMethod("broadcastMessageIsSpecial", Message.class);
		isMessageSpecial .setAccessible(true);
		
		Message testMsg = Message.makeBroadcastMessage("dan", "test message");
		assertFalse((boolean) isMessageSpecial.invoke(clientThread, testMsg));
		
		Message specialMsg = Message.makeBroadcastMessage("client1", "What is the date?");
		assertTrue((boolean) isMessageSpecial.invoke(clientThread, specialMsg));
		
		Message emptyMessage = Message.makeBroadcastMessage("kt", null);
		assertFalse((boolean) isMessageSpecial.invoke(clientThread, emptyMessage));

	}
	
	@Test
	void testSetUserName() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);
		
		// username has not been set so it is null
		assertEquals(null, clientThread.getName());
		
		Method setUsername = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUsername.setAccessible(true);
		
		// username cannot be null
		String username = null;
		assertFalse((boolean) setUsername.invoke(clientThread, username));
		
		// username not yet set, so it sets the username as "karan"
		assertTrue((boolean) setUsername.invoke(clientThread, "karan"));
		assertEquals("karan", clientThread.getName());
			
	}
	
	@Test
	void testGetUserId() throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException,
	IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);
		User user;
		
		Field userField = ClientRunnable.class.getDeclaredField("user");
		userField.setAccessible(true);
		user = (User) userField.get(clientThread);
		int userId = user.getUserId();

		assertEquals(userId, clientThread.getUserId());
	}
	
	@Test
	void testGetName() throws IOException {
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);
		String clientThreadName = null;
		assertEquals(clientThreadName, clientThread.getName());
	}

	@Test
	void testSetName() throws IOException {
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);
		clientThread.setName("Mr. Fox");
		String clientUsername = "Mr. Fox";
		assertEquals(clientUsername, clientThread.getName());
	}

	@Test
	void testIsInitialized() throws IOException {
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);

		messages = new ConcurrentLinkedQueue<Message>();

		Message newMsg = Message.makeBroadcastMessage("kt", "simple hello message");
		messages.add(newMsg);
		//clientThread.run();
		String clientName = "Mr. Fox";
		clientThread.setName(clientName);
		assertFalse(clientThread.isInitialized());
	}

	@SuppressWarnings("unchecked")
	@Test
	void testHandleSpecial()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException, NoSuchFieldException {

		Queue<Message> specialResponse = null;
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);

		Field field = ClientRunnable.class.getDeclaredField("specialResponse");
		field.setAccessible(true);
		specialResponse = (Queue<Message>) field.get(clientThread);

		// no special responses added to special response queue till now
		assertEquals(0, specialResponse.size());

		// creating a special date message
		GregorianCalendar cal = new GregorianCalendar();
		Message specialDateMessage = Message.makeBroadcastMessage("NIST",
				(cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR));
		
		
		Method method = ClientRunnable.class.getDeclaredMethod("handleSpecial", Message.class);
		method.setAccessible(true);
		// handling special date message
		// handleSpecial method adds it to special Messages Queue
		method.invoke(clientThread, specialDateMessage);
		assertEquals(1, specialResponse.size());
		
		// handling another special date message
		method.invoke(clientThread, specialDateMessage);
		assertEquals(2, specialResponse.size());
		
	}
	
	@Test
	void testMessageChecks()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException, NoSuchFieldException {
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);
		
		Method messageCheck = ClientRunnable.class.getDeclaredMethod("messageChecks", Message.class);
		messageCheck.setAccessible(true);
		
		Message testMsg = Message.makeBroadcastMessage("kt", "simple test message");
		clientThread.setName("tom");		
	
		boolean testName =  (boolean) messageCheck.invoke(clientThread, testMsg);
		assertFalse(testName);
		
		clientThread.setName("kt");
		assertTrue((boolean) messageCheck.invoke(clientThread, testMsg));
		
		Message testMsg2 =  Message.makeBroadcastMessage(null, "simple test message");
		assertFalse((boolean) messageCheck.invoke(clientThread, testMsg2));
	
	}
	
	@Test
	void testsendMessage()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException, NoSuchFieldException {
		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);
		
		Method sendMessage = ClientRunnable.class.getDeclaredMethod("sendMessage", Message.class);
		sendMessage.setAccessible(true);
		Message.makeBroadcastMessage("kt", "simple test message");		
	}
	

	@Test
	void testCheckForInitialization()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, IOException, NoSuchFieldException {

		clientSocket = SocketChannel.open();
		clientThread = new ClientRunnable(clientSocket);

		Field initializedField = ClientRunnable.class.getDeclaredField("initialized");
		initializedField.setAccessible(true);
		Boolean initialized = (boolean) initializedField.get(clientThread);
		
		assertFalse(initialized);		
	}
			
}

package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.PrintNetNB;
/*
 * @author : Mahima Singh
 * This class test PrintNetNB Class.
 */
class PrintNetNBTest {
	
	PrintNetNB instance;
	static ServerSocketChannel serverSocket;
	private SocketNB socket;
	private Selector selector;
	private static String hostName = "localhost";

	
	
	@BeforeEach
	void setUp() throws IOException
	{
		// Mocking a Server for testing SocketNB
		serverSocket = ServerSocketChannel.open();
		serverSocket.configureBlocking(false);
		serverSocket.socket().bind(new InetSocketAddress(4444));
		
		selector = SelectorProvider.provider().openSelector();
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	@Test
	void testPrintNetNBCreation() throws IOException {
	
		SocketChannel socketChannel = SocketChannel.open();
		PrintNetNB instance = new PrintNetNB(socketChannel);
		// assert that the PrintNetNB object is not null
		assertNotNull(instance);
		socketChannel.close();
	} 
	@Test
	void testPrintNetNBCreationFromSocket() throws IOException{
		socket = new SocketNB(hostName,4444);
		instance = new PrintNetNB(socket);
		// assert that the PrintNetNB object is not null
		assertNotNull(instance);
	
	}
	
	@Test 
	void testPrint() throws IOException
	{
		socket = new SocketNB(hostName,4444);
		instance = new PrintNetNB(socket);
		Message broadcast = Message.makeBroadcastMessage("user", "This is a broadcast message");
		// assert that the message was successfully sent over the network
		assertTrue(instance.print(broadcast));
		
	}
	
	@Test 
	void testPrintFailure() throws IOException
	{
		socket = new SocketNB(hostName,4444);
		instance = new PrintNetNB(socket);
		Message broadcast = null;
		// assert that the message was successfully sent over the network
		Assertions.assertThrows(NullPointerException.class, () -> instance.print(broadcast));
		
	}
	
	@Test
	void testPrintWhenSetToZeroFailure() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		socket = new SocketNB(hostName, 4444);
		PrintNetNB print = new PrintNetNB(socket.getSocket());
		Field method = print.getClass().getDeclaredField("maximunTriesSending");
		method.setAccessible(true);

		method.set(print,0);
		Message broadcast = Message.makeBroadcastMessage("bob", "Failure");
		assertFalse(print.print(broadcast));
	}
	
	@Test 
	void socketFailureWhilePrinting() throws IOException
	{
		socket = new SocketNB(hostName, 4444);
		PrintNetNB print = new PrintNetNB(socket.getSocket());
		Message broadcast = Message.makeBroadcastMessage("bob", "Failure");
		socket.close();
		assertFalse(print.print(broadcast));
	}
	
	@AfterEach
	void breakDown() throws IOException
	{
		// closing the server socket
		serverSocket.close();
		
		// closing the selector on which serverSocket is registered
		selector.close();
	}

}

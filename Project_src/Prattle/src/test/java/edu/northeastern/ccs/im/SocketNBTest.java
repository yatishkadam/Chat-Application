package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Testing the SocketNB class
 * @author Prasad
 */
class SocketNBTest {

	private SocketNB socket;
	private Selector selector;
	private ServerSocketChannel serverSocket;
	private static String hostName = "localhost";
	private static int portNum = 4848;
	
	@BeforeEach
	void setUp() throws IOException
	{
		// Mocking a Server for testing SocketNB
		serverSocket = ServerSocketChannel.open();
		serverSocket.configureBlocking(false);
		serverSocket.socket().bind(new InetSocketAddress(portNum));
		
		selector = SelectorProvider.provider().openSelector();
		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
	}
	
	@Test
	void testSocketNB() throws IOException{
		
		// testing for proper connection with the running server
		socket = new SocketNB(hostName, portNum);

		// assert socket is open
		assertTrue(socket.getSocket().isOpen());
		
	}
	
	@Test
	void testSocketNBThrowingIOException() throws IOException
	{
		// we give SocketNB so as to make it throw a IOEXception
		Executable socketNB = () -> new SocketNB(hostName, 3268); 
		
		// assert that we got a IOException
		Assertions.assertThrows(Exception.class, socketNB);	
	}
	
	@Test
	void testGetSocket() throws IOException
	{
		// connect to the running server
		socket = new SocketNB(hostName, portNum);
		
		// assert that the socket is not blocking
		assertFalse(socket.getSocket().isBlocking());
		
		// assert that the socket is not blocking
		assertTrue(socket.getSocket().isConnected());
		
		// assert that the socket is open
		assertTrue(socket.getSocket().isOpen());
				
		// assert that the channel is not registered to any selector
		assertFalse(socket.getSocket().isRegistered());
		
		// register the channel to our selector, we don't need the key
		SelectionKey key = socket.getSocket().register(selector, SelectionKey.OP_CONNECT);
		
		// assert that the channel is now registered to our selector
		assertTrue(socket.getSocket().isRegistered());
		
		// assert that the key for registration on our selector is the same as we got earlier
		assertEquals(key, socket.getSocket().keyFor(selector));
		
		// running a connection to random port using getSocket
		Executable failer = () -> socket.getSocket().connect(new InetSocketAddress(hostName, 8888));
		
		//just testing connection to a random port fails
		Assertions.assertThrows(Exception.class, failer);
	}
	
	@Test
	void testClose() throws IOException
	{
		// creating new SocketNB for testing
		socket = new SocketNB(hostName, portNum);
		
		// assert that the channel is open
		assertTrue(socket.getSocket().isOpen());
		
		// now we close the channel
		socket.close();
		
		// assert that the channel is indeed closed
		assertFalse(socket.getSocket().isOpen());
	}
	
	@AfterEach
	void breakDown() throws IOException
	{
		// closing the server socket
		serverSocket.close();
		
		// closing the selector on which serverSocket is registered
		selector.close();
	}
	
	@Test
	void printFail() throws IOException
	{
		socket = new SocketNB(hostName, portNum);
		
		socket.close();
		
		assertFalse(socket.isConnected());
		
		Message msg = Message.makeHelloMessage("Hello");
		Assertions.assertThrows(IllegalOperationException.class, () -> socket.print(msg));
	
	}
	
	@Test
	void testNullSocket() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		// creating new SocketNB for testing
		socket = new SocketNB(hostName, portNum);
		
		// assert that the channel is open
		assertTrue(socket.getSocket().isOpen());
		
		// assert that the channel is indeed closed
		assertTrue(socket.getSocket().isOpen());
       Field method = socket.getClass().getDeclaredField("channel");
       method.setAccessible(true);

       method.set(socket,null);

       assertFalse(socket.isConnected());
	}
	
}

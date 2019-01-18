package edu.northeastern.ccs.im;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.im.server.Prattle;
import edu.northeastern.ccs.im.server.ServerConstants;

class ScanNetNBTest {

	private Thread server;
	private static String hostName = "localhost";
	private ExecutorService exec = Executors.newFixedThreadPool(10);
	
	@BeforeEach
	void setUp() throws Exception {
		
		server = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try {
							Prattle.main(new String[1]);
						} catch (IOException e) {
							
						}
					}
				});

		exec.submit(server);
	}
	
	@Test
	void testScanNetNBSocketChannel() throws IOException {
		// create a channel
		SocketNB socket = new SocketNB(hostName, ServerConstants.getPort());
		
		ScanNetNB scanNetNB = new ScanNetNB(socket.getSocket());
		
		scanNetNB.close();
		socket.close();
	}

	@Test
	void testScanNetNBSocketNB() throws IOException {
		// create a channel
		SocketNB socket = new SocketNB(hostName, ServerConstants.getPort());
	
		ScanNetNB scanNetNB = new ScanNetNB(socket);
		
		scanNetNB.close();
		socket.close();
	}

	@Test
	void testHasNextMessage() throws IOException, InterruptedException {
		SocketNB socket = new SocketNB(hostName, ServerConstants.getPort());
		
		PrintNetNB print = new PrintNetNB(socket.getSocket());
		ScanNetNB scan = new ScanNetNB(socket.getSocket());
		
		Message testMsg = Message.makeBroadcastMessage("tester", "Hello this is the test message");
		assertTrue(print.print(testMsg));
		
		assertFalse(scan.hasNextMessage());
		exec.shutdownNow();
		socket.close();
	}

	@Test
	void testNextMessage() throws IOException {
		SocketNB socket = new SocketNB(hostName, ServerConstants.getPort());
		ScanNetNB scan = new ScanNetNB(socket.getSocket());
		Assertions.assertThrows(Exception.class , () -> scan.nextMessage());	
		socket.close();
	}

	@Test
	void testClose() throws IOException {
		ServerSocketChannel server = ServerSocketChannel.open();
		server.configureBlocking(false);
		server.bind(new InetSocketAddress(5555));
		
		// create a channel
		SocketNB socket = new SocketNB(hostName, 5555);
		ScanNetNB scan = new ScanNetNB(socket);
		scan.close();
		socket.close();
	}

}

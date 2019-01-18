package edu.northeastern.ccs.im.server;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import edu.northeastern.ccs.im.Message;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;


class PrattleTest {

   private ServerSocketChannel serverSocket;
   private ClientRunnable clientThread;
   private String message = "Hello this is the test message";

   private ConcurrentLinkedQueue<ClientRunnable> active;
   
   
   @SuppressWarnings("unchecked")	
   @BeforeEach
   void setUp() throws IOException, IllegalAccessException, NoSuchFieldException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException
   {
       // Mocking a Server for testing SocketNB
       serverSocket = ServerSocketChannel.open();
       serverSocket.configureBlocking(false);
       serverSocket.socket().bind(new InetSocketAddress(4949));

       SocketChannel clientSocket = SocketChannel.open();
       clientThread = new ClientRunnable(clientSocket);
	   
	   Method setName = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
	   setName.setAccessible(true);
	   setName.invoke(clientThread, "pacman");
       
       Field method = Prattle.class.getDeclaredField("active");
       method.setAccessible(true);
       active = (ConcurrentLinkedQueue<ClientRunnable>) method.get("Prattle");
       active.add(clientThread);
   }
   
   @AfterEach
   void closeSocket() throws IOException{
       serverSocket.close();
   }

   @Test
   void broadcastMessage() throws IOException {
       Message testMsg = Message.makeBroadcastMessage("tester", message);
       clientThread.enqueueMessage(testMsg);
       assertEquals("tester", clientThread.getWaitingList().element().getName());
       assertEquals(message, clientThread.getWaitingList().element().getText());
   }

   @Test
   void removeClient() throws NoSuchFieldException,IllegalAccessException,IOException {
       int activeClients = active.size();
	   Prattle.removeClient(clientThread);
       
       assertEquals(activeClients-1, active.size());
   }
 
   @Test
   void TestTurnOffLoging() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
   {
	   Method method = Prattle.class.getDeclaredMethod("turnOffLogging");
       method.setAccessible(true);
       method.invoke(null);
       
	   Method method2 = Prattle.class.getDeclaredMethod("turnOnLogging");
       method2.setAccessible(true);
       method2.invoke(null);
   }
}

package edu.northeastern.ccs.im.server;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.northeastern.ccs.im.models.User;

class WrongPassword {

    private ClientRunnable client;
    private Thread server;
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
		
		client = clientRunnableForTesting();
	}

	/**
	 * Create a ClientRunnable for testing
	 * whose user is properly initialized
	 * @return ClientRunnable
	 */
	ClientRunnable clientRunnableForTesting() {
		ClientRunnable clientThread = null;
		
		try
		{
			SocketChannel clientSocket = SocketChannel.open();
			clientThread = new ClientRunnable(clientSocket);
			reflectionUserObjectCreator(clientThread);
			
			Field initialize = ClientRunnable.class.getDeclaredField("initialized");
			initialize.setAccessible(true);
			initialize.set(clientThread, true);
			assertTrue(clientThread.isInitialized());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return clientThread;
	}
	
	void reflectionUserObjectCreator(ClientRunnable clientThread)
	{
		User user = null;
		try {
			
			Field userField = ClientRunnable.class.getDeclaredField("user");
			userField.setAccessible(true);
			user = (User) userField.get(clientThread);
			user.setFirstName("Mad");
			user.setLastName("Max");
			user.setPassword("12345");
			user.setUsername("madmax");
			user.setUserId("madmax".hashCode());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	@Test
	void test() throws IOException, InterruptedException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method login = ClientRunnable.class.getDeclaredMethod("loginWorkflow", User.class, String.class);
		login.setAccessible(true);
		login.invoke(client, client.getUser(), "wrongpasswd");
	}

}

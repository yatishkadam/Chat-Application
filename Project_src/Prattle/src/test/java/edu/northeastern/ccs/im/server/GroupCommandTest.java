package edu.northeastern.ccs.im.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.db.services.ConnectionService;
import edu.northeastern.ccs.db.services.GroupService;
import edu.northeastern.ccs.db.services.MembershipService;
import edu.northeastern.ccs.db.services.UserService;
import edu.northeastern.ccs.im.HashGenerator;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.CommandService;

class GroupCommandTest {
	
	private static Logger logger = null;
	private static Connection dbConnection = null;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		logger = LogManager.getLogger(ConnectionService.class);
		dbConnection = ConnectionService.getDatabaseConnection();

		// delete previous dummy data if present
		// delete dummy memberships
		MembershipService.removeMember("dummieeUser123xyz", "dummieeGroup123xyz");
		MembershipService.removeMember("dummieeUser456def", "dummieeGroup456def");
		MembershipService.removeMember("dummieeUser789abc", "dummieeGroup789abc");

		// delete dummy groups from database after testing user services
		GroupService.deleteGroup("dummieeGroup123xyz");
		GroupService.deleteGroup("dummieeGroup456def");
		GroupService.deleteGroup("dummieeGroup789abc");
		GroupService.deleteGroup("dummeeeiiG3435");

		// delete dummy users
		UserService.deleteUser("dummieeUser123xyz");
		UserService.deleteUser("dummieeUser456def");
		UserService.deleteUser("dummieeUser789abc");
		UserService.deleteUser("dummieeUser789abcd");

		// create dummy users
		UserService.createUser("dummieeUser456def", HashGenerator.getSHA256("456def"));
		UserService.createUser("dummieeUser789abc", HashGenerator.getSHA256("789abc"));
		UserService.createUser("dummieeUser789abcd", HashGenerator.getSHA256("789abcd"));
		// create dummy groups
		GroupService.createNewGroup("dummieeGroup123xyz", "dummieeUser123xyz");
		GroupService.createNewGroup("dummieeGroup789abc", "dummieeUser789abc");
	}
	

	private ClientRunnable client;
    private Thread server;
	private ExecutorService exec = Executors.newFixedThreadPool(10);
	private Queue<Message> waitingList;
	private static final String POSSIBLE_SET = "abcdefghijklmnopqrstuvwxyz";
	
	
	
	@SuppressWarnings("unchecked")
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
		reflectionUserObjectCreator(client);
		
		Field list = ClientRunnable.class.getDeclaredField("waitingList");
		list.setAccessible(true);
		
		waitingList = (Queue<Message>) list.get(client);	
	}
	
	@Test
	void testNewGroup()
	{
		// new existing group
		CommandService.getInstance().processCommand(client, "/newgroup Hello");
	
		CommandService.getInstance().processCommand(client, "/newgroup "+"dummieeGroup123xyz");
		
		// let user join a group and test that path
		UserService.joinGroup(client.getUser().getUsername(), "dummieeGroup123xyz");
		
		// check for group in system but not part of the users
		CommandService.getInstance().processCommand(client, "/newgroup "+"dummieeGroup123xyz");
		
		// remove at the end to avoid unnecessarily large groups in users
		UserService.leaveGroup(client.getUser().getUsername(),"dummieeGroup123xyz");
		
		waitingList.clear();
		
		// send wrong command to check error prompt
		CommandService.getInstance().processCommand(client, "/newgroup");
		
		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());
	}
	
	@Test
	void TestJoinGroup()
	{
		
		CommandService.getInstance().processCommand(client, "/joingroup dummieeGroup789abc");
		
		assertEquals("Group not created!", waitingList.poll().getText());
		
		//join non-exisiting group should throw error prompt
//		CommandService.getInstance().processCommand(client, "/joingroup "+"randomGmlklnjh");
//		assertEquals("Cannot join this group as no such group exists !", waitingList.poll().getText());
	
//		CommandService.getInstance().processCommand(client, "/newgroup testing2");
//		
//		CommandService.getInstance().processCommand(client, "/leavegroup testing2");
//		
//		CommandService.getInstance().processCommand(client, "/joingroup testing2");
		
		waitingList.clear();
		
		// send wrong command to check error prompt
		CommandService.getInstance().processCommand(client, "/joingroup");
		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());
	}
	
	@Test
	void TestLeaveGroup()
	{
		CommandService.getInstance().processCommand(client, "/leavegroup");

		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());
		
		CommandService.getInstance().processCommand(client, "/leavegroup non-exisiting");
		
		assertEquals("No such group exists ! Enter a correct group name.", waitingList.poll().getText());
	}
	
	@Test
	void TestListMembers()
	{

		CommandService.getInstance().processCommand(client, "/listmembers dummieeGroup123xyz");
		
		CommandService.getInstance().processCommand(client, "/listmembers hbfwb--erojn");
	
		CommandService.getInstance().processCommand(client, "/listmembers");
	}
	
	@Test
	void TestListAdmins()
	{		
		CommandService.getInstance().processCommand(client, "/listadmins dummieeGroup123xyz");
		
		CommandService.getInstance().processCommand(client, "/listadmins hbfwb--erojn");
	
		CommandService.getInstance().processCommand(client, "/listadmins");
	}
	
	@Test
	void TestDeleteGroup()
	{
		CommandService.getInstance().processCommand(client, "/newgroup dummieeGroup789abc");
		GroupService.deleteGroup("dummieeGroup789abc");
		List<String> listOfGrpsUserIsPartOf = UserService.myGroups("dummieeUser123xyz");
		System.out.println(listOfGrpsUserIsPartOf.toString());
		assertFalse(listOfGrpsUserIsPartOf.contains("dummieeGroup789abc"));
		CommandService.getInstance().processCommand(client, "/deletegroup dummieeGroup789abc");
		listOfGrpsUserIsPartOf = UserService.myGroups(client.getUser().getUsername());
		assertFalse(listOfGrpsUserIsPartOf.contains("dummieeGroup123xyz"));
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
			
			Method setName = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
			setName.setAccessible(true);
			setName.invoke(clientThread, "pacman");
			
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
			user.setFirstName("Pacman");
			user.setLastName("Musk");
			user.setPassword("1234");
			user.setUserId(1234567);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
		
	@AfterAll
	static void tearDownAfterClass() throws Exception {
		// delete dummy users

		// delete dummy memberships
		MembershipService.removeMember("dummieeUser123xyz", "dummieeGroup123xyz");
		MembershipService.removeMember("dummieeUser123xyz", "dummieeGroup456def");
		MembershipService.removeMember("dummieeUser789abc", "dummieeGroup789abc");

		// delete dummy groups from database after testing user services
		GroupService.deleteGroup("dummieeGroup123xyz");
		GroupService.deleteGroup("dummieeGroup456def");
		GroupService.deleteGroup("dummieeGroup789abc");
		GroupService.deleteGroup("dummeeeiiG3435");

		// delete dummy users
		UserService.deleteUser("dummieeUser123xyz");
		UserService.deleteUser("dummieeUser456def");
		UserService.deleteUser("dummieeUser789abc");
		UserService.deleteUser("dummieeUser789abcd");
	}
	
	
}

package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;	

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.db.services.GroupService;
import edu.northeastern.ccs.db.services.UserService;
import edu.northeastern.ccs.im.HashGenerator;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.CommandService;

class ProcessCommandTest {
	
    private ClientRunnable client;
    private Thread server;
	private ExecutorService exec = Executors.newFixedThreadPool(10);
	private Queue<Message> waitingList;
	
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
		
		Field list = ClientRunnable.class.getDeclaredField("waitingList");
		list.setAccessible(true);
		
		waitingList = (Queue<Message>) list.get(client);	
	}

	@Test
	void testGetFirstNameCommand() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		
		CommandService.getInstance().processCommand(client, "/getfirstname");
		
		// Check we get proper first name
		assertEquals("Pacman", waitingList.poll().getText());
	}
	
	@Test
	void testGetLastNameCommand()
	{
		CommandService.getInstance().processCommand(client, "/getlastname");
		
		//check we get proper last name
		assertEquals("Musk", waitingList.poll().getText());
	}
	
	@Test
	void testGetUserNameCommand()
	{
		CommandService.getInstance().processCommand(client, "/getusername");
		
		// check we get a null as the username is not set
		assertEquals(null , waitingList.poll().getText());
	}
	
	@Test
	void testGetUserIDCommand()
	{
		CommandService.getInstance().processCommand(client, "/getuserid");
		
		// check we get correct User ID
		assertEquals("1234567" , waitingList.poll().getText());
	}
	
	@Test
	void testSetFirstName()
	{
		CommandService.getInstance().processCommand(client, "/setfirstname Elon");
		
		CommandService.getInstance().processCommand(client, "/getfirstname");
		
		// remove first element
		waitingList.poll();
		//check if we get correct 
		assertEquals("Elon", waitingList.poll().getText());
	}
	
	@Test 
	void testSetFirstNameFailure()
	{
		CommandService.getInstance().processCommand(client, "/setfirstname");
		
		//check if we get correct 
		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());
	}
	
	
	@Test
	void testSetLastName()
	{
		CommandService.getInstance().processCommand(client, "/setlastname MuskMellon");
		
		CommandService.getInstance().processCommand(client, "/getlastname");
		
		// remove first element
		waitingList.poll();
		//check if we get correct 
		assertEquals("MuskMellon", waitingList.poll().getText());
	}
	
	@Test 
	void testSetLastNameFailure()
	{
		CommandService.getInstance().processCommand(client, "/setlastname");
		
		//check if we get correct 
		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());
	}
	
	@Test
	void testDeleteAccount()
	{
		CommandService.getInstance().processCommand(client, "/deleteaccount");
	}
	
	@Test
	void testProfile()
	{
		CommandService.getInstance().processCommand(client, "/profile");
	
		String expectedProfile = "------ Profile ------\n"+
								 "Username  : null\n"+
								 "FirstName :Pacman\n"+
								 "LastName  : Musk\n"+
								 "User ID :1234567";
		
		assertEquals(expectedProfile, waitingList.poll().getText());
	}
	
	@Test
	void testGetHelp()
	{
		String help =  getHelp();
		
		CommandService.getInstance().processCommand(client, "/help");
		
		//check if we correct help command output
		assertEquals(help, waitingList.poll().getText());
		
	}
	
	/**
	 * Helper for test
	 * @return help message
	 */
	private String getHelp() {
		return "\nHELP COMMANDS:\n\n" + " Command                    Operation\n"
				+ "profile                    Gets the user's profile like firstname, lastname, userID\n"
				+ "getfirstname                Returns the first name of the user\n"
				+ "getlastname                Returns the last name of the user\n"
				+ "getuserid                Returns the user's ID\n"
				+ "setfirstname                Sets the first name of the user\n"
				+ " USAGE:                    '/setfirstname <First-Name>' \n"
				+ "setlastname                Sets the last name of the user\n"
				+ " USAGE:                    '/setlastname <Last-Name> '\n"
				+ "deleteaccount                Delete this user from the system and log out\n"
				+ "getusername                Returns user's username\n"
				+ "updatepassword                Update user's password with the provided password\n"
				+ " USAGE:                    '/updatedpassword <New-Password>'\n"
				+ "help                    Displays this help message\n" + "quit                    Logs out the user\n"
				+ "newgroup                Creates a group of the given name'\n"
				+ " USAGE:                    '/creategroup <Group-Name>'\n"
				+ "joingroup                Adds the user to the given group'\n"
				+ " USAGE:                    '/joingroup <Group-Name>'\n"
				+ "leavegroup                Removes a user from the group\n"
				+ " USAGE:                    '/leavegroup <Group-Name>'\n"
				+ "deletegroup                Deletes a group completely\n"
				+ " USAGE:                    '/deletegroup <Group-Name>'\n"
				+ "listmembers                List all members of a group\n"
				+ " USAGE:                    '/listmembers <Group-Name>\n"
				+ "listadmins                List the admin users of the group\n"
				+ " USAGE:                    '/listadmins <Group-Name>'\n"
				+ "\nCommand for Sending Private message to a User\n"
				+ " USAGE:                    @<receiver's-user-name> <message-to-be-sent>\n"
				+ "\nCommand for Sending Message to a Group\n"
				+ " USAGE:                    ><group-name> <message-to-be-sent>\n"
				+ "displayunreadmsgs        Displays Unread Messages\n"
				+ "USAGE:                    '/displayunreadmsgs'\n"
				+ "turnonparentalcontrol    Turns on parental control\n"
				+ "USAGE:                   '/turnonparentalcontrol\n"
				+ "turnoffparentalcontrol   Turns off parental control\n"
				+ "USAGE:                   '/turnoffparentalcontrol\n"
				+ "addfilteredwords         Adds words to the list of censored words\n"
				+ "USAGE:                   '/addfilteredwords <word1> <word2>'...\n"
				+ "removefilteredwords      Removes the word from the list of censored words\n"
				+ "USAGE:                   '/removefilteredwords <word>\n"
				+ "recalllastmessage                   Recalls the last message sent by user\n"
				+ "USAGE:                   '/recalllastmessage \n"
				+ "getsentmessages          Gets all messages sent by the user\n"
				+ "USAGE:                   '/getsentmessages \n"
				+ "getreceivedmessages      Gets all messages received by the user \n"
				+ "USAGE:                   '/getreceivedmessages \n"
				+ "getmessagebetween        Gets all messages between two timestamps \n"
				+ "USAGE:                   '/getmessagebetween <TimeStamp1> <TimeStamp2> \n";
	}
	
	@Test
	void testMyGroup() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		CommandService.getInstance().processCommand(client, "/mygroups");
		String expectedOut = "You are still not a member of any group!\n0 Groups";
		
		assertEquals(expectedOut, waitingList.poll().getText());
	}
	
	@Test
	void testWrongCommands()
	{
		CommandService.getInstance().processCommand(client, "/wrongcommand");
		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());
	
		CommandService.getInstance().processCommand(client, "/getwrongname");
		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());

		CommandService.getInstance().processCommand(client, "/setwrongname hello");
		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());

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
	
	@Test
   void TestgetUserFromActive() throws Exception
   {
       Field method = Prattle.class.getDeclaredField("active");
       method.setAccessible(true);
       @SuppressWarnings("unchecked")
	   ConcurrentLinkedQueue<ClientRunnable> active = (ConcurrentLinkedQueue<ClientRunnable>) method.get("Prattle");
       
	   Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
	   setUserMethod.setAccessible(true);
	   setUserMethod.invoke(client, "pacman");
       
       active.add(client);
	   User userfetched = Prattle.getUserFromActive("pacman");	
	   assertEquals("Pacman", userfetched.getFirstName());
	   
	   Assertions.assertThrows(Exception.class, () -> Prattle.getUserFromActive("nonexisitinguser"));
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
	
	@Test
	void TestTerminateClient()
	{
		Assertions.assertThrows(NullPointerException.class, () -> client.terminateClient());
	}
	
	@Test
	void testGetWaitingList() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, IOException
	{
		Message message = Message.makeBroadcastMessage("Elon", "Hello");
		client.enqueueMessage(message);
		
		Field method = Prattle.class.getDeclaredField("active");
		method.setAccessible(true);
		@SuppressWarnings("unchecked")
		ConcurrentLinkedQueue<ClientRunnable> activeUsers = (ConcurrentLinkedQueue<ClientRunnable>) method.get("Prattle");

		Field initField = ClientRunnable.class.getDeclaredField("initialized");
		initField.setAccessible(true);
		initField.set(client, true);

		// add user
		Field userField = ClientRunnable.class.getDeclaredField("user");
		userField.setAccessible(true);
		User user = (User) userField.get(client);
		user.setUsername("karan");
		user.setFirstName("Karan");
		user.setLastName("Tyagi");

		activeUsers.add(client);
	}
	
	@Test
	void TestListCommandFailure() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchAlgorithmException
	{
	    UserService.createUser("dummieeUser123xyz", HashGenerator.getSHA256("123xyz"));
		GroupService.createNewGroup("dummieeGroup123xyz", "dummieeUser123xyz");

		CommandService.getInstance().processCommand(client, "/listmembers MSD");
		CommandService.getInstance().processCommand(client, "/listplumbers MSD");
		
		waitingList.poll();
		assertEquals("No such group exists ! Enter a correct group name.", waitingList.poll().getText());
		GroupService.deleteGroup("dummieeGroup123xyz");
		UserService.deleteUser("dummieeUser123xyz");
	}
	
	@Test
	void TestUpdatePassword()
	{
		CommandService.getInstance().processCommand(client, "/updatepassword testpassword");
		assertEquals("Password updated successfully!", waitingList.poll().getText());
	}

	@Test
	void TestBecomeWireTape()
	{
		CommandService.getInstance().processCommand(client, "/becomewiretapper");
	}
	
	@Test
	void TestBecomeWireTapeFalse()
	{
		client.getUser().setWiretappee(false);
		UserService.updateUser(client.getName(), client.getUser());
		
		CommandService.getInstance().processCommand(client, "/becomewiretapper");
	}
	
	@Test
	void TestTurnOnParentalControl()
	{
		// turn off before testing
		client.getUser().setParentalControl(false);
		
		CommandService.getInstance().processCommand(client, "/turnonparentalcontrol");
		assertEquals("Parental Control is set to on", waitingList.poll().getText());
	}
	
	@Test
	void TestTurnOffParentalControl()
	{
		// turn on before testing
		client.getUser().setParentalControl(true);
		
		CommandService.getInstance().processCommand(client, "/turnoffparentalcontrol");
		assertEquals("Parental Control is set to off", waitingList.poll().getText());
	}
	
	@Test
	void TestAddRemoveFilterWords()
	{
		CommandService.getInstance().processCommand(client, "/addfilteredwords abc");
		assertEquals("words to be filtered added", waitingList.poll().getText());
		
		CommandService.getInstance().processCommand(client, "/removefilteredwords abc");
		assertEquals("words to be filtered removed", waitingList.poll().getText());
	}
	
	@Test
	void TestRecallMessage()
	{
		CommandService.getInstance().processCommand(client, "/recalllastmessage");
		assertEquals("unable to recall last sent message", waitingList.poll().getText());
		Message msg = Message.makeBroadcastMessage(client.getName(), "testing");
	}
	
	@Test
	void TestWireTap() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException
	{
	   Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
	   setUserMethod.setAccessible(true);
	   setUserMethod.invoke(client, "pacman");
	   
        Field method = Prattle.class.getDeclaredField("active");
        method.setAccessible(true);
        ConcurrentLinkedQueue<ClientRunnable> active = (ConcurrentLinkedQueue<ClientRunnable>) method.get("Prattle");
        active.add(client);

		client.getUser().setWiretapper(true);
		CommandService.getInstance().processCommand(client, "/wiretap aryan 2018-12-01 00:00:00 2018-12-01 01:00:00");
		client.getUser().setWiretapper(true);
		
		waitingList.clear();
		
		client.getUser().setWiretapper(true);
		CommandService.getInstance().processCommand(client, "/wiretap aryan 2018-11-20 00:00:00 2018-11-20 01:00:00");
		client.getUser().setWiretapper(true);
	}
	
	@Test
	void TestGetSendMessages() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "pacman");
		   
        Field method = Prattle.class.getDeclaredField("active");
        method.setAccessible(true);
        ConcurrentLinkedQueue<ClientRunnable> active = (ConcurrentLinkedQueue<ClientRunnable>) method.get("Prattle");
        active.add(client);
        
        CommandService.getInstance().processCommand(client, "/getsentmessages");
        assertEquals("No sent Messages Found!", waitingList.poll().getText());
	
		// rename client
		setUserMethod.invoke(client, "robin");
		active.add(client);
		
		Message msg = Message.makePrivateMessage(client.getName(), "@aryan Testing getSentMessages");
		Prattle.broadcastMessage(msg);
		CommandService.getInstance().processCommand(client, "/getsentmessages");
	
		setUserMethod.invoke(client, "kyle");
		active.add(client);
		
		CommandService.getInstance().processCommand(client, "/getsentmessages");
	}
	
	@Test
	void TestGetReceivedMessages() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "pacman");
		   
        Field method = Prattle.class.getDeclaredField("active");
        method.setAccessible(true);
        ConcurrentLinkedQueue<ClientRunnable> active = (ConcurrentLinkedQueue<ClientRunnable>) method.get("Prattle");
        active.add(client);
        
		CommandService.getInstance().processCommand(client, "/getreceivedmessages");
		assertEquals("No received Messages Found!", waitingList.poll().getText());
	
		setUserMethod.invoke(client, "kyle");
		active.add(client);
		
		CommandService.getInstance().processCommand(client, "/getreceivedmessages");
		assertFalse(waitingList.isEmpty());
	}
	
	@Test
	void TestSendGroupMessage() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "kyle");
		   
        Field method = Prattle.class.getDeclaredField("active");
        method.setAccessible(true);
        ConcurrentLinkedQueue<ClientRunnable> active = (ConcurrentLinkedQueue<ClientRunnable>) method.get("Prattle");
        active.add(client);
        
        Message msg = Message.makeGroupMessage(client.getName(), ">MSD testing send group message");
        Prattle.sendGroupMessage(msg);
        
        setUserMethod.invoke(client, "pacman");
        active.add(client);
        
        Message msg2 = Message.makeGroupMessage(client.getName(), ">MSD testing send group message from not a member");
        Prattle.sendGroupMessage(msg2);
	}
	
	@Test
	void TestGetMessageBetween() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "kyle");
		
		CommandService.getInstance().processCommand(client, "/getmessagebetween 2018-12-01 01:00:00 2018-12-01 02:00:00");
		assertFalse(waitingList.isEmpty());
		
		waitingList.clear();
		
		CommandService.getInstance().processCommand(client, "/getmessagebetween 2018-11-20 01:00:00 2018-11-20 02:00:00");
		assertEquals("No messages between the provided timestamps", waitingList.poll().getText());
	
		CommandService.getInstance().processCommand(client, "/getmessagebetween");
		assertEquals("Provide command in proper format. Use /help if needed!", waitingList.poll().getText());
	}
	
	@Test
	void TestGetWireTapperHelp() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "mike");
		
		client.getUser().setWiretapper(true);
		
		String expectedHelp = getWireTapperHelp();
		
		CommandService.getInstance().processCommand(client, "/help");
		assertEquals(expectedHelp, waitingList.poll().getText());
	}
	
	@Test
	void TestDisplayUnreadMsg() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "mike");
		
		client.getUser().setWiretapper(true);
		
		CommandService.getInstance().processCommand(client, "/displayunreadmsgs");
		assertEquals("Let the wiretapping begin", waitingList.poll().getText());
	}
	
	@Test
	void TestSetTarget() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "mike");
		
		client.getUser().setWiretapper(true);
		
		User user = UserService.getUserData("lxiu");
		user.setWiretappee(false);
		UserService.updateUser("lxiu", user);
		CommandService.getInstance().processCommand(client, "/settarget lxiu");
		assertEquals("Wiretap target set", waitingList.poll().getText());
		
		CommandService.getInstance().processCommand(client, "/settarget lxiu");
		assertEquals("Can't tap this user as another agency is already tapping the user", waitingList.poll().getText());
	
		CommandService.getInstance().processCommand(client, "/settarget");
	}
	
	@Test
	void TestRecallMessageById() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "kyle");
		
		CommandService.getInstance().processCommand(client, "/recallmessagebyid 8");
		assertEquals("unable to recall message by given id", waitingList.poll().getText());
	
		CommandService.getInstance().processCommand(client, "/recallmessagebyid 887");
		assertEquals("Message recalled successfully by ID", waitingList.poll().getText());
	}
	
	@Test
	void TestForwardMessage() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException
	{
		Method setUserMethod = ClientRunnable.class.getDeclaredMethod("setUserName", String.class);
		setUserMethod.setAccessible(true);
		setUserMethod.invoke(client, "Tom");
		
		ClientRunnable recvClient = clientRunnableForTesting();
		setUserMethod.invoke(recvClient, "ooof");
		
		client.getUser().setWiretappee(true);
		recvClient.getUser().setWiretappee(true);
		
		ClientRunnable recvTapperClient = clientRunnableForTesting();
		setUserMethod.invoke(recvTapperClient, "hefi");
		
		ClientRunnable senderTapperClient = clientRunnableForTesting();
		setUserMethod.invoke(senderTapperClient, "gvl");
		
		Field initField = ClientRunnable.class.getDeclaredField("initialized");
		initField.setAccessible(true);
		initField.set(recvTapperClient, true);
		initField.set(senderTapperClient, true);
		
        Field method = Prattle.class.getDeclaredField("active");
        method.setAccessible(true);
        ConcurrentLinkedQueue<ClientRunnable> active = (ConcurrentLinkedQueue<ClientRunnable>) method.get("Prattle");
        active.add(client);
		active.add(recvClient);
		active.add(recvTapperClient);
		active.add(senderTapperClient);
		
		client.getUser().setTappedBy(senderTapperClient.getName());
		recvClient.getUser().setTappedBy(recvTapperClient.getName());
		
		UserService.updateUser(client.getName(), client.getUser());
		UserService.updateUser(recvClient.getName(), recvClient.getUser());
		

		

		
//		// update to database
//		// waiting list 2
//		Field list2 = ClientRunnable.class.getDeclaredField("waitingList");
//		list2.setAccessible(true);
//		Queue<Message> waitingList2 = (Queue<Message>) list2.get(client);
//		
		Message msg = Message.makeBroadcastMessage(client.getName(), "Forward message testing");
		msg.setRecName(recvClient.getName());
		
		Prattle.forwardToWireTapper(msg);
	}
	
	
	/**
	 * Helper for gethelp method
	 * @return string help
	 */
	private String getWireTapperHelp() {
		return "\nHELP COMMANDS:\n\n" + " Command                    Operation\n"
				+ " wiretap                 Start Wire Tapping \n"
				+ " USAGE:                  '/wiretap <usename> <TimeStamp1> <TimeStamp2> \n"
				+ " help                    Get help message for wiretapper \n" + " USAGE:                  '/help\n"
				+ " quit                    Logout \n" + " USAGE:                  '/quit\n";
	}
	
}

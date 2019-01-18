package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.PrintNetNB;
import edu.northeastern.ccs.im.ScanNetNB;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.db.services.UserService;
import edu.northeastern.ccs.im.services.CommandService;
import edu.northeastern.ccs.slack.Notification;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Instances of this class handle all of the incoming communication from a
 * single IM client. Instances are created when the client signs-on with the
 * server. After instantiation, it is executed periodically on one of the
 * threads from the thread pool and will stop being run only when the client
 * signs off.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class ClientRunnable implements Runnable {

	private static final Logger logger = LogManager.getLogger(ClientRunnable.class);

	/**
	 * Number of milliseconds that special responses are delayed before being sent.
	 */
	private static final int SPECIAL_RESPONSE_DELAY_IN_MS = 5000;

	/**
	 * Number of milliseconds after which we terminate a client due to inactivity.
	 * This is currently equal to 5 hours.
	 */
	private static final long TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS = 18000000;

	/**
	 * Number of milliseconds after which we terminate a client due to inactivity.
	 * This is currently equal to 5 hours.
	 */
	private static final long TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS = 600000;

	/**
	 * Time at which we should send a response to the (private) messages we were
	 * sent.
	 */
	private Date sendResponses;

	/** Time at which the client should be terminated due to lack of activity. */
	private GregorianCalendar terminateInactivity;

	/** Queue of special Messages that we must send immediately. */
	private Queue<Message> immediateResponse;

	/** Queue of special Messages that we will need to send. */
	private Queue<Message> specialResponse;

	/** Socket over which the conversation with the single client occurs. */
	private final SocketChannel socket;

	/**
	 * Utility class which we will use to receive communication from this client.
	 */
	private ScanNetNB input;

	/** Utility class which we will use to send communication to this client. */
	private PrintNetNB output;

	/** The user for whom we use this ClientRunnable to communicate. */
	private User user;

	/**
	 * Whether this client has been initialized, set its user name, and is ready to
	 * receive messages.
	 */
	private boolean initialized;

	/**
	 * The future that is used to schedule the client for execution in the thread
	 * pool.
	 */
	private ScheduledFuture<ClientRunnable> runnableMe;

	/** Collection of messages queued up to be sent to this client. */
	private Queue<Message> waitingList;

	/**
	 * Create a new thread with which we will communicate with this single client.
	 * 
	 * @param client SocketChannel over which we will communicate with this new
	 *               client
	 * @throws IOException Exception thrown if we have trouble completing this
	 *                     connection
	 */
	public ClientRunnable(SocketChannel client) throws IOException {
		// Set up the SocketChannel over which we will communicate.
		socket = client;
		socket.configureBlocking(false);
		// Create the class we will use to receive input
		input = new ScanNetNB(socket);
		// Create the class we will use to send output
		output = new PrintNetNB(socket);

		user = new User();
		// Mark that we are not initialized
		initialized = false;
		// Create our queue of special messages
		specialResponse = new LinkedList<>();
		// Create the queue of messages to be sent
		waitingList = new ConcurrentLinkedQueue<>();
		// Create our queue of message we must respond to immediately
		immediateResponse = new LinkedList<>();
		// Mark that the client is active now and start the timer until we
		// terminate for inactivity.
		terminateInactivity = new GregorianCalendar();
		terminateInactivity
				.setTimeInMillis(terminateInactivity.getTimeInMillis() + TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
	}

	public String getIpAddressOfClient() {
		if (isInitialized()){
			try {
				return socket.getRemoteAddress().toString();
			} catch (Exception e) {
				logger.info(e);
			}
		}
		return "";
	}

	/**
	 * Determines if this is a special message which we handle differently. It will
	 * handle the messages and return true if msg is "special." Otherwise, it
	 * returns false.
	 * 
	 * @param msg Message in which we are interested.
	 * @return True if msg is "special"; false otherwise.
	 */
	private boolean broadcastMessageIsSpecial(Message msg) {
		boolean result = false;
		String text = msg.getText();
		if (text != null) {
			List<Message> responses = ServerConstants.getBroadcastResponses(text);
			if (responses != null) {
				for (Message current : responses) {
					handleSpecial(current);
				}
				result = true;
			}
		}
		return result;
	}

	/**
	 * Print the user profile
	 */
	public String printProfile() {
		StringBuilder str = new StringBuilder();
		str.append("------ Profile ------\n" + "Username  : " + this.user.getUsername() + "\n" + "FirstName :"
				+ this.user.getFirstName() + "\n" + "LastName  : " + this.user.getLastName() + "\n" + "User ID :"
				+ this.user.getUserId());
		return str.toString();
	}

	/**
	 * Check to see for an initialization attempt and process the message sent.
	 * @throws IOException 
	 * 
	 * @throws Exception
	 */
	private void checkForInitialization() {
		// Check if there are any input messages to read
		if (input.hasNextMessage()) {

			// If a message exists, try to use it to initialize the connection
			Message msg = input.nextMessage();
			if (setUserName(msg.getName())) {
				// Update the time until we terminate this client due to inactivity.
				terminateInactivity.setTimeInMillis(
						new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
				// Set that the client is initialized.
				initialized = true;
				// display default client profile everytime the User is initialized
				// if a user is not already written to the file, write it to a file

				if (UserService.checkIfUserExists(this.user.getUsername())) {
					this.user = UserService.getUserData(this.user.getUsername());
				}
				loginWorkflow(this.user, msg.getText());
			} else {
				initialized = false;
			}
		}
	}

	/*
	 * This utility function checks if a user exists and if the user exists, check if 
	 * the password provided is correct or not, if a user is a new user, it registers 
	 * the user with new username and provided password
	 * @param : Integer : UserId, String :Password
	 * 
	 * @return : true : All is good, keep the connection false : User is malicious
	 */
	private boolean loginWorkflow(User user, String password) {
		if (UserService.checkIfUserExists(user.getUsername())) {
			if (UserService.validatePassword(user.getUsername(), password)) {
				Message loginMessage = Message.makeCommandMessage(user.getUsername(), "You have successfully logged in.");
				enqueueMessage(loginMessage);
				CommandService.getInstance().processCommand(this, "/help");
				CommandService.getInstance().processCommand(this, "/displayunreadmsgs");
				return true;
			}
			else {
				Message quitMsg = Message.makeQuitMessage(user.getUsername(), "Invalid Password! Come back later");
				logger.info("A malicious user is trying to enter, breaking the connection for " + user.getUsername());
				Notification.sendNotificationToSlack("SECURITY CONCERN | LEVEL : MED | MSG - User entered invalid password ");
				enqueueMessage(quitMsg);
				return false;
			}
		} else {
			user.setPassword(password);
			logger.info("New user Detected, registering the user" + user.getUsername());
			Notification.sendNotificationToSlack("SECURITY CONCERN | LEVEL : LOW | MSG - New User : "+ user.getUsername()+" registered to the system");
			UserService.createUser(user.getUsername(), user.getPassword());
			printProfile();
			Message loginMessage = Message.makeCommandMessage(user.getUsername(),"User successfully registered");
			enqueueMessage(loginMessage);
			CommandService.getInstance().processCommand(this, "/help");
			return true;
		}
	}

	/**
	 * Process one of the special responses
	 * 
	 * @param msg Message to add to the list of special responses.
	 */
	private void handleSpecial(Message msg) {
		if (specialResponse.isEmpty()) {
			sendResponses = new Date();
			sendResponses.setTime(sendResponses.getTime() + SPECIAL_RESPONSE_DELAY_IN_MS);
		}
		specialResponse.add(msg);
	}

	/**
	 * Check if the message is properly formed. At the moment, this means checking
	 * that the identifier is set properly.
	 * 
	 * @param msg Message to be checked
	 * @return True if message is correct; false otherwise
	 */
	private boolean messageChecks(Message msg) {
		// Check that the message name matches.
		return (msg.getName() != null) && (msg.getName().compareToIgnoreCase(getName()) == 0);
	}

	/**
	 * Immediately send this message to the client. This returns if we were
	 * successful or not in our attempt to send the message.
	 * 
	 * @param message Message to be sent immediately.
	 * @return True if we sent the message successfully; false otherwise.
	 */
	public boolean sendMessage(Message message) {
		logger.info("\t" + message);
		return output.print(message);
	}

	/**
	 * Try allowing this user to set his/her user name to the given username.
	 * 
	 * @param userName The new value to which we will try to set userName.
	 * @return True if the username is deemed acceptable; false otherwise
	 */
	private boolean setUserName(String userName) {
		// Now make sure this name is legal.
		if (userName != null) {
			// Optimistically set this users ID number.
			setName(userName);
			user.setUserId(user.hashCode());
			return true;
		}
		// Clear this name; we cannot use it. *sigh*
		user.setUserId(-1);
		return false;
	}

	/**
	 * Add the given message to this client to the queue of message to be sent to
	 * the client.
	 * 
	 * @param message Complete message to be sent.
	 */
	public void enqueueMessage(Message message) {
		waitingList.add(message);
	}

	/**
	 * Get the name of the user for which this ClientRunnable was created.
	 * 
	 * @return Returns the name of this client.
	 */
	public String getName() {
		return this.user.getUsername();
	}

	/**
	 * Set the name of the user for which this ClientRunnable was created.
	 * 
	 * @param name The name for which this ClientRunnable.
	 */
	public void setName(String name) {
		this.user.setUsername(name);
	}

	/**
	 * Gets the name of the user for which this ClientRunnable was created.
	 * 
	 * @return Returns the current value of userName.
	 */
	public int getUserId() {
		return this.user.getUserId();
	}

	/**
	 * Return if this thread has completed the initialization process with its
	 * client and is read to receive messages.
	 * 
	 * @return True if this thread's client should be considered; false otherwise.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Perform the periodic actions needed to work with this client.
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		boolean terminate = false;
		// The client must be initialized before we can do anything else
		if (!initialized) {
			try {
				checkForInitialization();
			} catch (Exception e) {
				logger.error(e);
			}
		} else {
			try {
				terminate = checkAndProcessMessage(terminate);
				terminate = checkClientGoodAndSendMessage(terminate);
			} catch (Exception e) {
				logger.error(e);
			} finally {
				// When it is appropriate, terminate the current client.
				if (terminate) {
					try {
						terminateClient();
					} catch (IOException e) {
						logger.info(e);
					}
				}
			}
		}
		// Finally, check if this client have been inactive for too long and,
		// when they have, terminate
		// the client.
		if (!terminate && terminateInactivity.before(new GregorianCalendar())) {
			logger.error("Timing out or forcing off a user " + this.user.getUsername());
			try {
				terminateClient();
			} catch (IOException e) {
				logger.info(e);
			}
		}
	}

	private boolean checkClientGoodAndSendMessage(boolean terminate)
	{
		if (!immediateResponse.isEmpty()) {
			while (!immediateResponse.isEmpty()) {
				sendMessage(immediateResponse.remove());
			}
		}

		// Check to make sure we have a client to send to.
		boolean processSpecial = checkWeHaveAClient();
		boolean keepAlive = !processSpecial;
		// Send the responses to any special messages we were asked.
		if (processSpecial) {
			// Send all of the messages and check that we get valid
			// responses.
			while (!specialResponse.isEmpty()) {
				keepAlive |= sendMessage(specialResponse.remove());
			}
		}
		if (!waitingList.isEmpty()) {
			if (!processSpecial) {
				keepAlive = false;
			}
			// Send out all of the message that have been added to the
			// queue.
			do {
				Message msg = waitingList.remove();
				boolean sentGood = sendMessage(msg);
				keepAlive |= sentGood;
			} while (!waitingList.isEmpty());
		}
		terminate |= !keepAlive;
		
		return terminate;
	}
	
	
	private boolean checkWeHaveAClient()
	{
		return !specialResponse.isEmpty()
				&& ((!initialized) || (!waitingList.isEmpty()) || sendResponses.before(new Date()));
	}
	
	private boolean checkAndProcessMessage(boolean terminate)
	{
		// Client has already been initialized, so we should first check
		// if there are any input
		// messages.
		if (input.hasNextMessage()) {
			// Get the next message
			Message msg = input.nextMessage();

			// Update the time until we terminate the client for
			// inactivity.
			terminateInactivity.setTimeInMillis(
					new GregorianCalendar().getTimeInMillis() + TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS);
			// If the message is a broadcast message, send it out
			if (msg.isDisplayMessage()) {
				processDisplayMessage(msg);
			} else if (msg.isCommandMessage()) {
				CommandService.getInstance().processCommand(this, msg.getText());
			} else if (msg.isPrivateMessage()) {
				Prattle.sendPrivateMessasge(msg);
			}else if(msg.isGroupMessage()) {
				Prattle.sendGroupMessage(msg);
			} else if (msg.terminate()) {
				// Stop sending the poor client message.
				terminate = true;
				// Reply with a quit message.
				enqueueMessage(Message.makeQuitMessage(this.user.getUsername()));
			}
			// Otherwise, ignore it (for now).
		}
		
		return terminate;
	}
	
	private void processDisplayMessage(Message msg)
	{
		// Check if the message is legal formatted
		if (messageChecks(msg)) {
			// Check for our "special messages"
			if ((msg.isBroadcastMessage()) && (!broadcastMessageIsSpecial(msg))) {
				// Check for our "special messages"
				if ((msg.getText() != null)
						&& (msg.getText().compareToIgnoreCase(ServerConstants.BOMB_TEXT) == 0)) {
					initialized = false;
					Prattle.broadcastMessage(Message.makeQuitMessage(this.user.getUsername()));
				} else {
					Prattle.broadcastMessage(msg);
				}
			}
		}
		else {

			Message sendMsg;
			sendMsg = Message.makeBroadcastMessage(ServerConstants.BOUNCER_ID,
					"Last message was rejected because it specified an incorrect user name.");
			enqueueMessage(sendMsg);
		}
	}
	
	/**
	 * Store the object used by this client runnable to control when it is scheduled
	 * for execution in the thread pool.
	 * 
	 * @param future Instance controlling when the runnable is executed from within
	 *               the thread pool.
	 */
	public void setFuture(ScheduledFuture<ClientRunnable> future) {
		runnableMe = future;
	}

	/**
	 * Terminate a client that we wish to remove. This termination could happen at
	 * the client's request or due to system need.
	 * @throws IOException 
	 */
	public void terminateClient() throws IOException {
		try {
			// Once the communication is done, close this connection.
			input.close();
			socket.close();
		} catch (IOException e) {
			// If we have an IOException, ignore the problem
			logger.error(e);
		} 
		Prattle.removeClient(this);
		// And remove the client from our client pool.
		runnableMe.cancel(false);
	}
	
	public Queue<Message> getWaitingList() {
		return new LinkedList<>(waitingList);
	}

	/**
	 * Get the user for which this ClientRunnable was created.
	 * 
	 * @return Returns the user.
	 */
	public User getUser() {
		return this.user;
	}

}

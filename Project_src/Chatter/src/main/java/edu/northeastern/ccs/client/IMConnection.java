package edu.northeastern.ccs.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.SwingWorker;

/**
 * This class manages the connection between an the IM client and the IM server.
 * Instances of this class can be relied upon to manage all the details of this
 * connection and sends alerts when appropriate. Instances of this class must be
 * constructed and connected before it can be used to transmit messages.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class IMConnection {

	/**
	 * Real Connection instance which this class wraps and makes presentable to the
	 * user
	 */
	private SocketNBChatter socketConnection;

	/**
	 * List of instances that have registered as a listener for connection events.
	 */
	private Vector<LinkListener> linkListeners;

	/**
	 * List of instances that have registered as a listener for received message
	 * events.
	 */
	private Vector<MessageListener> messageListeners;

	/** Server to which this connection will be made. */
	private String hostName;

	/** Port to which this connection will be made. */
	private int portNum;

	/** Name of the user for which this connection was formed. */
	private String userName;

	/**
	 * Holds the SwingWorker which is used to read and process all incoming data.
	 */
	private SwingWorker<Void, MessageChatter> workerBee;

	/** Instance used to read the messages. */
	private MessageScanner messageScanner;

	private SocketChannel channel;
	
	private static final String NOT_CONNECTED_ERROR = "Cannot send a message if you are not connected to a server!\n";
	
	/**
	 * Creates an instance that will manage a connection with an IM server, but does
	 * not begin the process of making a connection to the IM server.
	 * 
	 * @param host     The name of the host that this connection is using
	 * @param port     The port number to use.
	 * @param username Name of the user for which this connection is being made.
	 */
	public IMConnection(String host, int port, String username) {
		if ((username == null) || username.trim().equals("")) {
			username = "TooDumbToEnterRealUsername";
		}
		linkListeners = new Vector<>();
		messageListeners = new Vector<>();
		userName = username;
		hostName = host;
		portNum = port;
	}

	/**
	 * Add the given listener to be notified whenever 1 or more Messages are
	 * received from IM server via this connection.
	 * 
	 * @param listener Instance which will begin to receive notifications of any
	 *                 messages received by this IMConnection.
	 * @throws InvalidListenerException Exception thrown when this is called with a
	 *                                  value of null for {@code listener}
	 */
	public void addMessageListener(MessageListener listener) {
		if (listener == null) {
			throw new InvalidListenerException("Cannot add (null) as a listener!");
		}
		messageListeners.add(listener);
	}

	/**
	 * Send a message to log in to the IM server using the given username. For the
	 * moment, you will automatically be logged in to the server, even if there is
	 * already someone with that username.<br/>
	 * Precondition: connectionActive() == false
	 * 
	 * @throws IllegalNameException Exception thrown if we try to connect with an
	 *                              illegal username. Legal usernames can only
	 *                              contain letters and numbers.
	 * @return True if the connection was successfully made; false otherwise.
	 */
	public boolean connect() {
		String name = getUserName();
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if (!Character.isLetter(ch) && !Character.isDigit(ch)) {
				throw new IllegalNameException("Cannot log in to the server using the name " + name);
			}
		}
		boolean retVal = login();
		MessageScanner rms = MessageScanner.getInstance();
		addMessageListener(rms);
		messageScanner = rms;
		return retVal;
	}

	/**
	 * Returns whether the instance is managing an active, logged-in connection
	 * between the client and an IM server.
	 * 
	 * @return True if the client is logged in to the server using this connection;
	 *         false otherwise.
	 */
	public boolean connectionActive() {
		if (socketConnection == null) {
			return false;
		} else {
			return socketConnection.isConnected();
		}
	}

	/**
	 * Break this connection with the IM server. Once this method is called, this
	 * instance will need to be logged back in to the IM server to be usable.<br/>
	 * Precondition: connectionActive() == true
	 */
	public void disconnect() {
		MessageChatter quitMessage = MessageChatter.makeQuitMessage(getUserName());
		socketConnection.print(quitMessage);
		KeyboardScanner.close();
	}

	/**
	 * Gets an object which can be used to read what the user types in on the
	 * keyboard without waiting. The object returned by this method should be used
	 * rather than {@link Scanner} since {@code Scanner} will cause a program to
	 * halt if there is no input.
	 * 
	 * @return Instance of {@link KeyboardScanner} that can be used to read keyboard
	 *         input for this connection of the server.
	 */
	public KeyboardScanner getKeyboardScanner() {
		return KeyboardScanner.getInstance();
	}

	/**
	 * Gets an object which can be used to get the message sent by the server over
	 * this connection. This is the only object that can be used to retrieve all
	 * these messages.
	 * 
	 * @return Instance of {@link MessageScanner} that can be used to read message
	 *         sent over this connection for this user.
	 */
	public MessageScanner getMessageScanner() {
		if (messageScanner == null) {
			throw new IllegalOperationException("Cannot get a MessageScanner if you have not connected to the server!");
		}
		return messageScanner;
	}

	/**
	 * Get the name of the user for which we have created this connection.
	 * 
	 * @return Current value of the user name and/or the username with which we
	 *         logged in to this IM server.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Unless this is a &quot;special&quot; server message, this sends the given
	 * message to all of the users logged in to the IM server. <br/>
	 * Precondition: connectionActive() == true
	 * 
	 * @param message Text of the message which will be broadcast to all users.
	 */
	public void sendMessage(String message) {
		if (!connectionActive()) {
			throw new IllegalOperationException(NOT_CONNECTED_ERROR);
		}
		MessageChatter bctMessage = MessageChatter.makeBroadcastMessage(userName, message);
		socketConnection.print(bctMessage);
	}

	/**
	 * Unless this is a special server message, this sends the given command message
	 * to the IM server.
	 * @param commandName Name of the command to be executed
	 */
	public void sendCommand(String commandName) {
		if (!connectionActive()) {
			throw new IllegalOperationException(NOT_CONNECTED_ERROR);
		}
		
		MessageChatter cmdMessage = MessageChatter.makeCommandMessage(userName, commandName);
		socketConnection.print(cmdMessage);
	}
	
	
	/**
	 * Unless this is a special server message, this sends the given Private message
	 * to the intended receiver if he/she is connected to the server.
	 * @param message Message text to be sent
	 */
	public void sendPrivateMessage(String message)
	{
		if (!connectionActive()) {
			throw new IllegalOperationException(NOT_CONNECTED_ERROR);
		}
		
		MessageChatter msg = MessageChatter.makePrivateMessageSender(userName, message);
		socketConnection.print(msg);
	}

	public void sendGroupMessage(String message)
	{
		if (!connectionActive()) {
			throw new IllegalOperationException(NOT_CONNECTED_ERROR);
		}
		
		MessageChatter msg = MessageChatter.makeGroupMessageSender(userName, message);
		socketConnection.print(msg);
	}
	
	
	/**
	 * Send a message to log in to the IM server using the given username. For the
	 * moment, you will automatically be logged in to the server, even if there is
	 * already someone with that username.<br/>
	 * Precondition: connectionActive() == false
	 * 
	 * @return True if the connection was successfully made; false otherwise.
	 */
	private boolean login() {
		// Now log in using this name.
		MessageChatter loginMessage = MessageChatter.makeLoginMessage(userName);
		try {
			socketConnection = new SocketNBChatter(hostName, portNum);
			socketConnection.startIMConnection();
		} catch (IOException e) {
			// Report the error
			System.err.println("ERROR:  Could not make a connection to: " + hostName + " at port " + portNum);
			System.err.println(
					"        If the settings look correct and your machine is connected to the Internet, report this error to Dr. Jump");
			// And print out the problem
			
			// Return that the connection could not be made.
			return false;
		}
		// Send the message to log us into the system.
		socketConnection.print(loginMessage);
		// Create the background thread that handles our incoming messages.
		workerBee = new ScanForMessagesWorker(this, socketConnection);
		// Start the worker bee scanning for messages.
		workerBee.execute();
		// Return that we were successful
		return true;
	}

	/**
	 * Starts the IM connection by performing actions like opening and binding sockets
	 * to the proper address
	 * @throws IOException when connection to the sockets fails
	 */
	protected void startIMConnection() throws IOException {
		// Open a new channel
		channel = SocketChannel.open();
		// Make this channel a non-blocking channel
		channel.configureBlocking(false);
		// Connect the channel to the remote port
		channel.connect(new InetSocketAddress(hostName, portNum));
		// Open the selector to handle our non-blocking I/O
		Selector regSelector = Selector.open();
		// Register our channel to receive alerts to complete the connection
		SelectionKey regKey = channel.register(regSelector, SelectionKey.OP_CONNECT);
		// Do nothing but wait until we have a response.
		regSelector.select(0);
		assert regKey.isConnectable();
		// Try and complete creating this connection
		if (!channel.finishConnect()) {
			throw new IOException("Error, something went wrong and I was unable to finish making this connection");
		}
		// We are done, close this selector.
		regSelector.close();

		try {
			// Open the selector to handle our non-blocking I/O
			Selector selector = Selector.open();
			// Register our channel to receive alerts to complete the connection
			channel.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			// For the moment we are going to simply cover up that there was a
			// problem.
			assert false;
		}
	}
	
	/**
	 * Triggers the message sending
	 * @param mess List of messages to be sent
	 */
	@SuppressWarnings({ "unchecked" })
	protected void fireSendMessages(List<MessageChatter> mess) {
		Vector<MessageListener> targets;
		synchronized (this) {
			targets = (Vector<MessageListener>) messageListeners.clone();
		}
		for (MessageListener iml : targets) {
			iml.messagesReceived(mess.iterator());
		}
	}

	/**
	 * Implies change in status for the given user
	 * @param userName User name of the user whose status is changed
	 */
	@SuppressWarnings("unchecked")
	protected void fireStatusChange(String userName) {
		Vector<LinkListener> targets;
		synchronized (this) {
			targets = (Vector<LinkListener>) linkListeners.clone();
		}
		for (LinkListener iml : targets) {
			iml.linkStatusUpdate(userName, this);
		}
	}

	/**
	 * Sets the socket Connection to null which is intended to imply that
	 * user has logged out
	 */
	protected void loggedOut() {
		socketConnection = null;
	}
}
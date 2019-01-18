package edu.northeastern.ccs.im.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.northeastern.ccs.db.services.ConnectionService;
import edu.northeastern.ccs.db.services.MembershipService;
import edu.northeastern.ccs.db.services.MessageService;
import edu.northeastern.ccs.db.services.UserService;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.profanityfilter.ProfanityFilter;
import edu.northeastern.ccs.slack.Notification;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * A network server that communicates with IM clients that connect to it. This
 * version of the server spawns a new thread to handle each client that connects
 * to it. At this point, messages are broadcast to all of the other clients. It
 * does not send a response when the user has gone off-line.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public abstract class Prattle {
	/*
	 * Logger
	 */
	private static final Logger logger = LogManager.getLogger(Prattle.class);

	/** Amount of time we should wait for a signal to arrive. */
	private static final int DELAY_IN_MS = 50;

	/** Number of threads available in our thread pool. */
	private static final int THREAD_POOL_SIZE = 20;

	/** Delay between times the thread pool runs the client check. */
	private static final int CLIENT_CHECK_DELAY = 200;

	/** Collection of threads that are currently being used. */
	private static ConcurrentLinkedQueue<ClientRunnable> active;

	/** connection instance for connecting to database **/
	private static Connection dbConnection;

	/** All of the static initialization occurs in this "method" */
	static {
		// Create the new queue of active threads.
		active = new ConcurrentLinkedQueue<>();

		// open connection with database
		try {
			dbConnection = ConnectionService.getDatabaseConnection();
		} catch (ClassNotFoundException | SQLException e) {
			logger.info(e);
		}
	}

	/**
	 * Broadcast a given message to all the other IM clients currently on the
	 * system. This message _will_ be sent to the client who originally sent it.
	 * 
	 * @param message Message that the client sent.
	 */
	public static void broadcastMessage(Message msg) {

		// Loop through all of our active threads
		for (ClientRunnable tt : active) {
			// Do not send the message to any clients that are not ready to receive it.
			if (tt.isInitialized()) {
				MessageService.createMessage(msg.getText(), msg.getName(), "", tt.getName(), "",
						new Timestamp(System.currentTimeMillis()), true, false);
				tt.enqueueMessage(msg);
			}
		}
		
		// check for wiretapper
		forwardToWireTapper(msg);
	}

	@SuppressWarnings("unchecked")
	private static void runPrattleWithSocket(SocketChannel inputSocket, ScheduledExecutorService pool) {

		ClientRunnable tt;
		try {
			tt = new ClientRunnable(inputSocket);

			// Add the thread to the queue of active threads
			active.add(tt);
			// Have the client executed by our pool of threads.
			@SuppressWarnings("rawtypes")
			ScheduledFuture clientFuture = pool.scheduleAtFixedRate(tt, CLIENT_CHECK_DELAY, CLIENT_CHECK_DELAY,
					TimeUnit.MILLISECONDS);
			tt.setFuture(clientFuture);
		} catch (Exception e) {
			logger.error("Caught an exception : " + e);
		}

	}

	private static void iterate(Iterator<SelectionKey> it, ServerSocketChannel serverSocket,
			ScheduledExecutorService threadPool) {

		while (it.hasNext()) {
			// Get the next key; it had better be from a new incoming connection
			SelectionKey key = it.next();
			it.remove();
			// Assert certain things I really hope is true
			assert key.isAcceptable();
			assert key.channel() == serverSocket;
			// Create a new thread to handle the client for which we just received a
			// request.
			try {
				// Accept the connection and create a new thread to handle this client.
				SocketChannel socket = serverSocket.accept();
				// Make sure we have a connection to work with.
				if (socket != null) {
					runPrattleWithSocket(socket, threadPool);
				}
			} catch (Exception e) {
				logger.info("Caught Exception: " + e.toString());
				Notification.sendNotificationToSlack("FAILURE | LEVEL : HIGH | MSG : Prattle Server Failed");
			}
		}
	}

	/**
	 * Start up the threaded talk server. This class accepts incoming connections on
	 * a specific port specified on the command-line. Whenever it receives a new
	 * connection, it will spawn a thread to perform all of the I/O with that
	 * client. This class relies on the server not receiving too many requests -- it
	 * does not include any code to limit the number of extant threads.
	 * 
	 * @param args String arguments to the server from the command line. At present
	 *             the only legal (and required) argument is the port on which this
	 *             server should list.
	 * @throws IOException            Exception thrown if the server cannot connect
	 *                                to the port to which it is supposed to listen.
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws IOException {
		
		String loggingBit = "ON";
		if(System.getenv("LOGGING")!=null) {
			loggingBit = System.getenv("LOGGING");
		}
		if(loggingBit.equals("OFF")) {
			System.out.println("Turning Off Logging for Server as Environment variable LOGGING is OFF. "
					+ "If you wish to log `export LOGGING=ON`");
			turnOffLogging();
		}
		
		// Connect to the socket on the appropriate port to which this server connects.
		try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
			serverSocket.configureBlocking(false);
			serverSocket.socket().bind(new InetSocketAddress(ServerConstants.PORT));
			// Create the Selector with which our channel is registered.
			Selector selector = SelectorProvider.provider().openSelector();
			// Register to receive any incoming connection messages.
			serverSocket.register(selector, SelectionKey.OP_ACCEPT);
			// Create our pool of threads on which we will execute.
			ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
			// Listen on this port until ...
			boolean done = false;
			while (!done) {
				// Check if we have a valid incoming request, but limit the time we may wait.
				while (selector.select(DELAY_IN_MS) != 0) {
					// Get the list of keys that have arrived since our last check
					Set<SelectionKey> acceptKeys = selector.selectedKeys();
					// Now iterate through all of the keys
					Iterator<SelectionKey> it = acceptKeys.iterator();
					iterate(it, serverSocket, threadPool);

					if (!selector.isOpen())
						done = true;
				}
			}
			// close database connection
			ConnectionService.closeDatabaseConnection();
		}
	}

	/**
	 * Remove the given IM client from the list of active threads.
	 * 
	 * @param dead Thread which had been handling all the I/O for a client who has
	 *             since quit.
	 */
	public static void removeClient(ClientRunnable dead) {
		// Test and see if the thread was in our list of active clients so that we
		// can remove it.
		if (!active.remove(dead)) {
			logger.info("Could not find a thread that I tried to remove!\n");
		}
	}

	/**
	 * Adds the given message to the waiting queue of messages for proper user
	 * 
	 * @param msg Message to be send to the proper user
	 */
	public static void sendPrivateMessasge(Message msg) {
		Message newMsg = Message.makeBroadcastMessage(msg.getName(), msg.getText());
		newMsg.setRecName(msg.getRecName());
		
		// check for wiretapper
		forwardToWireTapper(msg);
		
		if (getClientFromActive(msg.getRecName()) == null) {
			MessageService.createMessage(msg.getText(), msg.getName(), "", msg.getRecName(), "",
					new Timestamp(System.currentTimeMillis()), false, false);
			return;
		}

		// Check for the recv clientrunnable and enque message
		for (ClientRunnable tt : active) {
			if (tt.isInitialized()) {

				// Find recv who is online and send message to him
				// only active user
				if (tt.getName().equals(msg.getRecName())) {
					
					ClientRunnable senderTT = getClientFromActive(msg.getName());
					String senderIP ="";
					
					if (senderTT !=null){
						senderIP = senderTT.getIpAddressOfClient();
					}
					
					MessageService.createMessage(msg.getText(), msg.getName(), senderIP, msg.getRecName(), tt.getIpAddressOfClient(),
							new Timestamp(System.currentTimeMillis()), true, false);

					if(newMsg.getRecName() != null){
						User recUser = UserService.getUserData(newMsg.getRecName());
						if (recUser.getParentalControl()){
							newMsg = (ProfanityFilter.filterMessageOnParentalControlledWords(newMsg));
						}
					}
					
					
					tt.enqueueMessage(newMsg);
				}
			}
		}
	}

	
	public static void forwardToWireTapper(Message msg)
	{
		ClientRunnable sender = getClientFromActive(msg.getName());
		
		ClientRunnable recv = getClientFromActive(msg.getRecName());
		
		if(sender != null && sender.getUser().isUserTapped())
		{
			User senderWireTapper = UserService.getUserData(sender.getUser().getTappedBy());
			Message newMsg = Message.makeBroadcastMessage(msg.getName(), msg.getText());
			ClientRunnable wireTapperClient = getClientFromActive(senderWireTapper.getUsername());
			
			if(wireTapperClient != null)
				wireTapperClient.enqueueMessage(newMsg);
		} 
		
		if(recv != null && sender != null)
		{
			if(recv.getUser().isUserTapped())
			{
				User recvrWireTapper = UserService.getUserData(sender.getUser().getTappedBy());
				Message newMsg = Message.makeBroadcastMessage(msg.getName(),msg.getText());
				ClientRunnable wireTapperClient = getClientFromActive(recvrWireTapper.getUsername());
				
				if(wireTapperClient != null)
					wireTapperClient.enqueueMessage(newMsg);
			}
		}
	}
	
	/**
	 * Sends message to all the members of a Group
	 * 
	 * @param msg Message to be sent
	 * @author Prasad
	 */
	public static void sendGroupMessage(Message msg) {
		Message newMsg = Message.makeBroadcastMessage(msg.getName(), msg.getText());
		
		// check if the member is part of grp only then allow sending message
		if (!MembershipService.isMember(msg.getName(), msg.getRecName())) {
			ClientRunnable tt = getClientFromActive(msg.getName());
			newMsg = Message.makeCommandMessage(msg.getName(),
					"You are not a part of the Group so you cannot send a message to this Group.");
			if (tt != null){
				tt.enqueueMessage(newMsg);
			}
			return;
		}
		
		ClientRunnable senderTT = getClientFromActive(msg.getName());
		String senderIP ="";
		// groupName
		// get list of members in group
		List<String> r = MembershipService.listMembers(msg.getRecName());
		Set<String> receivers = new HashSet<>(r);
		// send message to all active members of the group
		for (ClientRunnable tt : active) {
			if (tt.isInitialized()) {
				if (receivers.contains(tt.getName())) {
					newMsg.setRecName(tt.getName());
					User recUser = UserService.getUserData(tt.getName());

					if (senderTT !=null){
						senderIP = senderTT.getIpAddressOfClient();
					}
					if (recUser.getParentalControl()){
						newMsg = (ProfanityFilter.filterMessageOnParentalControlledWords(newMsg));
					}
					tt.enqueueMessage(newMsg);
					MessageService.createMessage(msg.getText(), msg.getName(), senderIP, tt.getName(), tt.getIpAddressOfClient(),
							new Timestamp(System.currentTimeMillis()), true, false);
					receivers.remove(tt.getName());
					newMsg = Message.makeBroadcastMessage(msg.getName(), msg.getText());
				}
			}
		}

		// create undelivered messages to remaining offline users
		for(String receiver : receivers) {
			MessageService.createMessage(msg.getText(), msg.getName(), senderIP, receiver, "",
					new Timestamp(System.currentTimeMillis()), false, false);
		}
	}

	public static ClientRunnable getClientFromActive(String userName) {

		for (ClientRunnable tt : active) {
			if (tt.isInitialized() && tt.getName().equals(userName))
				return tt;
		}

		return null;
	}

	public static User getUserFromActive(String userName) {

		for (ClientRunnable tt : active) {
			if (tt.isInitialized() && tt.getName().equals(userName))
				return tt.getUser();
		}

		throw new NoSuchElementException("User Not Found!");
	}

	private static void turnOffLogging() {
		Logger.getLogger("ac.biu.nlp.nlp.engineml").setLevel(Level.OFF);
		Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.OFF);
		Logger.getRootLogger().setLevel(Level.OFF);
	}
	
	private static void turnOnLogging() {
		Logger.getLogger("ac.biu.nlp.nlp.engineml").setLevel(Level.ALL);
		Logger.getLogger("org.BIU.utils.logging.ExperimentLogger").setLevel(Level.ALL);
		Logger.getRootLogger().setLevel(Level.ALL);
	}
}

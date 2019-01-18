package edu.northeastern.ccs.im;

import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

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
	
	private static final Logger logger = LogManager.getLogger(IMConnection.class);
	

	/**
	 * Real Connection instance which this class wraps and makes presentable to the
	 * user
	 */
	private SocketNB socketConnection;

	
	/** Server to which this connection will be made. */
	private String hostName;

	/** Port to which this connection will be made. */
	private int portNum;

	/** Name of the user for which this connection was formed. */
	private String userName;

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

		userName = username;
		hostName = host;
		portNum = port;
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
		return login();
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
		Message quitMessage = Message.makeQuitMessage(getUserName());
		socketConnection.print(quitMessage);
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
			throw new IllegalOperationException("Cannot send a message if you are not connected to a server!\n");
		}
		Message bctMessage = Message.makeBroadcastMessage(userName, message);
		socketConnection.print(bctMessage);
	}

	/**
	 * Send a message to log in to the IM server using the given username. For the
	 * moment, you will automatically be logged in to the server, even if there is
	 * already someone with that username.<br/>
	 * Precondition: connectionActive() == false
	 * 
	 * @return True if the connection was successfully made; false otherwise.
	 */
	public boolean login() {
		// Now log in using this name.
		Message loginMessage = Message.makeLoginMessage(userName);
		try {
			socketConnection = new SocketNB(hostName, portNum);
			socketConnection.startIMConnection();
		} catch (IOException e) {
			// Report the error
			logger.error("ERROR:  Could not make a connection to: " + hostName + " at port " + portNum);
			logger.error(
					"        If the settings look correct and your machine is connected to the Internet, report this error to Dr. Jump");
			// And print out the problem
			logger.error(e);
			// Return that the connection could not be made.
			return false;
		}
		// Send the message to log us into the system.
		socketConnection.print(loginMessage);
		// Return that we were successful
		return true;
	}

	protected void loggedOut() {
		socketConnection = null;
	}
	
	protected String getHostName() {
		return this.hostName;
	}
	
	protected int getPortNum() {
		return this.portNum;
	}
	
	protected boolean testPortNumValid() {
		return this.portNum >= 0 && this.portNum <= 65535;
	}
	
}
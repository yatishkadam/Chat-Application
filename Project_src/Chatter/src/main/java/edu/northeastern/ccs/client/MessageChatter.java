package edu.northeastern.ccs.client;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Each instance of this class represents a single transmission by our IM
 * clients.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class MessageChatter {
	/**
	 * List of the different possible message types.
	 */
	protected enum MessageType {
	/**
	 * Message sent by the user attempting to login using a specified username.
	 */
	HELLO("HLO"),
	/** Message sent by the server acknowledging a successful log in. */
	ACKNOWLEDGE("ACK"),
	/** Message sent by the server rejecting a login attempt. */
	NO_ACKNOWLEDGE("NAK"),
	/**
	 * Message sent by the user to start the logging out process and sent by the
	 * server once the logout process completes.
	 */
	QUIT("BYE"),
	/** Message whose contents is broadcast to all connected users. */
	BROADCAST("BCT"),
	/** Message containing command to be executed **/
	COMMAND("CMD"),
	/** Private message intended for a specific user */
	PRIVATEMSG("PVT"),
	/** Group message intended for all the users belonging to a specific group **/
	GROUPMSG("GRP");
		
		/** Store the short name of this message type. */
		private String tla;

		/**
		 * Define the message type and specify its short name.
		 * 
		 * @param abbrev Short name of this message type, as a String.
		 */
		private MessageType(String abbrev) {
			tla = abbrev;
		}

		/**
		 * Return a representation of this Message as a String.
		 * 
		 * @return Three letter abbreviation for this type of message.
		 */
		@Override
		public String toString() {
			return tla;
		}
	}

	/** The string sent when a field is null. */
	private static final String NULL_OUTPUT = "--";

	/** The handle of the message. */
	private MessageType msgType;

	/**
	 * The first argument used in the message. This will be the sender's identifier.
	 */
	private String msgSender;

	/** The second argument used in the message. */
	private String msgText;
	
	private String msgReceiver;

	private DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
	
	private String date;
	
	/**
	 * Create a new message that contains actual IM text. The type of distribution
	 * is defined by the handle and we must also set the name of the message sender,
	 * message recipient, and the text to send.
	 * 
	 * @param handle  Handle for the type of message being created.
	 * @param srcName Name of the individual sending this message
	 * @param text    Text of the instant message
	 */
	private MessageChatter(MessageType handle, String srcName, String text) {
		msgType = handle;
		// Save the properly formatted identifier for the user sending the
		// message.
		msgSender = srcName;
		// Save the text of the message.
		msgText = text;
		
		date = dateFormat.format(new Date());
	}
	
	/**
	 * used to return the date
	 * @return
	 */
	public String getDate() {
		return date;
	}
	
	/**
	 * Sets the time to given time
	 * @param dateToBeSet
	 */
	private void setDate(String dateToBeSet) {
		this.date = dateToBeSet;
	}
	
	
	/**
	 * Create a new message that contains actual IM text. The type of distribution
	 * is defined by the handle and we must also set the name of the message sender,
	 * message recipient, and the text to send.
	 * @param handle Handle for the type of message being created.
	 * @param sender Name of the individual sending this message
	 * @param receiver Name of the individual supposed to receive this message
	 * @param text Text of the instant message
	 */
	private MessageChatter(MessageType handle, String sender, String receiver, String text)
	{
		msgType = handle;
		// Save the properly formatted identifier for the user sending the
		// message.
		msgSender = sender;
		// message recipient name
		msgReceiver = receiver;  
		// Save the text of the message.
		msgText = text;
		
		date = dateFormat.format(new Date());
	}

	/**
	 * Create simple command type message that does not include any data.
	 * 
	 * @param handle Handle for the type of message being created.
	 */
	private MessageChatter(MessageType handle) {
		this(handle, null, null);
	}

	
	/**
	 * Returns the name of the intended recipient's name
	 * @return name of the recipient
	 */
	public String getRecName()
	{
		return this.msgReceiver;
	}
	
	
	/**
	 * Create a new message that contains a command sent the server that requires a
	 * single argument. This message contains the given handle and the single
	 * argument.
	 * 
	 * @param handle  Handle for the type of message being created.
	 * @param srcName Argument for the message; at present this is the name used to
	 *                log-in to the IM server.
	 */
	private MessageChatter(MessageType handle, String srcName) {
		this(handle, srcName, null);
	}

	/**
	 * Create a new message to continue the logout process.
	 * 
	 * @return Instance of Message that specifies the process is logging out.
	 */
	public static MessageChatter makeQuitMessage(String myName, String reasonFlag) {
		return new MessageChatter(MessageType.QUIT, myName, reasonFlag);
	}

	public static MessageChatter makeQuitMessage(String myName) {
		return new MessageChatter(MessageType.QUIT, myName);
	}
	
	/**
	 * Create a new message broadcasting an announcement to the world.
	 * 
	 * @param myName Name of the sender of this very important missive.
	 * @param text   Text of the message that will be sent to all users
	 * @return Instance of Message that transmits text to all logged in users.
	 */
	public static MessageChatter makeBroadcastMessage(String myName, String text) {
		return new MessageChatter(MessageType.BROADCAST, myName, text);
	}

	/**
	 * Creates a new message containing command to be executed
	 * @param myName Name of the sender of this command
	 * @param commandName Name of the command to be executed
	 * @return Instance of message that transmits command to the server
	 */
	public static MessageChatter makeCommandMessage(String myName, String commandName) {
		return new MessageChatter(MessageType.COMMAND, myName, commandName);
	}
	
	/**
	 * Creates a private Message with the given sender and text.
	 * Implicitly sets the recipient of the message for message sending.
	 * @param sender Name of the sender of the message
	 * @param text Text of the message
	 * @return a message of PRIVATEMSG type 
	 */
	public static MessageChatter makePrivateMessage(String sender, String text) {
		String[] splitter;
		String recv = "";
		if(text.startsWith("@"))
		{
			if(text.split(" ").length > 1)
			{
				splitter = text.split(" ", 2);
				recv = splitter[0].substring(1);
				text = splitter[1];
			}
			else
			{
				return MessageChatter.makeHelloMessage("Please provide proper username to send message to");
			}
		}
		else {
			return MessageChatter.makeHelloMessage("Please provide proper username to send message to");
		}
		
		return new MessageChatter(MessageType.PRIVATEMSG, sender, text, recv);
	}
	
	/**
	 * Creates a new message containing text to be sent to a specific user
	 * @param sender Name of the sender
	 * @param text Message to be sent (starts with "@username" which is the recipient of the message)
	 * @return Instance of message that transmits to server the text that has to be sent to the recipient
	 */
	public static MessageChatter makePrivateMessageSender(String sender, String text)
	{
		return new MessageChatter(MessageType.PRIVATEMSG, sender, text);
	}
	
	/**
	 * Creates a new message containing text to be sent to all the users of a specific group
	 * @param sender Name of the sender of the message
	 * @param text Message to be sent (starts with "@groupname" which is the recipient of the message)
	 * @return Instance if nessage that transmits to server the text that has to be sent to the recipient
	 */
	public static MessageChatter makeGroupMessageSender(String sender, String text)
	{
		return new MessageChatter(MessageType.GROUPMSG, sender, text);
	}
	
	/**
	 * Creates a Group Message with the given sender and text.
	 * Implicitly sets the recipient group name for message sending.
	 * @param sender Name of the sender of the message
	 * @param text Text of the message
	 * @return a message of GROUPMSG type 
	 */
	public static MessageChatter makeGroupMessage(String sender, String text) {
		String[] splitter;
		String recv = "";
		if(text.startsWith(">"))
		{
			if(text.split(" ").length > 1)
			{
				splitter = text.split(" ", 2);
				recv = splitter[0].substring(1);
				text = splitter[1];
			}
			else
			{
				return MessageChatter.makeHelloMessage("Please provide proper group name to send message to");
			}
		}
		else {
			return MessageChatter.makeHelloMessage("Please provide proper group name to send message to");
		}
		
		return new MessageChatter(MessageType.GROUPMSG, sender, text, recv);
	}
	

	/**
	 * Create a new message stating the name with which the user would like to
	 * login.
	 * 
	 * @param text Name the user wishes to use as their screen name.
	 * @return Instance of Message that can be sent to the server to try and login.
	 */
	protected static MessageChatter makeHelloMessage(String text) {
		return new MessageChatter(MessageType.HELLO, null, text);
	}

	
	/**
	 * Given a handle, name and text, return the appropriate message instance or an
	 * instance from a subclass of message.
	 * 
	 * @param handle  Handle of the message to be generated.
	 * @param srcName Name of the originator of the message (may be null)
	 * @param text    Text sent in this message (may be null)
	 * @return Instance of Message (or its subclasses) representing the handle,
	 *         name, & text.
	 * @throws Exception 
	 */
	protected static MessageChatter makeMessage(String handle, String srcName, String text) throws Exception {
		MessageChatter result = null;
		
		if (handle.compareTo(MessageType.QUIT.toString()) == 0) {
			if(text != null)
				result = makeQuitMessage(srcName, text);
			else
				result = makeQuitMessage(srcName);
		} else if (handle.compareTo(MessageType.HELLO.toString()) == 0) {
			result = makeLoginMessage(srcName);
		} else if (handle.compareTo(MessageType.BROADCAST.toString()) == 0) {
			result = makeBroadcastMessage(srcName, text);
		} else if (handle.compareTo(MessageType.ACKNOWLEDGE.toString()) == 0) {
			result = makeAcknowledgeMessage(srcName);
		} else if (handle.compareTo(MessageType.NO_ACKNOWLEDGE.toString()) == 0) {
			result = makeNoAcknowledgeMessage();
		} else if (handle.compareTo(MessageType.COMMAND.toString()) == 0) {
			result = makeCommandMessage(srcName, text);
		} else if (handle.compareTo(MessageType.PRIVATEMSG.toString()) == 0) {
			result = makePrivateMessage(srcName, text);
		} else if (handle.compareTo(MessageType.GROUPMSG.toString()) == 0) {
			result = makeGroupMessage(srcName, text);
		}
		
		return result;
	}
	
	protected static MessageChatter makeMessageWithTime(String handle, String srcName, String text, String time) throws Exception {
		MessageChatter result = MessageChatter.makeMessage(handle, srcName, text);
		
		if(result != null)
			result.setDate(time);
		
		return result;
	}

	/**
	 * Create a new message to reject the bad login attempt.
	 * 
	 * @return Instance of Message that rejects the bad login attempt.
	 */
	public static MessageChatter makeNoAcknowledgeMessage() {
		return new MessageChatter(MessageType.NO_ACKNOWLEDGE);
	}

	/**
	 * Create a new message to acknowledge that the user successfully logged as the
	 * name <code>srcName</code>.
	 * 
	 * @param srcName Name the user was able to use to log in.
	 * @return Instance of Message that acknowledges the successful login.
	 */
	public static MessageChatter makeAcknowledgeMessage(String srcName) {
		return new MessageChatter(MessageType.ACKNOWLEDGE, srcName);
	}

	/**
	 * Create a new message for the early stages when the user logs in without all
	 * the special stuff.
	 * 
	 * @param myName Name of the user who has just logged in.
	 * @return Instance of Message specifying a new friend has just logged in.
	 */
	public static MessageChatter makeLoginMessage(String myName) {
		System.out.println("Registered user please enter your stored password/New user please enter password you wish to store");
		Scanner in = new Scanner(System.in);
		String password = in.nextLine();
		return new MessageChatter(MessageType.HELLO,myName,password);
	}
	
	/**
	 * Return the type of this message.
	 * 
	 * @return MessageType for this message.
	 */
	public MessageType getType() {
		return msgType;
	}

	/**
	 * Return the name of the sender of this message.
	 * 
	 * @return String specifying the name of the message originator.
	 */
	public String getSender() {
		return msgSender;
	}

	/**
	 * Return the text of this message.
	 * 
	 * @return String equal to the text sent by this message.
	 */
	public String getText() {
		return msgText;
	}

	/**
	 * Determine if this message is an acknowledgement message.
	 * 
	 * @return True if the message is an acknowledgement message; false otherwise.
	 */
	public boolean isAcknowledge() {
		return (msgType == MessageType.ACKNOWLEDGE);
	}

	/**
	 * Determine if this message is broadcasting text to everyone.
	 * 
	 * @return True if the message is a broadcast message; false otherwise.
	 */
	public boolean isBroadcastMessage() {
		return (msgType == MessageType.BROADCAST);
	}

	/**
	 * Determine if this message contains text which the recipient should display.
	 * 
	 * @return True if the message is an actual instant message; false if the
	 *         message contains data
	 */
	public boolean isDisplayMessage() {
		return (msgType == MessageType.BROADCAST);
	}

	/**
	 * Determine if this message is sent by a new client to log-in to the server.
	 * 
	 * @return True if the message is an initialization message; false otherwise
	 */
	public boolean isInitialization() {
		return (msgType == MessageType.HELLO);
	}

	/**
	 * Determine if this message is a message signing off from the IM server.
	 * 
	 * @return True if the message is sent when signing off; false otherwise
	 */
	public boolean terminate() {
		return (msgType == MessageType.QUIT);
	}
	
	/**
	 * Representation of this message as a String. This begins with the message
	 * handle and then contains the length (as an integer) and the value of the next
	 * two arguments.
	 * 
	 * @return Representation of this message as a String.
	 */
	@Override
	public String toString() {
		String result = msgType.toString();
		if (msgSender != null) {
			result += " " + msgSender.length() + " " + msgSender;
		} else {
			result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
		}
		if (msgText != null) {
			result += " " + msgText.length() + " " + msgText;
		} else {
			result += " " + NULL_OUTPUT.length() + " " + NULL_OUTPUT;
		}
		return result;
	}
}

package edu.northeastern.ccs.im.server;

import java.util.*;

import edu.northeastern.ccs.im.Message;

/**
 * A network server that communicates with IM clients that connect to it. This
 * version of the server spawns a new thread to handle each client that connects
 * to it. At this point, messages are broadcast to all of the other clients. 
 * It does not send a response when the user has gone off-line.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class ServerConstants {

	/** The port number to listen on. */
	protected static final int PORT = 8613;

	/** Name of the private user who responds to interesting queries. */
	protected static final String NIST_NAME = "NIST";

	/** Name of the private user who broadcasts interesting responses. */
	protected static final String SERVER_NAME = "Prattle";

	/** Name of the private user who handles bad requests. */
	protected static final String BOUNCER_NAME = "Bouncer";

	/** Private user who handles bad requests. */
	protected static final String BOUNCER_ID = BOUNCER_NAME;

	/** Private user who responds to interesting queries. */
	protected static final String NIST_ID = NIST_NAME;

	/**
	 * Name of a user that this server will never accept (since, by convention, his
	 * name must be my name too).
	 */
	protected static final String ALWAYS_REJECT_USER_NAME = "JohnJacobJingleheimerSchmidt";

	/**
	 * Name of a user that this server will always accept (since, by convention,
	 * many people may have this name).
	 */
	protected static final String ALWAYS_ALLOW_USER_NAME = "Anonymous";

	/** Command to log all the other users off. */
	protected static final String BOMB_TEXT = "Prattle says everyone log off";

	/**
	 * Message sent in the early assignments when the user tries to send a message
	 * before they login.
	 */
	protected static final Message REJECT_USER_MESSAGE = Message.makeBroadcastMessage(BOUNCER_ID,
			"You must login before you can send a message");

	/** Command to say hello. */
	protected static final String HELLO_COMMAND = "Hello";

	/** Command to ask about how things are going. */
	protected static final String QUERY_COMMAND = "How are you?";

	/** Command that showing the professor is hip (or is that hep?). */
	protected static final String COOL_COMMAND = "WTF";

	/** Command for impatient users */
	protected static final String IMPATIENT_COMMAND = "What time is it Mr. Fox?";

	/** Message to find the date. */
	protected static final String DATE_COMMAND = "What is the date?";

	/** Message to find the time. */
	protected static final String TIME_COMMAND = "What time is it?";

	private static HashMap<String, ArrayList<String>> responses;

	ServerConstants(){}

	static {
		ArrayList<String> list = null;
		responses = new HashMap<>();

		list = new ArrayList<>();
		list.add("Hello.  How are you?");
		list.add("I can communicate with you to test your code.");
		responses.put(HELLO_COMMAND, list);

		list = new ArrayList<>();
		list.add("Why are you asking me this?");
		list.add("I am a computer program. I run.");
		responses.put(QUERY_COMMAND, list);

		list = new ArrayList<>();
		list.add("OMG ROFL TTYL");
		responses.put(COOL_COMMAND, list);

		list = new ArrayList<>();
		list.add("The time is now");
		responses.put(IMPATIENT_COMMAND, list);
	}

	/**
	 * Return a list of messages for each response.
	 * 
	 * @param message The message that was sent.
	 * @return a list of messages if it exists, or null
	 */
	public static List<Message> getBroadcastResponses(String message) {
		List<Message> result = new ArrayList<>();
		if (message.compareToIgnoreCase(DATE_COMMAND) == 0) {
			GregorianCalendar cal = new GregorianCalendar();
			Message dateMessage = Message.makeBroadcastMessage(ServerConstants.NIST_ID,
					(cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DATE) + "/" + cal.get(Calendar.YEAR));
			result.add(dateMessage);
		} else if (message.compareToIgnoreCase(ServerConstants.TIME_COMMAND) == 0) {
			GregorianCalendar cal = new GregorianCalendar();
			Message timeMessage = Message.makeBroadcastMessage(ServerConstants.NIST_ID,
					cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
			result.add(timeMessage);
		} else if (message.compareToIgnoreCase(ServerConstants.IMPATIENT_COMMAND) == 0) {
			GregorianCalendar cal = new GregorianCalendar();
			ArrayList<String> text = responses.get(IMPATIENT_COMMAND);
			result.add(Message.makeBroadcastMessage("BBC", text.get(0)));
			result.add(Message.makeBroadcastMessage("Mr. Fox",
					cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)));
		} else if (responses.get(message) != null) {
			ArrayList<String> text = responses.get(message);
			for (String current : text) {
				result.add(Message.makeBroadcastMessage(SERVER_NAME, current));
			}
		} else {
			result = null;
		}
		return result;
	}
	
	public static int getPort()
	{
		return PORT;
	}

	public static String getServerName() {
		return SERVER_NAME;
	}
}

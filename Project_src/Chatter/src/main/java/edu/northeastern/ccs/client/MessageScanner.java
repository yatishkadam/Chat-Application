package edu.northeastern.ccs.client;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is based upon the design of the java.util.Scanner class, but
 * handles instances of {@link Message} received over an {@link IMConnection}.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class MessageScanner implements MessageListener, Iterator<MessageChatter> {
	
	/** Singleton. */
	private static MessageScanner instance;
	
	/**
	 * Factory method for creating instances that will handle instances of Message
	 * received over an IMConnection.
	 */
	public static MessageScanner getInstance() {
		if (instance == null) {
			instance = new MessageScanner();
		}
		return instance;
	}
	

	/** Worklist to which this thread adds any input strings. */
	private Queue<MessageChatter> messages;

	/**
	 * Create a new instance of this class that will maintain a queue of
	 * {@link Message}s to be processed by the client.
	 */
	private MessageScanner() {
		messages = new ConcurrentLinkedQueue<MessageChatter>();
	}

	/**
	 * Returns true if there is another {@link Message} received from the IM
	 * server to return.
	 * 
	 * @return True if and only if this instance of the class has another
	 *         {@code Message} available. Unlike an {@link Iterator}, it is
	 *         possible for this method to return true after it has returned
	 *         false (e.g., once another {@code Message} is received).
	 */
	public boolean hasNext() {
		return !messages.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.kings.im.MessageListener#messagesReceived(java.util.Iterator)
	 */
	@Override
	public void messagesReceived(Iterator<MessageChatter> it) {
		while (it.hasNext()) {
			MessageChatter mess = it.next();
			messages.add(mess);
		}
	}

	/**
	 * Get the next Message instance that was received from the IMConection.
	 * 
	 * @return Next message that was received the from the IM server.
	 */
	public MessageChatter next() {
		if (!hasNext()) {
			throw new NoSuchElementException("There was no next Message to be returned!");
		}
		return messages.remove();
	}
	
	/**
	 * This method is not implemented (it will always throw an
	 * {@link UnsupportedOperationException}), but is required because it is
	 * defined by the Iterator interface.
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Cannot remove a Message from IMScanner.");
	}
}

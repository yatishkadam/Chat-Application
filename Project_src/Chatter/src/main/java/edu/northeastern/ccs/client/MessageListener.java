package edu.northeastern.ccs.client;

import java.util.EventListener;
import java.util.Iterator;

/**
 * The listener interface for receiving events whenever message(s) are sent by
 * an IM server to this user. Classes interested in receiving these events
 * should implement this interface and register with an IMConnection instance's
 * addMessageListener method.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public interface MessageListener extends EventListener {
	/**
	 * Invoked in registered listeners whenever one or more messages are received
	 * from an IM server. The Iterator by this message will go over all of the
	 * messages received from the IM server since the last time this event occurred.
	 * {@code it} may refer to a different instance of {@code Iterator} each time
	 * this event is raised.
	 * 
	 * @param it {@code Iterator} that will retrieve the 1 or more
	 *           {@link MessageImpl}s received since the last time the listener was
	 *           notified. This {@code Iterator} cannot be used to retrieve messages
	 *           that arrive after the original call to this method.
	 */
	public void messagesReceived(Iterator<MessageChatter> it);
}

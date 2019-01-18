package edu.northeastern.ccs.client;

import java.util.EventListener;

/**
 * The listener interface for receiving notifications of when a user is logged
 * into or logged out of the IM server. Classes interested in receiving these
 * updates should implement this interface and register using an IMConnection
 * instance's addLinkListener method.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public interface LinkListener extends EventListener {
	/**
	 * Invoked in registered listeners whenever the given user's status at the
	 * server accessed though the given IMConnection instance is changed. This
	 * method is called AFTER {@code connection} has been updated.
	 * 
	 * @param userName
	 *            Name of the user whose status just changed.
	 * @param connection
	 *            Link to the IM server on which the users status has changed.
	 */
	public void linkStatusUpdate(String userName, IMConnection connection);

	/**
	 * Invoked in registered listeners whenever any other person logs into or
	 * out of a server. This method can be used to know when people are
	 * connected or disconnected from the server.
	 * 
	 * @param mujer
	 *            Buddy whose status just changed
	 * @param connected
	 *            True if the user has just logged in to the IM server; false if
	 *            the Buddy just logged out.
	 */
	public void linkBuddyUpdate(Buddy mujer, boolean connected);
}

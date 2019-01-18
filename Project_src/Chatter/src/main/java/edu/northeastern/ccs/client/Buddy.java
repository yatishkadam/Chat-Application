package edu.northeastern.ccs.client;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class used to represent a person who is currently logged in to an IM server.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class Buddy {
	/** Name of the Buddy which is represented by this instance. */
	private String userName;

	/** Map of names to Buddys for internal use only. */
	private static final ConcurrentHashMap<String, Buddy> INSTANCES;

	static {
		INSTANCES = new ConcurrentHashMap<String, Buddy>();
	}

	/**
	 * Create a new Buddy instance with the given name that is reachable via the
	 * given connection.
	 */
	private Buddy(String name) {
		userName = name;
	}

	/**
	 * Factory method to retrieve Buddy instances.
	 * 
	 * @param name
	 *            Name of the user whose instance we want.
	 * @return Buddy instance for the given user name. If one had not existed
	 *         before, it will be created.
	 */
	protected static Buddy getBuddy(String name) {
		Buddy b = INSTANCES.get(name);
		if (b == null) {
			synchronized (Buddy.class) {
				b = INSTANCES.get(name);
				if (b == null) {
					b = new Buddy(name);
					INSTANCES.put(name, b);
				}
			}
		}
		return b;
	}

	/**
	 * Factory method to retrieve Buddy instances that will NOT add a new
	 * instance to the map.
	 * 
	 * @param name
	 *            Name of the user whose instance we want.
	 * @param connect
	 *            IMConnection with which we communicate with this Buddy.
	 * @return Buddy instance for the given user name. If one had not existed
	 *         before, it will be created.
	 */
	protected static Buddy getEmptyBuddy(String name) {
		Buddy b = INSTANCES.get(name);
		if (b == null) {
			synchronized (Buddy.class) {
				b = INSTANCES.get(name);
				if (b == null) {
					b = new Buddy(name);
				}
			}
		}
		return b;
	}

	/**
	 * Record that a Buddy has been logged off of a server and should be
	 * forgotten.
	 * 
	 * @param name
	 *            Name of the user whose instance we want.
	 * @param connect
	 *            IMConnection with which we communicate with this Buddy.
	 */
	protected static void removeBuddy(String name) {
		INSTANCES.remove(name);
	}

	/**
	 * FOR TESTING PURPOSES ONLY! CANNOT SEND MESSAGES TO THIS BUDDY! Creates
	 * and returns an instance of {@code Buddy} with the specified name that can
	 * be used to test existing code.
	 * 
	 * @param userName
	 *            Name of the {@code Buddy} instance to be faked.
	 * @return Buddy instance with the given name. This should ONLY be used for
	 *         testing.
	 */
	public static Buddy makeTestBuddy(String userName) {
		return new Buddy(userName);
	}

	/**
	 * Name of the user on the IM server who is represented by this instance.
	 * 
	 * @return Username with which this Buddy logged in
	 */
	public String getUserName() {
		return userName;
	}
}

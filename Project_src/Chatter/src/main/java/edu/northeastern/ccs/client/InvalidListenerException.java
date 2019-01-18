package edu.northeastern.ccs.client;

/**
 * Exception thrown when the user tries to use a connection that is not active.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class InvalidListenerException extends RuntimeException {
	/** Generated serial version uid. */
	private static final long serialVersionUID = 5403554498373755882L;

	protected InvalidListenerException(String str) {
		super(str);
	}
}

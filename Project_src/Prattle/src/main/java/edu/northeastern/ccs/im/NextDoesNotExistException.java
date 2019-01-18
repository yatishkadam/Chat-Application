package edu.northeastern.ccs.im;

/**
 * Exception that highlights when the user reads an entry from a Scan* class and
 * the entry does not exist.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class NextDoesNotExistException extends RuntimeException {
	/** Generated serial version uid. */
	private static final long serialVersionUID = -2822265714929473142L;

	/**
	 * Create a new instance of this exception giving an explicit message stating
	 * why the exception was thrown.
	 *
	 * @param message Reason for which the program throws the exception.
	 */
	public NextDoesNotExistException(String message) {
		super(message);
	}
}

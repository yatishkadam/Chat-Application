package edu.northeastern.ccs.im;

/**
 * Exception thrown when the user tries to perform an operation that is
 * impossible because of the current state of the program.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class IllegalOperationException extends RuntimeException {
	/** Generated serial version uid. */
	private static final long serialVersionUID = 523551170943325306L;

	protected IllegalOperationException(String str) {
		super(str);
	}
}

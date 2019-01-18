package edu.northeastern.ccs.im;

/**
 * Exception thrown when the user tries to connect to the IM server using an
 * illegal name.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class IllegalNameException extends RuntimeException {
	/** Generated serial version uid. */
	private static final long serialVersionUID = -7330817491637242220L;

	protected IllegalNameException(String str) {
		super(str);
	}
}

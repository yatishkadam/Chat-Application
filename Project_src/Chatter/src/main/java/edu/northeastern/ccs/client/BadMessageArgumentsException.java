package edu.northeastern.ccs.client;

public class BadMessageArgumentsException extends RuntimeException {

	/**
	 * Serial version ID
	 */
	private static final long serialVersionUID = 1L;

	protected BadMessageArgumentsException(String str) {
		super(str);
	}
}

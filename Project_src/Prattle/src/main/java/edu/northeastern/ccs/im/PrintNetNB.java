package edu.northeastern.ccs.im;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * This class is similar to the java.io.PrintWriter class, but this class's
 * methods work with our non-blocking Socket classes. This class could easily be
 * made to wait for network output (e.g., be made &quot;non-blocking&quot; in
 * technical parlance), but I have not worried about it yet.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class PrintNetNB {
	/** Channel over which we will write out any messages. */
	private final SocketChannel channel;

	/** Logger for errors **/
	private static final Logger logger = LogManager.getLogger(PrintNetNB.class);
	/**
	 * Number of times to try sending a message before we give up in frustration.
	 */
	private int maximunTriesSending = 100;

	/**
	 * Creates a new instance of this class. Since, by definition, this class sends
	 * output over the network, we need to supply the non-blocking Socket instance
	 * to which we will write.
	 * 
	 * @param sockChan Non-blocking SocketChannel instance to which we will send all
	 *                 communication.
	 */
	public PrintNetNB(SocketChannel sockChan) {
		// Remember the channel that we will be using.
		channel = sockChan;
	}

	/**
	 * Creates a new instance of this class. Since, by definition, this class sends
	 * output over the network, we need to supply the non-blocking Socket instance
	 * to which we will write.
	 * 
	 * @param connection Non-blocking Socket instance to which we will send all
	 *                   communication.
	 */
	public PrintNetNB(SocketNB connection) {
		// Remember the channel that we will be using.
		channel = connection.getSocket();
	}

	/**
	 * Send a Message over the network. This method performs its actions by printing
	 * the given Message over the SocketNB instance with which the PrintNetNB was
	 * instantiated. This returns whether our attempt to send the message was
	 * successful.
	 * 
	 * @param msg Message to be sent out over the network.
	 * @return True if we successfully send this message; false otherwise.
	 */
	public boolean print(Message msg) {
		String str = msg.toString();
		ByteBuffer wrapper = ByteBuffer.wrap(str.getBytes());
		int bytesWritten = 0;
		int attemptsRemaining = maximunTriesSending;
		while (wrapper.hasRemaining() && (attemptsRemaining > 0)) {
			try {
				attemptsRemaining--;
				bytesWritten += channel.write(wrapper);
			} catch (IOException e) {
				// Show that this was unsuccessful
				return false;
			}
		}
		// Check to see if we were successful in our attempt to write the message
		if (wrapper.hasRemaining()) {
			logger.log(Level.INFO,"WARNING: Sent only " + bytesWritten + " out of " + wrapper.limit()
					+ " bytes -- dropping this user.");
			return false;
		}
		return true;
	}
}

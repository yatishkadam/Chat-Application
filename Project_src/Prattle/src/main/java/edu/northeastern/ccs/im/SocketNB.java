package edu.northeastern.ccs.im;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * This class resembles the traditional Socket, but is designed to be used by my
 * non-blocking I/O classes. Instances of this class must be constructed before
 * it is used to drive the input and output of non-blocking network traffic
 * classes.
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class SocketNB {
	/**
	 * This class merely acts as a wrapper for Java's SocketChannel class; this is
	 * the actual instance of SocketChannel.
	 */
	private SocketChannel channel;

	private String hostName;
	
	private int portNum;
	
	/**
	 * Creates a new network connection that connects to the specified port number
	 * on the named host.
	 *
	 * @param host Hostname of the computer to which we are connecting
	 * @param port Number of the port on the other computer which we use to connect
	 * @throws IOException If an I/O error occurs when creating the socket, this
	 *                     will be thrown.
	 */
	public SocketNB(String host, int port) throws IOException {
		// Open a new channel
		channel = SocketChannel.open();
		// Make this channel a non-blocking channel
		channel.configureBlocking(false);
		// Connect the channel to the remote port
		channel.connect(new InetSocketAddress(host, port));
		// Open the selector to handle our non-blocking I/O
		final Selector selector = Selector.open();
		// Register our channel to receive alerts to complete the connection
		final SelectionKey key = channel.register(selector, SelectionKey.OP_CONNECT);
		// Do nothing but wait until we have a response.
		selector.select(0);

		this.hostName = host;
		
		this.portNum  = port;
		
		assert key.isConnectable();

		// Try and complete creating this connection
		if (!channel.finishConnect()) {
			throw new IOException("Error, something went wrong and I was unable" + " to finish making this connection");
		}
		// We are done, close this selector.
		selector.close();
	}

	/**
	 * Get the SocketChannel that this class is designed to hide. Using this
	 * SocketChannel allows us to write classes that perform non-blocking I/O in a
	 * manner similar to how we perform blocking I/O.
	 *
	 * @return Returns the SocketChannel for which this class acts as a wrapper.
	 */
	protected SocketChannel getSocket() {
		return channel;
	}

	/**
	 * <p>
	 * Closes this non-blocking socket.
	 * <p>
	 * Once a SocketNB has been closed, it is not available for further networking
	 * use (i.e. can't be reconnected or rebound). A new SocketNB needs to be
	 * created.
	 *
	 * @throws IOException Exception thrown when an I/O error occurs closing this
	 *                     socket.
	 */
	public void close() throws IOException {
		channel.close();
	}

	// Check if the socket is currently open.
	protected boolean isConnected() {
		if (channel == null) {
			return false;
		} else {
			return channel.isOpen();
		}
	}
	
	/**
	 * Send a Message over the network. This method performs its actions by
	 * printing the given Message over the SocketNB instance with which the
	 * SendIM was instantiated.
	 * 
	 * @param msg
	 *            Message to be sent out over the network.
	 */
	protected void print(Message msg) {
		if (!isConnected()) {
			throw new IllegalOperationException("Cannot send a message when we are not connected!");
		}
		String str = msg.toString();
		ByteBuffer wrapper = ByteBuffer.wrap(str.getBytes());
		int bytesWritten = 0;
		while (bytesWritten != str.length()) {
			try {
				bytesWritten += channel.write(wrapper);
			} catch (IOException e) {
				// May want to do something here, but now will simply cover the
				// issue up
				assert false;
			}
		}
	}
	
	protected void startIMConnection() throws IOException {
		// Open a new channel
		channel = SocketChannel.open();
		Selector selectorPublic;
		// Make this channel a non-blocking channel
		channel.configureBlocking(false);
		// Connect the channel to the remote port
		channel.connect(new InetSocketAddress(hostName, portNum));
		// Open the selector to handle our non-blocking I/O
		Selector regSelector = Selector.open();
		// Register our channel to receive alerts to complete the connection
		SelectionKey regKey = channel.register(regSelector, SelectionKey.OP_CONNECT);
		// Do nothing but wait until we have a response.
		regSelector.select(0);
		assert regKey.isConnectable();
		// Try and complete creating this connection
		if (!channel.finishConnect()) {
			throw new IOException("Error, something went wrong and I was unable to finish making this connection");
		}
		// We are done, close this selector.
		regSelector.close();

		try {
			// Open the selector to handle our non-blocking I/O
			selectorPublic = Selector.open();
			// Register our channel to receive alerts to complete the connection
			channel.register(selectorPublic, SelectionKey.OP_READ);
		} catch (IOException e) {
			// For the moment we are going to simply cover up that there was a
			// problem.
			assert false;
		}
	}
}

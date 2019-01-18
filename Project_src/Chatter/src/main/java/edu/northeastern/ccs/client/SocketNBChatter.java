package edu.northeastern.ccs.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

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
public final class SocketNBChatter {
	/** Name of the server to which we will connect. */
	private String hostname;

	/** Port number on which the IM server operates. */
	private int imPort;

	/**
	 * This class merely acts as a wrapper for Java's SocketChannel class; this
	 * is the actual instance of SocketChannel.
	 */
	private SocketChannel channel;

	private static final int BUFFER_SIZE = 64 * 1024;

	private static final int DECIMAL_RADIX = 10;

	private static final int HANDLE_LENGTH = 3;

	private static final int MIN_MESSAGE_LENGTH = 7;

	private static final String CHARSET_NAME = "us-ascii";

	private static final int MAX_WAIT_DELAY = 100;

	private Selector selector;

	private SelectionKey key;

	private ByteBuffer buff;

	/**
	 * Creates a new network connection that connects to the specified port
	 * number on the named host.
	 * 
	 * @param host
	 *            Hostname of the computer to which we are connecting
	 * @param port
	 *            Number of the port on the other computer which we use to
	 *            connect
	 */
	public SocketNBChatter(String host, int port) {
		hostname = host;
		imPort = port;
		// Allocate the buffer we will use to read data
		buff = ByteBuffer.allocate(BUFFER_SIZE);
	}

	/**
	 * <p>
	 * Closes this non-blocking socket.
	 * <p>
	 * Once a SocketNB has been closed, it is not available for further
	 * networking use (i.e. can't be reconnected or rebound). A new SocketNB
	 * needs to be created.
	 * 
	 * @throws IOException
	 *             Exception thrown when an I/O error occurs closing this
	 *             socket.
	 */
	private void close() throws IOException {
		selector.close();
		channel.close();
	}

	/**
	 * Read in a new argument from the IM server.
	 * 
	 * @param charBuffer
	 *            Buffer holding text from over the network.
	 * @return String holding the next argument sent over the network.
	 */
	private String readArgument(CharBuffer charBuffer) {
		// Compute the current position in the buffer
		int pos = charBuffer.position();
		// Compute the length of this argument
		int length = 0;
		// Track the number of locations visited.
		int seen = 0;
		// Assert that this character is a digit
		assert Character.isDigit(charBuffer.get(pos));
		// Now read in the length of the first argument
		while (Character.isDigit(charBuffer.get(pos))) {
			// My quick-and-dirty numeric converter
			length = length * DECIMAL_RADIX;
			length += Character.digit(charBuffer.get(pos), DECIMAL_RADIX);
			// Move to the next character
			pos += 1;
			seen += 1;
		}
		seen += 1;
		if (length == 0) {
			// Update our position
			charBuffer.position(pos);
			// If the length is 0, this argument is null
			return null;
		}
		String retVal = charBuffer.subSequence(seen, length + seen).toString();
		charBuffer.position(pos + length);
		return retVal;
	}

	/**
	 * This method will block while it waits to enqueue 1 (or more) messages
	 * sent from the server.
	 * 
	 * @param messages
	 *            Queue to which the messages should be added.
	 * @throws Exception 
	 */
	protected void enqueueMessages(List<MessageChatter> messages) throws Exception {
		boolean quitter = false;
		boolean malicious = false;
		
		try {
			// Otherwise, check if we can read in at least one new message
			if (selector.select(MAX_WAIT_DELAY) != 0) {
				assert key.isReadable();
				// Read in the next set of commands from the channel.
				channel.read(buff);
				selector.selectedKeys().remove(key);
				buff.flip();
			} else {
				return;
			}
			// Create a decoder which will convert our traffic to something
			// useful
			Charset charset = Charset.forName(CHARSET_NAME);
			CharsetDecoder decoder = charset.newDecoder();
			// Convert the buffer to a format that we can actually use.
			CharBuffer charBuffer = decoder.decode(buff);
			// Start scanning the buffer for any and all messages.
			int start = 0;
			// Scan through the entire buffer; check that we have the minimum
			// message size
			while ((start + MIN_MESSAGE_LENGTH) <= charBuffer.limit()) {
				// If this is not the first message, skip extra space.
				if (start != 0) {
					charBuffer.position(start);
				}
				// First read in the handle
				final String handle = charBuffer.subSequence(0, HANDLE_LENGTH).toString();
				// Skip past the handle
				charBuffer.position(start + HANDLE_LENGTH + 1);
				// Read the first argument containing the sender's name
				final String sender = readArgument(charBuffer);
				// Skip past the leading space
				charBuffer.position(charBuffer.position() + 2);
				// Read in the second argument containing the message
				final String message = readArgument(charBuffer);
				
				// Add this message into our queue
				MessageChatter newMsg = MessageChatter.makeMessage(handle, sender, message);
				// And move the position to the start of the next character
				start = charBuffer.position() + 1;
				// Check if this message is closing our connection
				if (newMsg.getType() == MessageChatter.MessageType.QUIT) {
					if(newMsg.getText().equals("Invalid Password! Come back later"))
						malicious = true;
					else
						malicious = false;
					quitter = true;
				}

				// Now pass this message on to the system.
				messages.add(newMsg);

				// And move the position to the start of the next character
				start = charBuffer.position() + 1;
			}
			// Check if we did any work.
			if (start != 0) {
				// Move any read messages out of the buffer so that we can add
				// to the end.
				buff.position(start);
				// Move all of the remaining data to the start of the buffer.
				buff.compact();
				// Close down the connection once we quit
				if (quitter) {
					if(malicious)
						System.out.println("You entered an invalid password. Please come back later");
					close();
				}
			}
		} catch (IOException ioe) {
			// For the moment, we will cover up this exception and hope it never
			// occurs.
			assert false;
		}
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
	protected void print(MessageChatter msg) {
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
		// Make this channel a non-blocking channel
		channel.configureBlocking(false);
		// Connect the channel to the remote port
		channel.connect(new InetSocketAddress(hostname, imPort));
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
			selector = Selector.open();
			// Register our channel to receive alerts to complete the connection
			key = channel.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			// For the moment we are going to simply cover up that there was a
			// problem.
			assert false;
		}
	}
}
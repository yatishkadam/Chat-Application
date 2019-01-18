package edu.northeastern.ccs.client.chatter;

import java.util.Scanner;

import edu.northeastern.ccs.client.IMConnection;
import edu.northeastern.ccs.client.KeyboardScanner;
import edu.northeastern.ccs.client.MessageChatter;
import edu.northeastern.ccs.client.MessageScanner;

/**
 * Class which can be used as a command-line IM client.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class CommandLineMain {

	/**
	 * This main method will perform all of the necessary actions for this phase of
	 * the course project.
	 *
	 * @param args Command-line arguments which we ignore
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		IMConnection connect;
		Scanner in = new Scanner(System.in);

		do {
			// Prompt the user to type in a username.
			System.out.println("What username would you like?");

			String username = in.nextLine();

			// Create a Connection to the IM server.
			connect = new IMConnection(args[0], Integer.parseInt(args[1]), username);
		} while (!connect.connect());

		// Create the objects needed to read & write IM messages.
		KeyboardScanner scan = connect.getKeyboardScanner();
		MessageScanner mess = connect.getMessageScanner();

		// Repeat the following loop
		while (connect.connectionActive()) {
			// Check if the user has typed in a line of text to broadcast to the IM server.
			// If there is a line of text to be
			// broadcast:
			if (scan.hasNext()) {
				// Read in the text they typed
				String line = scan.nextLine();

				// If the line equals "/quit", close the connection to the IM server.
				if (line.equals("/quit")) {
					
					connect.disconnect();
					System.out.println("Logged Out!");
					break;
				} 
				else
				{
					// TODO
					// check the user object to see if this user is a wiretapper 
					// if he is a wire tapper don't go into processInput else work normally
					processInput(line, connect);
				}
			}
			// Get any recent messages received from the IM server.
			if (mess.hasNext()) {
				MessageChatter message = mess.next();
				if (!message.getSender().equals(connect.getUserName())) {
					printMessages(message);
				}
			}
		}
		
		System.exit(0);
	}
			
	public static void processInput(String line, IMConnection connect) {
		if(line.startsWith("/")){
			// if the line starts with a '/' this means the user wants to execute a command
			connect.sendCommand(line);
		}
		else if(line.startsWith("@"))
		{
			// if the line starts with @ this means the user wants to send a private message
			connect.sendPrivateMessage(line);
		}
		else if(line.startsWith(">"))
		{
			connect.sendGroupMessage(line);
		}
		else {
			// Else, send the text so that it is broadcast to all users logged in to the IM
			// server.
			connect.sendMessage(line);
		}
	}
	
	
	/**
	 * Print message to the user console
	 * @param msg Message to be printed
	 */
	public static void printMessages(MessageChatter msg)
	{
		System.out.println(msg.getSender() + ": " + msg.getText()+ " ["+ msg.getDate() + "]");
	}
	
	/**
	 * Print command output onto the user console
	 * @param cmdOut Command Output to be printed
	 */
	public static void printCommandOutput(String cmdOut)
	{
		System.out.println(cmdOut + "");
	}
}

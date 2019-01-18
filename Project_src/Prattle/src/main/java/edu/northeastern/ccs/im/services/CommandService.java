package edu.northeastern.ccs.im.services;

import java.util.Arrays;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.northeastern.ccs.db.services.GroupService;
import edu.northeastern.ccs.db.services.MembershipService;
import edu.northeastern.ccs.db.services.MessageService;
import edu.northeastern.ccs.db.services.UserService;
import edu.northeastern.ccs.im.Message;
import edu.northeastern.ccs.im.models.MessageModel;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.server.Prattle;
import edu.northeastern.ccs.slack.Notification;

public class CommandService {

	private CommandService() {
	}

	private static CommandService singleInstance;

	private static final String WRONGCOMMANDMSG = "Provide command in proper format. Use /help if needed!";

	private static final String WRONGGROUPNAMEMSG = "No such group exists ! Enter a correct group name.";

	/**
	 * Singleton pattern to keep only one instance of the CommandService floating
	 * around
	 * 
	 * @return an instance of the CommandService class
	 */
	public static synchronized CommandService getInstance() {
		if (singleInstance == null) {
			singleInstance = new CommandService();
		}
		return singleInstance;
	}

	/**
	 * Processes commands based on the type of the user
	 * 
	 * @param requester   ClienRunnable of the caller
	 * @param messageLine Command to be executed
	 */
	public void processCommand(ClientRunnable requester, String messageLine) {
		User user = requester.getUser();
		String commandOut = "";

		if (user.isWireTapper())
			commandOut = processWireTapperCommands(requester, messageLine);
		else
			commandOut = processUserCommand(requester, messageLine);

		Message cmdmsg = Message.makeCommandMessage(requester.getName(), commandOut);
		if (requester.isInitialized())
			requester.enqueueMessage(cmdmsg);
	}

	/**
	 * Processes command given by the user to provide the proper output
	 * 
	 * @param requester   ClientRunnable instance of the user that requested the
	 *                    command
	 * @param messageLine Name of the command requested by the user
	 * @return command output
	 */
	public String processUserCommand(ClientRunnable requester, String messageLine) {
		String commandOut = "";
		String[] splitInput = messageLine.split(" ");
		messageLine = splitInput[0];
		User user = requester.getUser();

		try {
			if (messageLine.equals("/profile"))
				commandOut = requester.printProfile();
			else if (messageLine.startsWith("/get"))
				commandOut = processGetCommand(messageLine, user, requester, splitInput);
			else if (messageLine.startsWith("/set"))
				commandOut = processSetCommand(messageLine, user, splitInput[1]);
			else if (messageLine.equals("/deleteaccount"))
				commandOut = processDeleteAccountCommand(requester, user);
			else if (messageLine.equals("/deletegroup"))
				commandOut = processDeleteGroupCommand(user, splitInput[1]);
			else if (messageLine.equals("/help"))
				commandOut = getHelp();
			else if (messageLine.equals("/mygroups"))
				commandOut = PrintService.printGroupsforUser(user);
			else if (messageLine.equals("/newgroup"))
				commandOut = newGroup(user, splitInput[1]);
			else if (messageLine.equals("/joingroup"))
				commandOut = joinGroup(user, splitInput[1]);
			else if (messageLine.equals("/leavegroup"))
				commandOut = leaveGroup(user, splitInput[1]);
			else if (messageLine.startsWith("/list"))
				commandOut = processlistCommands(messageLine, splitInput[1]);
			else if (messageLine.equals("/updatepassword"))
				commandOut = updatePassword(user, splitInput[1]);
			else if (messageLine.equals("/becomewiretapper"))
				commandOut = processSetWireTapper(user);
			else if (messageLine.equals("/displayunreadmsgs")) {
				if (!processDisplayUnreadMsg(user))
					commandOut = "You don't have any unread messages";
				else
					commandOut = "All unread Messages displayed!";
			} else if (messageLine.equals("/turnonparentalcontrol")) {
				user.setParentalControl(true);
				UserService.updateUser(user.getUsername(), user); 
				commandOut = "Parental Control is set to on";
			} else if (messageLine.equals("/turnoffparentalcontrol")) {
				user.setParentalControl(false);
				UserService.updateUser(user.getUsername(), user);
				commandOut = "Parental Control is set to off";
			} else if (messageLine.equals("/addfilteredwords")) {
				String[] filterWordsTOUpate = Arrays.copyOfRange(splitInput, 1, splitInput.length);
				user.appendWordsTOProfane(String.join(",", filterWordsTOUpate));
				UserService.updateUser(user.getUsername(), user);
				commandOut = "words to be filtered added";
			} else if (messageLine.equals("/removefilteredwords")) {
				String filterWordsTOUpate = splitInput[1];
				user.removeWordFromFilteredList(filterWordsTOUpate);
				UserService.updateUser(user.getUsername(), user);
				commandOut = "words to be filtered removed";
			} else if (messageLine.equals("/recalllastmessage")) {
				if (!MessageService.handleRecallMessagesForReceiver(user.getUsername()))
					commandOut = "unable to recall last sent message";
				else
					commandOut = "Last message recalled. Will not be delivered to offline user";
			} else if(messageLine.equals("/recallmessagebyid"))
			{
				if(!MessageService.recallMessageById(Integer.parseInt(splitInput[1]), user.getUsername()))
					commandOut = "unable to recall message by given id";
				else
					commandOut = "Message recalled successfully by ID";
			}
			else {
				commandOut = WRONGCOMMANDMSG;
			}
		} catch (Exception e) {
			commandOut = WRONGCOMMANDMSG;
			Notification.sendNotificationToSlack("FAILURE | LEVEL : LOW | MSG : Wrong command entered by user");
		}

		return commandOut;
	}

	/**
	 * Process commands sent by wiretapper
	 * 
	 * @param requester   ClientRunnable of the wiretapper
	 * @param messageLine command to be executed
	 * @return
	 */
	private String processWireTapperCommands(ClientRunnable requester, String messageLine) {
		String commandOut = "";
		String[] splitInput = messageLine.split(" ");
		messageLine = splitInput[0];

		try {
			if (messageLine.equals("/wiretap")) {
				User tappedUser = UserService.getUserData(splitInput[1]);
				tappedUser.tapUser();

				String startTime = splitInput[2] + " " + splitInput[3];
				String endTime = splitInput[4] + " " + splitInput[5];

				List<MessageModel> conversation = MessageService.getMessageConversationforDuration(
						tappedUser.getUsername(), getTimestampFromString(startTime), getTimestampFromString(endTime));
 
				if (!displayConversationWireTapper(conversation, requester.getUser()))
					commandOut = "No conversations for the provided user";
				else
					commandOut = "All wiretapped messages diplayed!";
			} else if (messageLine.equals("/help")) {
				commandOut = getWireTapperHelp();
			} else if (messageLine.equals("/displayunreadmsgs")) {
				commandOut = "Let the wiretapping begin";
			}
			else if(messageLine.equals("/settarget"))
			{
				User target = UserService.getUserData(splitInput[1]);
				
				if(!target.isUserTapped() && !target.isWireTapper())
				{	
					target.setTappedBy(requester.getName());
					target.setWiretappee(true);
					UserService.updateUser(target.getUsername(), target);
					commandOut = "Wiretap target set";
				}
				else
					commandOut = "Can't tap this user as another agency is already tapping the user";
			}
		} catch (Exception e) {
			commandOut = WRONGCOMMANDMSG;
		}

		return commandOut;
	}

	private boolean processDisplayUnreadMsg(User user) {
		List<MessageModel> unreadMsgs = MessageService.getListOfUndeliveredMessagesForUser(user.getUsername());
		ClientRunnable client = Prattle.getClientFromActive(user.getUsername());
		boolean status = false;
		
		if (unreadMsgs.isEmpty())
			return status;

		for (MessageModel msg : unreadMsgs) {
			Message toBeSent = Message.makeBroadcastMessageWithTime(UserService.getUsernameForId(msg.getSenderId()),
					msg.getText(), msg.getDate().toString());
			client.enqueueMessage(toBeSent);
			String ipAddress = client.getIpAddressOfClient();
			MessageService.updateReceiverIp(user.getUsername(), ipAddress);
			MessageService.updateMessageDeliveryStatus(msg.getId(), true);
			status = true;
		}

		return status;
	}

	private boolean processDisplaySentMessage(List<MessageModel> sentMessages, ClientRunnable requester) {
		boolean status = false;

		if (sentMessages.isEmpty())
			return status;

		for (MessageModel msg : sentMessages) {
			Message toBeSent = Message.makeBroadcastMessageWithTime("Me", msg.getText(), msg.getDate().toString());
			requester.enqueueMessage(toBeSent);
			status = true;
		}
		return status;
	}

	private boolean processDisplayRecvMessage(List<MessageModel> recvMessages, ClientRunnable requester) {
		boolean status = false;

		if (recvMessages.isEmpty())
			return status;

		for (MessageModel msg : recvMessages) {
			Message toBeSent = Message.makeBroadcastMessageWithTime(UserService.getUsernameForId(msg.getSenderId()),
					msg.getText(), msg.getDate().toString());
			requester.enqueueMessage(toBeSent);
			status = true;
		}
		return status;
	}

	private boolean displayConversationWireTapper(List<MessageModel> conv, User wireTapper) {
		ClientRunnable client = Prattle.getClientFromActive(wireTapper.getUsername());
		boolean status = false;

		if (conv.isEmpty())
			return status;
		Message toBeSent;

		for (MessageModel msg : conv) {
			if (!wireTapper.getUsername().equals(UserService.getUsernameForId(+msg.getSenderId())))
				toBeSent = Message.makeBroadcastMessageWithTime(UserService.getUsernameForId(+msg.getSenderId()),
						msg.getText(), msg.getDate().toString());
			else
				toBeSent = Message.makeBroadcastMessageWithTime("Me", msg.getText(), msg.getDate().toString());
			client.enqueueMessage(toBeSent);
			status = true;
		}

		return status;
	}

	private Timestamp getTimestampFromString(String time) throws ParseException {
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date parsedDate = date.parse(time);
		return new Timestamp(parsedDate.getTime());
	}

	/**
	 * Help message for wiretappers
	 * 
	 * @return
	 */
	private String getWireTapperHelp() {
		return "\nHELP COMMANDS:\n\n" + " Command                    Operation\n"
				+ " wiretap                 Start Wire Tapping \n"
				+ " USAGE:                  '/wiretap <usename> <TimeStamp1> <TimeStamp2> \n"
				+ " help                    Get help message for wiretapper \n" + " USAGE:                  '/help\n"
				+ " quit                    Logout \n" + " USAGE:                  '/quit\n";
	}

	/**
	 * Processes user's request to become a wiretapper
	 * 
	 * @param user requesting user
	 * @return command Output
	 */
	private String processSetWireTapper(User user) {
		user.setAsWiretapper();
		boolean result = UserService.updateUser(user.getUsername(), user);

		if (result)
			return "User successfully set as wiretapper";

		return "User is being tapped so cannot be set as wiretapper";
	}

	/**
	 * Update given user's password
	 * 
	 * @param user        User whose password is to be updated
	 * @param newPassword the new password
	 * @return command output
	 */
	private String updatePassword(User user, String newPassword) {
		String commandOut = "";
		user.setPassword(newPassword);
		UserService.updateUser(user.getUsername(), user);
		commandOut = "Password updated successfully!";

		return commandOut;
	}

	/**
	 * Process all the get commands
	 * 
	 * @param messageLine contains command
	 * @param user        caller
	 * @return command output
	 */
	private String processGetCommand(String messageLine, User user, ClientRunnable requester, String[] splitInput) {
		String commandOut = "";

		switch (messageLine) {
		case "/getusername":
			commandOut = user.getUsername();
			break;
		case "/getfirstname":
			commandOut = user.getFirstName();
			break;
		case "/getlastname":
			commandOut = user.getLastName();
			break;
		case "/getuserid":
			commandOut += user.getUserId();
			break;
		case "/getsentmessages":
			List<MessageModel> sentMessages = MessageService.getMessagesSentByUser(user.getUsername());
			if (!processDisplaySentMessage(sentMessages, requester))
				commandOut = "No sent Messages Found!";
			else
				commandOut = "All sent messages diplayed!";
			break;
		case "/getreceivedmessages":
			List<MessageModel> recvMessages = MessageService.getMessagesReceivedByUser(user.getUsername());
			if (!processDisplayRecvMessage(recvMessages, requester))
				commandOut = "No received Messages Found!";
			else
				commandOut = "All received messages diplayed!";
			break;
		case "/getmessagebetween":
			try {
				String startTime = splitInput[1] + " " + splitInput[2];
				String endTime = splitInput[3] + " " + splitInput[4];

				List<MessageModel> conversation = MessageService.getMessageConversationforDuration(user.getUsername(),
						getTimestampFromString(startTime), getTimestampFromString(endTime));
				if (!displayConversationWireTapper(conversation, requester.getUser()))
					commandOut = "No messages between the provided timestamps";
				else
					commandOut = "All messages between given timestamps diplayed!";
			} catch (Exception e) {
				commandOut = WRONGCOMMANDMSG;
			}
			break;
		default:
			commandOut = WRONGCOMMANDMSG;
			break;
		}

		return commandOut;
	}

	/**
	 * Process all the set commands
	 * 
	 * @param messageLine contains command
	 * @param user        caller
	 * @return command output
	 */
	private String processSetCommand(String messageLine, User user, String toBeSet) {
		String commandOut = "";

		switch (messageLine) {
		case "/setfirstname":
			user.setFirstName(toBeSet);
			UserService.updateUser(user.getUsername(), user);
			commandOut = "User's First Name Updated!";
			break;
		case "/setlastname":
			user.setLastName(toBeSet);
			UserService.updateUser(user.getUsername(), user);
			commandOut = "User's Last Name Updated!";
			break;
		default:
			commandOut = WRONGCOMMANDMSG;
			break;
		}

		return commandOut;
	}

	/**
	 * Process all the delete commands
	 * 
	 * @param messageLine contains command
	 * @param user        caller
	 * @param toBeSet     group name to delete
	 * @return command output
	 */
	private String processDeleteGroupCommand(User user, String toBeSet) {
		if (GroupService.checkIfGroupExists(toBeSet)) {
			if (MembershipService.isAdmin(user.getUsername(), toBeSet)) {
				if (GroupService.deleteGroup(toBeSet))
					return "Group deleted Successfully!";
			} else {
				return "Cannot delete the group. You are not an admin!";
			}
		}
		return "Cannot delete this group as no such group exists !";
	}

	/**
	 * Delete user's account completely
	 * 
	 * @param user User whose account has to be deleted
	 * @return command Output
	 */
	private String processDeleteAccountCommand(ClientRunnable requester, User user) {
		boolean result = UserService.deleteUser(user.getUsername());

		if (result) {
			// logout as you have deleted your account
			Message quitMsg = Message.makeQuitMessage(user.getUsername());
			requester.enqueueMessage(quitMsg);

			return "User deleted successfully";
		}

		return "Unable to delete account!";
	}

	/**
	 * Creates a new group
	 * 
	 * @param user      creator
	 * @param groupName name of the group
	 * @return command output
	 */
	private String newGroup(User user, String groupName) {
		if (GroupService.checkIfGroupExists(groupName)) {
			return "A group exists by this name";
		} else {
			if (GroupService.createNewGroup(groupName, user.getUsername()))
				return "Group created Successfully! You are admin!";
		}

		return "Group not created";
	}

	/**
	 * Adds the user to the given group
	 * 
	 * @param user      user to be added
	 * @param groupName name of the group
	 * @return command output
	 */
	private String joinGroup(User user, String groupName) {
		String commandOut = "";

		switch (UserService.joinGroup(user.getUsername(), groupName)) {
		case (1):
			commandOut = user.getUsername() + " added to Group " + groupName + " successfully!";
			break;
		case (0):
			commandOut = "Group not created!";
			break;
		case (-1):
			commandOut = "You are already a member of this group!";
			break;
		default:
			commandOut = "Something happened!";
			break;
		}

		return commandOut;
	}

	/**
	 * Prints the help menu for the users to refer to
	 * 
	 * @return String consisting of the help menu
	 */
	public String getHelp() {
		return "\nHELP COMMANDS:\n\n" + " Command                    Operation\n"
				+ "profile                    Gets the user's profile like firstname, lastname, userID\n"
				+ "getfirstname                Returns the first name of the user\n"
				+ "getlastname                Returns the last name of the user\n"
				+ "getuserid                Returns the user's ID\n"
				+ "setfirstname                Sets the first name of the user\n"
				+ " USAGE:                    '/setfirstname <First-Name>' \n"
				+ "setlastname                Sets the last name of the user\n"
				+ " USAGE:                    '/setlastname <Last-Name> '\n"
				+ "deleteaccount                Delete this user from the system and log out\n"
				+ "getusername                Returns user's username\n"
				+ "updatepassword                Update user's password with the provided password\n"
				+ " USAGE:                    '/updatedpassword <New-Password>'\n"
				+ "help                    Displays this help message\n" + "quit                    Logs out the user\n"
				+ "newgroup                Creates a group of the given name'\n"
				+ " USAGE:                    '/creategroup <Group-Name>'\n"
				+ "joingroup                Adds the user to the given group'\n"
				+ " USAGE:                    '/joingroup <Group-Name>'\n"
				+ "leavegroup                Removes a user from the group\n"
				+ " USAGE:                    '/leavegroup <Group-Name>'\n"
				+ "deletegroup                Deletes a group completely\n"
				+ " USAGE:                    '/deletegroup <Group-Name>'\n"
				+ "listmembers                List all members of a group\n"
				+ " USAGE:                    '/listmembers <Group-Name>\n"
				+ "listadmins                List the admin users of the group\n"
				+ " USAGE:                    '/listadmins <Group-Name>'\n"
				+ "\nCommand for Sending Private message to a User\n"
				+ " USAGE:                    @<receiver's-user-name> <message-to-be-sent>\n"
				+ "\nCommand for Sending Message to a Group\n"
				+ " USAGE:                    ><group-name> <message-to-be-sent>\n"
				+ "displayunreadmsgs        Displays Unread Messages\n"
				+ "USAGE:                    '/displayunreadmsgs'\n"
				+ "turnonparentalcontrol    Turns on parental control\n"
				+ "USAGE:                   '/turnonparentalcontrol\n"
				+ "turnoffparentalcontrol   Turns off parental control\n"
				+ "USAGE:                   '/turnoffparentalcontrol\n"
				+ "addfilteredwords         Adds words to the list of censored words\n"
				+ "USAGE:                   '/addfilteredwords <word1> <word2>'...\n"
				+ "removefilteredwords      Removes the word from the list of censored words\n"
				+ "USAGE:                   '/removefilteredwords <word>\n"
				+ "recalllastmessage                   Recalls the last message sent by user\n"
				+ "USAGE:                   '/recalllastmessage \n"
				+ "getsentmessages          Gets all messages sent by the user\n"
				+ "USAGE:                   '/getsentmessages \n"
				+ "getreceivedmessages      Gets all messages received by the user \n"
				+ "USAGE:                   '/getreceivedmessages \n"
				+ "getmessagebetween        Gets all messages between two timestamps \n"
				+ "USAGE:                   '/getmessagebetween <TimeStamp1> <TimeStamp2> \n";
	}

	/**
	 * User leaves the given group
	 * 
	 * @param user
	 * @param groupName name of the group to be left
	 * @return command output
	 */
	private String leaveGroup(User user, String groupName) {
		if (MembershipService.isMember(user.getUsername(), groupName)) {
			UserService.leaveGroup(user.getUsername(), groupName);
			return user.getUsername() + " removed from Group " + groupName + " successfully";
		}
		return WRONGGROUPNAMEMSG;
	}

	/**
	 * Lists all the members of the given group
	 * 
	 * @param groupName name of the group
	 * @return command output
	 */
	private String processlistCommands(String messageLine, String groupName) {
		if (GroupService.checkIfGroupExists(groupName)) {
			if (messageLine.equals("/listmembers")) {
				return MembershipService.listMembers(groupName).toString();
			}
			if (messageLine.equals("/listadmins")) {
				return MembershipService.listAdmins(groupName).toString();
			}
		}
		return WRONGGROUPNAMEMSG;
	}

}

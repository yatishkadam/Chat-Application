package edu.northeastern.ccs.db.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.northeastern.ccs.db.daos.MessageDao;
import edu.northeastern.ccs.db.daos.UserDao;
import edu.northeastern.ccs.im.models.MessageModel;

public class MessageService {

	private static final Logger logger = LogManager.getLogger(MessageService.class);
	private static final String SENDERNOTREGISTERED = "Sender is not registered with chat server.";
	private static MessageDao messageDao = MessageDao.getInstance();
	private static UserDao userDao = UserDao.getInstance();

	private MessageService() {
	}

	// return : true if success else false
	public static MessageModel createMessage(String text, String senderUsername, String senderIp,
			String receiverUsername, String receiverIp, Timestamp creationDate, boolean deliveryStatus,
			boolean recallStatus) {
		int senderId = userDao.getUserIdFromUsername(senderUsername);
		int receiverId = userDao.getUserIdFromUsername(receiverUsername);

		if (senderId == -1 || receiverId == -1) {
			logger.info("Either sender or receiver is not present in this chat server.");
			return null;
		}
		if (senderId == receiverId) {
			logger.info("Sender cannot send message to herself/himself.");
			return null;
		}
		MessageModel message = new MessageModel(text, senderId, senderIp, receiverId, receiverIp, creationDate,
				deliveryStatus, recallStatus);
		return messageDao.createMessage(message);
	}

	/**
	 * Update message delivery status.
	 *
	 * @param message_id      the message id
	 * @param delivery_status the delivery status
	 * @return the int
	 * @author Karan Tyagi
	 */
	// return 1 if successfully updated, -1 otherwise false
	public static int updateMessageDeliveryStatus(int messageId, boolean deliveryStatus) {
		return messageDao.setMessageDelivery(messageId, deliveryStatus);
	}
	
	/**
	 * Update recall message status.
	 *
	 * @param message_id the message id
	 * @param delivery_status the new recall status which needs to be updated
	 * @return the int
	 * @author Karan Tyagi
	 */
	// return 1 if successfully updated, -1 otherwise false
		public static int updateRecallMessageStatus(int messageId, boolean recallStatus) {
			return messageDao.setMessageRecallStatus(messageId, recallStatus);
		}

	// return 1 if successfully updated, -1 otherwise false
	public static int updateSenderIp(String senderUsername, String ipAddress) {
		int id = userDao.getUserIdFromUsername(senderUsername);
		if (id == -1) {
			logger.info("Sender not registered with chat server.");
			return -1;
		} else {
			return messageDao.updateSenderIp(id, ipAddress);
		}
	}

	// return 1 if successfully updated, -1 otherwise false
		public static int updateReceiverIp(String receiverUsername, String ipAddress) {
			int id = userDao.getUserIdFromUsername(receiverUsername);
			if (id == -1) {
				logger.info("Receiver is not registered with chat server.");
				return -1;
			} else {
				return messageDao.updateReceiverIp(id, ipAddress);
			}
		}

	public static MessageModel getMessage(int messageId) {
		return messageDao.findMessageById(messageId);
	}

	public static List<MessageModel> getListOfUndeliveredMessagesForUser(String username) {
		List<MessageModel> messages = new ArrayList<>();
		int receiverId = userDao.getUserIdFromUsername(username);
		if (receiverId == -1) {
			logger.info("The user doesn't exist on the chat system");
			return messages;
		}
		messages = messageDao.getListOfUndeliveredMessagesForUser(receiverId);
		return messages; 
	}

	public static List<MessageModel> getMessagesSentByUser(String username) {
		int senderId = userDao.getUserIdFromUsername(username);
		if (senderId == -1) {
			logger.info(SENDERNOTREGISTERED);
			return new ArrayList<>();
		} else {
			return messageDao.findSentMessages(senderId);
		}
	}

	public static List<MessageModel> getMessagesReceivedByUser(String username) {
		int senderId = userDao.getUserIdFromUsername(username);
		if (senderId == -1) {
			logger.info(SENDERNOTREGISTERED);
			return new ArrayList<>();
		} else {
			return messageDao.findReceivedMessages(senderId);
		}
	}

	public static List<MessageModel> getMessageConversation(String senderUsername, String receiverUsername) {
		int senderId = userDao.getUserIdFromUsername(senderUsername);
		int receiverId = userDao.getUserIdFromUsername(receiverUsername);
		if (senderId == -1 || receiverId == -1) {
			logger.info("Sender or receiver is not registered with chat server.");
			return new ArrayList<>();
		} else {
			return messageDao.findMessageConversation(senderId, receiverId);
		}
	}

	/**
	 * Gets the message conversation for duration.
	 *
	 * @param senderUsername the sender username
	 * @param start          the start
	 * @param end            the end
	 * @return the message conversation for duration
	 * @author Karan Tyagi
	 */
	public static List<MessageModel> getMessageConversationforDuration(String senderUsername, Timestamp start,
			Timestamp end) {
		int senderId = userDao.getUserIdFromUsername(senderUsername);
		if (senderId == -1) {
			logger.info(SENDERNOTREGISTERED);
			return new ArrayList<>();
		} else {
			return messageDao.getConversation(senderId, start, end);
		}
	}

	public static boolean handleRecallMessagesForReceiver(String senderUsername) {
		int senderId = userDao.getUserIdFromUsername(senderUsername);
		if (senderId == -1) {
			logger.info(SENDERNOTREGISTERED);
			return false;
		} else {
			return messageDao.handleRecallMessage(senderId);
		}
	}
	
	public static boolean recallMessageById(int messageId, String senderUsername) {
		return messageDao.recallById(messageId, senderUsername);
	}

}

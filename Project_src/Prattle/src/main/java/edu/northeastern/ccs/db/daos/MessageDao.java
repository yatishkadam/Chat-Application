package edu.northeastern.ccs.db.daos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.northeastern.ccs.db.services.ConnectionService;
import edu.northeastern.ccs.im.models.MessageModel;

/**
 * The Class MessageDao.
 */
public class MessageDao {

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(MessageDao.class);

	/** The instance. */
	private static MessageDao instance = null;

	/** The userdao. */
	static UserDao userdao = null;

	/** The conn instance. */
	static java.sql.Connection connInstance = null;

	/** The p statement. */
	PreparedStatement pStatement = null;

	/** The results. */
	ResultSet results = null;

	/**
	 * Instantiates a new message dao.
	 */
	private MessageDao() {
	}

	private static final String SENDERID = "sender_id";
	private static final String SENDERIP = "sender_ip";
	private static final String RECEIVERID = "receiver_id";
	private static final String RECEIVERIP = "receiver_ip";

	private static final String FINDMESSAGEBYID = "SELECT * FROM `message` WHERE `message`.`id`=?";

	private static final String CREATEMESSAGE = " insert into message (text, sender_id, sender_ip,receiver_id,receiver_ip,date,delivered_status,recall_status,profane) "
			+ "values (?, ?, ?, ?, ?,?,?,?,?)";

	private static final String UPDATEMESSAGE = "Update message SET `message`.`delivered_status` = ? WHERE `message`.`id` =?";

	private static final String UPDATEMESSAGERECALLSTATUS = "Update message SET `message`.`recall_status` = ? WHERE `message`.`id` =?";

	private static final String UPDATESENDERIP = "Update `message` SET `message`.`sender_ip` =? WHERE `message`.`sender_id` =?";

	private static final String UPDATERECEIVERIP = "Update `message` SET `message`.`receiver_ip` =? WHERE `message`.`receiver_id` =?";

	private static final String GETUNDELIVEREDMESSAGES = "Select * FROM `message`"
			+ " WHERE `message`.`delivered_status`=? and `message`.`receiver_id` = ?";

	private static final String GETSENTMESSAGES = "SELECT * FROM `message` WHERE `message`.`sender_id`=? AND `message`.`recall_status`=?";

	private static final String GETRECEIVEDMESSAGES = "SELECT * FROM `message` WHERE `message`.`receiver_id`=? AND `message`.`recall_status`=?";

	private static final String GETMESSAGECONVERSATION = "SELECT * FROM `message` "
			+ "WHERE `message`.`sender_id`=? AND `message`.`receiver_id`=?";

	private static final String GETMESSAGELOG = "SELECT * FROM `message` WHERE ((`message`.`sender_id`=?) OR (`message`.`receiver_id`=?)) AND (`message`.`date` BETWEEN ? and ? )";

	/** The Constant GET_MESSAGE_DATE. */
	private static final String GETMESSAGEDATE = "select MAX(`message`.`date`) as max from `message` where `message`.`sender_id`= ?";

	/** The Constant RECALL_LAST_MESSAGE_FOR_USER. */
	private static final String RECALLLASTMESSAGEFORUSER = "SELECT * from `message` where `message`.`date`= ? and `message`.`sender_id`= ?";

	/**
	 * Gets the single instance of MessageDao.
	 *
	 * @return single instance of MessageDao
	 */
	public static MessageDao getInstance() {
		try {
			connInstance = ConnectionService.getDatabaseConnection();
			userdao = UserDao.getInstance();

			if (instance == null)
				instance = new MessageDao();

		} catch (ClassNotFoundException | SQLException e) {
			logger.error(e);
		}
		return instance;
	}

	/**
	 * Handle recall message.
	 *
	 * @param senderId the sender id
	 * @return true, if successful
	 */
	public boolean handleRecallMessage(int senderId) {
		boolean status = false;
		Timestamp timestamp = null;
		try {
			pStatement = connInstance.prepareStatement(GETMESSAGEDATE);
			pStatement.setInt(1, senderId);
			results = pStatement.executeQuery();
			while (results.next()) {
				timestamp = results.getTimestamp("max");
				break;
			}
			pStatement.close();
			results.close();
			pStatement = connInstance.prepareStatement(RECALLLASTMESSAGEFORUSER);
			pStatement.setTimestamp(1, timestamp);
			pStatement.setInt(2, senderId);
			results = pStatement.executeQuery();
			int messageId = 0;
			while (results.next()) {
				messageId = results.getInt("id");
				break;
			}
			pStatement.close();
			results.close();
			setMessageDelivery(messageId, true);
			setMessageRecallStatus(messageId, true);
			pStatement.close();
			results.close();
			return true;
		} catch (SQLException e) {
			logger.error(e);
		}
		return status;
	}

	/**
	 * Creates the message.
	 *
	 * @param message the message
	 * @return the message model
	 */
	public MessageModel createMessage(MessageModel message) {
		MessageModel msg = null;
		try {
			pStatement = connInstance.prepareStatement(CREATEMESSAGE, Statement.RETURN_GENERATED_KEYS);
			pStatement.setString(1, message.getText());
			pStatement.setInt(2, message.getSenderId());
			pStatement.setString(3, message.getSenderIp());
			pStatement.setInt(4, message.getReceiverId());
			pStatement.setString(5, message.getSenderIp());
			pStatement.setTimestamp(6, message.getDate());
			pStatement.setBoolean(7, message.isDeliveredStatus());
			pStatement.setBoolean(8, message.isRecallStatus());
			pStatement.setBoolean(9, message.isProfane());
			pStatement.executeUpdate();
			results = pStatement.getGeneratedKeys();
			if (results.next()) {
				int messageId = results.getInt(1);
				results.close();
				pStatement.close();
				return findMessageById(messageId);
			} else {
				results.close();
				pStatement.close();
			}
		} catch (SQLException e) {
			logger.error(e);
		}
		return msg;
	}

	/**
	 * Find message by id.
	 *
	 * @param messageId the message id
	 * @return the message model
	 */
	public MessageModel findMessageById(int messageId) {
		MessageModel m = null;
		boolean found = false;
		try {
			pStatement = connInstance.prepareStatement(FINDMESSAGEBYID);
			pStatement.setInt(1, messageId);
			results = pStatement.executeQuery();
			while (results.next()) {
				int id = results.getInt("id");
				String text = results.getString("text");
				int senderId = results.getInt(SENDERID);
				String senderIp = results.getString(SENDERIP);
				int receiverId = results.getInt(RECEIVERID);
				String receiverIp = results.getString(RECEIVERIP);
				Timestamp date = results.getTimestamp("date");
				boolean deliveredStatus = results.getBoolean("delivered_status");
				boolean recallStatus = results.getBoolean("recall_status");
				m = new MessageModel(id, text, senderId, senderIp, receiverId, receiverIp, date, deliveredStatus,
						recallStatus);
				found = true;
				break;
			}
			pStatement.close();
			results.close();
			if (!found) {
				logger.info("id=" + messageId + " : No Message found ! ");
				return null;
			}

		} catch (SQLException e) {
			logger.error(e);
		}
		return m;
	}

	/**
	 * Sets the message delivery.
	 *
	 * @param messageId  the message id
	 * @param isDelivered the is delivered
	 * @return the int
	 */
	public int setMessageDelivery(int messageId, boolean isDelivered) {
		int status = -1;
		try {
			pStatement = connInstance.prepareStatement(UPDATEMESSAGE);
			pStatement.setBoolean(1, isDelivered);
			pStatement.setInt(2, messageId);
			status = pStatement.executeUpdate();
			pStatement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return status;
	}

	/**
	 * Sets the message recall status.
	 *
	 * @param messageId    the message id
	 * @param recallStatus the recall status
	 * @return the int
	 */
	public int setMessageRecallStatus(int messageId, boolean recallStatus) {
		int status = -1;
		try {
			pStatement = connInstance.prepareStatement(UPDATEMESSAGERECALLSTATUS);
			pStatement.setBoolean(1, recallStatus);
			pStatement.setInt(2, messageId);
			status = pStatement.executeUpdate();
			pStatement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return status;
	}

	/**
	 * Gets the list of undelivered messages for user.
	 *
	 * @param userId the user id
	 * @return the list of undelivered messages for user
	 */
	public List<MessageModel> getListOfUndeliveredMessagesForUser(int userId) {
		List<MessageModel> messages = new ArrayList<>();
		try {
			pStatement = connInstance.prepareStatement(GETUNDELIVEREDMESSAGES);
			pStatement.setBoolean(1, false);
			pStatement.setInt(2, userId);
			results = pStatement.executeQuery();
			while (results.next()) {
				int id = results.getInt("id");
				String text = results.getString("text");
				int senderId = results.getInt(SENDERID);
				String senderIp = results.getString(SENDERIP);
				int receiverId = results.getInt(RECEIVERID);
				String receiverIp = results.getString(RECEIVERIP);
				Timestamp creationDate = results.getTimestamp("date");
				MessageModel message = new MessageModel(text, senderId, senderIp, receiverId, receiverIp,
						creationDate);
				message.setId(id);
				messages.add(message);
			}
			pStatement.close();
			results.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return messages;
	}

	/**
	 * Find all sent messages for a sender.
	 *
	 * @param sender_id the sender id
	 * @return the list of all messages sent by the sender
	 */
	public List<MessageModel> findSentMessages(int senderId) {
		List<MessageModel> messages = new ArrayList<>();
		try {
			pStatement = connInstance.prepareStatement(GETSENTMESSAGES);
			pStatement.setInt(1, senderId);
			pStatement.setBoolean(2, false);
			results = pStatement.executeQuery();
			while (results.next()) {
				int id = results.getInt("id");
				String text = results.getString("text");
				int sId = results.getInt(SENDERID);
				String senderIp = results.getString(SENDERIP);
				int receiverId = results.getInt(RECEIVERID);
				String receiverIp = results.getString(RECEIVERIP);
				Timestamp creationDate = results.getTimestamp("date");
				MessageModel message = new MessageModel(text, sId, senderIp, receiverId, receiverIp, creationDate);
				message.setId(id);
				messages.add(message);
			}
			pStatement.close();
			results.close();
		} catch (SQLException e) {
			logger.error("fucked here " + e);
		}
		return messages;
	}

	/**
	 * Find received messages.
	 *
	 * @param receiver_id the receiver id
	 * @return the list
	 */
	public List<MessageModel> findReceivedMessages(int receiverId) {
		List<MessageModel> messages = new ArrayList<>();
		try {
			pStatement = connInstance.prepareStatement(GETRECEIVEDMESSAGES);
			pStatement.setInt(1, receiverId);
			pStatement.setBoolean(2, false);
			results = pStatement.executeQuery();
			while (results.next()) {
				int id = results.getInt("id");
				String text = results.getString("text");
				int senderId = results.getInt(SENDERID);
				String senderIp = results.getString(SENDERIP);
				int rId = results.getInt(RECEIVERID);
				String receiverIp = results.getString(RECEIVERIP);
				Timestamp creationDate = results.getTimestamp("date");
				MessageModel message = new MessageModel(text, senderId, senderIp, rId, receiverIp, creationDate);
				message.setId(id);
				messages.add(message);
			}
			pStatement.close();
			results.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return messages;
	}

	/**
	 * Find message conversation.
	 *
	 * @param sender_id   the sender id
	 * @param receiver_id the receiver id
	 * @return the list
	 */
	public List<MessageModel> findMessageConversation(int senderId, int receiverId) {
		List<MessageModel> messages = new ArrayList<>();
		try {
			pStatement = connInstance.prepareStatement(GETMESSAGECONVERSATION);
			pStatement.setInt(1, senderId);
			pStatement.setInt(2, receiverId);
			results = pStatement.executeQuery();
			while (results.next()) {
				int id = results.getInt("id");
				String text = results.getString("text");
				int sId = results.getInt(SENDERID);
				String senderIp = results.getString(SENDERIP);
				int rId = results.getInt(RECEIVERID);
				String receiverIp = results.getString(RECEIVERIP);
				Timestamp creationDate = results.getTimestamp("date");
				MessageModel message = new MessageModel(text, sId, senderIp, rId, receiverIp, creationDate);
				message.setId(id);
				messages.add(message);
			}
			pStatement.close();
			results.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return messages;
	}

	/**
	 * Gets the conversation.
	 *
	 * @param userId the user id
	 * @param start  the start
	 * @param end    the end
	 * @return the conversation
	 */
	public List<MessageModel> getConversation(int userId, Timestamp start, Timestamp end) {
		List<MessageModel> messages = new ArrayList<>();
		try {
			pStatement = connInstance.prepareStatement(GETMESSAGELOG);
			pStatement.setInt(1, userId);
			pStatement.setInt(2, userId);
			pStatement.setTimestamp(3, start);
			pStatement.setTimestamp(4, end);

			results = pStatement.executeQuery();
			while (results.next()) {
				int id = results.getInt("id");
				String text = results.getString("text");
				int senderId = results.getInt(SENDERID);
				String senderIp = results.getString(SENDERIP);
				int receiverId = results.getInt(RECEIVERID);
				String receiverIp = results.getString(RECEIVERIP);
				Timestamp creationDate = results.getTimestamp("date");
				MessageModel message = new MessageModel(text, senderId, senderIp, receiverId, receiverIp, creationDate);
				message.setId(id);
				messages.add(message);
			}
			pStatement.close();
			results.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return messages;
	}

	/**
	 * Update sender ip.
	 *
	 * @param id        the id
	 * @param ipAddress the ip address
	 * @return the int
	 */
	public int updateSenderIp(int id, String ipAddress) {
		int status = -1;
		try {
			pStatement = connInstance.prepareStatement(UPDATESENDERIP);
			pStatement.setString(1, ipAddress);
			pStatement.setInt(2, id);
			status = pStatement.executeUpdate();
			pStatement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return status;
	}

	/**
	 * Update receiver ip.
	 *
	 * @param id        the id
	 * @param ipAddress the ip address
	 * @return the int
	 */
	public int updateReceiverIp(int id, String ipAddress) {
		int status = -1;
		try {
			pStatement = connInstance.prepareStatement(UPDATERECEIVERIP);
			pStatement.setString(1, ipAddress);
			pStatement.setInt(2, id);
			status = pStatement.executeUpdate();
			pStatement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return status;
	}

	/**
	 * Recall message by id.
	 *
	 * @param messageId      the message id
	 * @param senderUsername the sender username
	 * @return true, if successful
	 * @author Karan Tyagi
	 */
	public boolean recallById(int messageId, String senderUsername) {
		return (setMessageDelivery(messageId, true) == 1 && setMessageRecallStatus(messageId, true) == 1);
	}

}

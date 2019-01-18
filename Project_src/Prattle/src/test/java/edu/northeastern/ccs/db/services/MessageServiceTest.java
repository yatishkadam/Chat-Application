package edu.northeastern.ccs.db.services;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mysql.jdbc.Messages;

import edu.northeastern.ccs.im.HashGenerator;
import edu.northeastern.ccs.im.models.MessageModel;

class MessageServiceTest {

	private static Logger logger = null;
	private static Connection dbConnection = null;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		logger = LogManager.getLogger(ConnectionService.class);
		dbConnection = ConnectionService.getDatabaseConnection();

		// delete previous dummy data if present
		// delete dummy memberships
		MembershipService.removeMember("dummieeUser123xyz", "dummieeGroup123xyz");
		MembershipService.removeMember("dummieeUser456def", "dummieeGroup456def");
		MembershipService.removeMember("dummieeUser789abc", "dummieeGroup789abc");

		// delete dummy groups from database after testing user services
		GroupService.deleteGroup("dummieeGroup123xyz");
		GroupService.deleteGroup("dummieeGroup456def");
		GroupService.deleteGroup("dummieeGroup789abc");

		// delete dummy users
		UserService.deleteUser("dummieeUser123xyz");
		UserService.deleteUser("dummieeUser456def");
		UserService.deleteUser("dummieeUser789abc");
		UserService.deleteUser("dummieeUser000pqr");

		// create dummy users
		UserService.createUser("dummieeUser123xyz", HashGenerator.getSHA256("123xyz"));
		UserService.createUser("dummieeUser456def", HashGenerator.getSHA256("456def"));
		UserService.createUser("dummieeUser789abc", HashGenerator.getSHA256("789abc"));
		UserService.createUser("dummieeUser456deff", HashGenerator.getSHA256("456deff"));
		UserService.createUser("dummieeUser789abcc", HashGenerator.getSHA256("789abcc"));
		UserService.createUser("dummieeUser000pqr", HashGenerator.getSHA256("000pqr"));
		// create dummy groups
		GroupService.createNewGroup("dummieeGroup123xyz", "dummieeUser123xyz");
		GroupService.createNewGroup("dummieeGroup789abc", "dummieeUser789abc");
		GroupService.createNewGroup("dummieeGroup456def", "dummieeUser456def");
		UserService.joinGroup("dummieeUser000pqr", "dummieeGroup123xyz");

	}

	@Test
	void testCreateMessage() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		// cannot send message to self
		assertTrue(MessageService.createMessage("garbage message", "dummieeUser456def", "111.111.111.1",
				"dummieeUser456def", "222.222.222.2", timestamp, true, false) == null);
		MessageModel m1 = MessageService.createMessage("garbage message", "dummieeUser456def", "111.111.111.1",
				"dummieeUser789abc", "222.222.222.2", timestamp, true, false);
		assertTrue(MessageService.createMessage("garbage message", "dummieeUser456def", "111.111.111.1",
				"dummieeUser789abc", "222.222.222.2", timestamp, true, false) != null);
		assertTrue(MessageService.createMessage("garbage message", "dummieeUser789abc", "111.111.111.1",
				"dummieeUser456def", "222.222.222.2", timestamp, true, false) != null);

		MessageService.updateReceiverIp("dummieeUser456def", "d456 R ipAddress");
		MessageService.updateSenderIp("dummieeUser456def", "d456 S ipAddress");
		assertTrue(m1 != null);

		assertTrue(MessageService.createMessage("garbage message", "dummieeUser456def", "111.111.111.1",
				"dummieeUser789achjbc", "222.222.222.2", timestamp, true, false) == null);
		assertTrue(MessageService.createMessage("garbage message", "dummieeUser456decbf", "111.111.111.1",
				"dummieeUser789abc", "222.222.222.2", timestamp, true, false) == null);
		/*
		 * TEST RECALL MESSAGE SERVICE
		 */
		assertTrue(MessageService.createMessage("garbage message", "alice", "111.111.111.1", "aryan",
				"222.222.222.2", timestamp, false, false) != null);
		assertTrue(MessageService.handleRecallMessagesForReceiver("alice"));

		assertEquals(2, MessageService.getMessagesSentByUser("dummieeUser456def").size());
		assertEquals(2, MessageService.getMessagesReceivedByUser("dummieeUser789abc").size());
		
		assertEquals(true, MessageService.recallMessageById(m1.getId(), "dummieeUser456def"));
		assertTrue(MessageService.recallMessageById(m1.getId(), "dummiyt7heeUser456def"));
	}

	@Test
	void testUpdateDeliveryStatus() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		MessageModel m1 = MessageService.createMessage("garbage message", "dummieeUser456def", "111.111.111.1",
				"dummieeUser123xyz", "222.222.222.2", new Timestamp(System.currentTimeMillis() + 360000000), false,
				false);
		assertFalse(m1.isDeliveredStatus());
		assertFalse(m1.isRecallStatus());

		assertEquals(0, MessageService.getListOfUndeliveredMessagesForUser("dummieeUser456def").size());
		assertEquals(1, MessageService.getListOfUndeliveredMessagesForUser("dummieeUser123xyz").size());

		assertEquals(1, MessageService.updateMessageDeliveryStatus(m1.getId(), true));
		assertTrue(MessageService.getMessage(m1.getId()).isDeliveredStatus());
		
		assertEquals(1, MessageService.updateMessageDeliveryStatus(m1.getId(), false));
		assertFalse(MessageService.getMessage(m1.getId()).isDeliveredStatus());
		
		assertEquals(1, MessageService.updateRecallMessageStatus(m1.getId(), true));
		assertTrue(MessageService.getMessage(m1.getId()).isRecallStatus());
		
		assertEquals(1, MessageService.updateRecallMessageStatus(m1.getId(), false));
		assertFalse(MessageService.getMessage(m1.getId()).isRecallStatus());
		
		assertEquals(-1,  MessageService.updateReceiverIp("hbwefkbwfhrg", "ipaddressss"));
		assertEquals(-1,  MessageService.updateSenderIp("hbwefkbwfhrg", "ipaddressss"));
		
		assertFalse(MessageService.handleRecallMessagesForReceiver("senderUsernadsbhgfbdsme"));
		assertTrue(MessageService.handleRecallMessagesForReceiver("dummieeUser456def"));
	}

	@Test
	void testUndeliveredmessages() {
		assertEquals(0, MessageService.getListOfUndeliveredMessagesForUser("dummdvheieeUser456def").size());
	}

	@Test
	void testSentMessages() {
		assertEquals(0, MessageService.getMessagesSentByUser("dummdvheieeUser456def").size());
	}

	@Test
	void testReceivedMessages() {
		assertEquals(0, MessageService.getMessagesReceivedByUser("dummdvheieeUser456def").size());
	}

	@Test
	void testGetConversation() throws NoSuchAlgorithmException {
		long start = System.currentTimeMillis();
		Timestamp timestamp = new Timestamp(start);
		
		assertEquals(0,
				MessageService
						.getMessageConversationforDuration("dummidgvehceeUser456deff", timestamp, new Timestamp(start + 2400))
						.size());
		
		assertEquals(0, MessageService.getMessageConversation("dummdvheieeUser456def", "dummdvheieeUser456def").size());
		assertEquals(0, MessageService.getMessageConversation("dummieeUser456def", "dummdvheieeUser456def").size());
		assertEquals(0, MessageService.getMessageConversation("dummdvheieeUser456def", "dummieeUser456def").size());
		UserService.createUser("dummieeUser456deff", HashGenerator.getSHA256("456deff"));
		UserService.createUser("dummieeUser789abcc", HashGenerator.getSHA256("789abcc"));
		assertTrue(MessageService.createMessage("garbage message 1", "dummieeUser456deff", "111.111.111.1",
				"dummieeUser789abcc", "222.222.222.2", timestamp, true, false) != null);
		assertTrue(MessageService.createMessage("garbage message 2", "dummieeUser456deff", "111.111.111.1",
				"dummieeUser789abcc", "222.222.222.2", timestamp, true, false) != null);
		assertTrue(MessageService.createMessage("garbage message 3", "dummieeUser456deff", "111.111.111.1",
				"dummieeUser789abcc", "222.222.222.2", timestamp, true, false) != null);
		assertEquals(3, MessageService.getMessageConversation("dummieeUser456deff", "dummieeUser789abcc").size());

//		System.out.println("START : " + timestamp);
//		System.out.println("END   : " + new Timestamp(start + 6400));
		assertEquals(3,
				MessageService
						.getMessageConversationforDuration("dummieeUser456deff", timestamp, new Timestamp(start + 2400))
						.size());

	}

	@Test
	void testMessageLogs() {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		// delete dummy users

		// delete all test messages
		final String DELETE_TEST_MESSAGES = "DELETE FROM `chat_database`.`message` WHERE `message`.`text` = 'garbage message' OR"
				+ "`message`.`text`='garbage message 1' OR " + "`message`.`text`='garbage message 2' OR "
				+ "`message`.`text`='garbage message 3'";
		try {
			PreparedStatement statement = dbConnection.prepareStatement(DELETE_TEST_MESSAGES);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.error(e);
		}

		// delete dummy groups from database after testing user services
		GroupService.deleteGroup("dummieeGroup123xyz");
		GroupService.deleteGroup("dummieeGroup456def");
		GroupService.deleteGroup("dummieeGroup789abc");

		// delete dummy users
		UserService.deleteUser("dummieeUser123xyz");
		UserService.deleteUser("dummieeUser456def");
		UserService.deleteUser("dummieeUser789abc");
		UserService.deleteUser("dummieeUser456deff");
		UserService.deleteUser("dummieeUser789abcc");
		UserService.deleteUser("dummieeUser000pqr");

//		ConnectionService.closeDatabaseConnection();
//		Field dbConnection = ConnectionService.class.getDeclaredField("dbConnection");
//		dbConnection.setAccessible(true);
//		Connection conn = (Connection) dbConnection.get(null);
//		assertTrue(conn.isClosed());
	}

}

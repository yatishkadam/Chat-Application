package edu.northeastern.ccs.db.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.slack.Notification;


class ConnectionServiceTest {
	
	/*
	 * Logger
	 */
	private static Logger logger = null;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		logger = LogManager.getLogger(ConnectionService.class);
	}

	@Test
	void testConnection() throws ClassNotFoundException, SQLException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Connection test = ConnectionService.getDatabaseConnection();
		assertFalse(test.isClosed());
//		System.out.println("Is DB connection closed ? " + test.isClosed());	
//		 ConnectionService.closeDatabaseConnection();
//	     System.out.println("All tests done");
//			Field dbConnection = ConnectionService.class.getDeclaredField("dbConnection");
//			dbConnection.setAccessible(true);
//			Connection conn = (Connection) dbConnection.get(null);
//			assertTrue(conn.isClosed());
//			System.out.println("Is DB connection closed ? " + conn.isClosed());
	}
	
	@Test
	void testNotifications() throws IOException {
		Notification.sendNotificationToSlack("check message");
	}
	
	

}

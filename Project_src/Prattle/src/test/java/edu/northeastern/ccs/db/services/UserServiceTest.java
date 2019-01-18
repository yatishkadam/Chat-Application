package edu.northeastern.ccs.db.services;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.db.daos.UserDao;
import edu.northeastern.ccs.im.HashGenerator;
import edu.northeastern.ccs.im.models.User;

class UserServiceTest {

	private static Logger logger = null;
	private static Connection test = null; 
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://cs5500database.cvunqyiiwmc8.us-east-1.rds.amazonaws.com/chat_database?autoReconnect=true";
	private static final String USER = "user";
	private static final String PASSWORD = "password";
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Class.forName(DRIVER);
		test = DriverManager.getConnection(URL, USER, PASSWORD);
		logger = LogManager.getLogger(ConnectionService.class);
		// delete previous dummy data if present
		UserService.deleteUser("dummieeUser123xyz");
		UserService.deleteUser("dummieeUser456def");
		UserService.deleteUser("dummieeUser789abc");
		UserService.deleteUser("dummeeeii3435");
		// create new dummy data
		UserService.createUser("dummieeUser123xyz", HashGenerator.getSHA256("123xyz"));
		UserService.createUser("dummieeUser789abc", HashGenerator.getSHA256("789abc"));
	}

	@Test
	void testCreateUser() {
		try {
			System.out.println(test.isClosed());
		} catch (SQLException e1) {
			logger.error(e1);
		}
		try {
			assertTrue(UserService.createUser("dummieeUser456def", HashGenerator.getSHA256("456def")));
			// once created, cannot be created again
			assertFalse(UserService.createUser("dummieeUser456def", HashGenerator.getSHA256("456def")));
			
			// validate password
			UserService.validatePassword("dummieeUser456def", null);
			assertTrue(UserService.validatePassword("dummieeUser456def", "456def"));
			assertFalse(UserService.validatePassword("dummieeUser456def", "4754858458"));
			UserService.setCensoredWordsForUser("dummieeUser456def", "fuck");
			User data = UserService.getUserData("dummieeUser456def");
			assertEquals(data.getUsername(), UserService.getUsernameForId(data.getUserId()));
			
			assertEquals("dummieeUser456def", data.getUsername());
			assertEquals(data.getUsername(), UserService.getUsernameForId(data.getUserId()));
			assertEquals("fuck", UserService.getCensoredWordsForUser("dummieeUser456def"));
			assertTrue(UserService.setCensoredWordsForUser("dummieeUser456def", "words"));
			
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		}
	}
	
	@Test
	void testUserExists() {
		assertTrue(UserService.checkIfUserExists("dummieeUser123xyz"));
		assertFalse(UserService.checkIfUserExists("bsdhgvjm475hv7@@4saf"));
	}
	
	@Test
	void testDeleteUser() {
		assertFalse(UserService.deleteUser("bsdhgvjm4@@75hv74saf"));
		assertTrue(UserService.deleteUser("dummieeUser123xyz"));
	}
	
	@Test
	void testUpdateUser() {
		User updatedDummy = new User("Vin", "Diesel", "dummeeeii3435", "123");
		assertTrue(UserService.updateUser("dummieeUser789abc", updatedDummy));
		assertFalse(UserService.updateUser("dumxbhxkmieeUser456def", updatedDummy));
	}
	
	@Test
	void testTappingAgencies() {
		UserService.setTappedByAgencyForUser("dummieeUser789abc", "FBI");
		UserService.setTappedByAgencyForUser("dummieeUsbfger789abc", "FBI");
		assertEquals("",UserService.getTappingAgencyForUser("dummieeUser789abc"));
		assertEquals("", UserService.getTappingAgencyForUser("dumxbhxkmieeUser456def"));	
	}
	
	@AfterAll
	static void tearDownAfterClass() throws Exception {
		// delete dummy users from database after testing user services
		UserService.deleteUser("dummieeUser123xyz");
		UserService.deleteUser("dummieeUser456def");
		UserService.deleteUser("dummieeUser789abc");
		UserService.deleteUser("dummeeeii3435");
	}

}

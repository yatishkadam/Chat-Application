package edu.northeastern.ccs.db.daos;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.db.services.ConnectionService;
import edu.northeastern.ccs.db.services.UserService;
import edu.northeastern.ccs.im.HashGenerator;

class UserDaoTest {

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
		 UserService.deleteUser("dummieeUserrr123xyz");
		// create new dummy data
		UserService.createUser("dummieeUserrr123xyz", HashGenerator.getSHA256("123xyz"));
	}

	@Test
	void testUserDao() {
		UserDao dao = UserDao.getInstance();

		assertEquals("", dao.getCensoredWords(null));
		assertEquals(false, dao.setCensoredWords(null, null));
		assertEquals(null, dao.findUserByUsername(null));
		assertEquals(null, dao.findUserByUsername(""));

		dao.setTappingAgencyForUser("dummieeUserrr123xyz", "CIAA");
		assertEquals("CIAA", dao.getTappedbyForUser("dummieeUserrr123xyz"));

	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		// delete dummy users from database after testing user services
		UserService.deleteUser("dummieeUserrr123xyz");
	}

}

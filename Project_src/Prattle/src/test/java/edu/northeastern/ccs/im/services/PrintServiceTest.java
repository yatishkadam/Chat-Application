package edu.northeastern.ccs.im.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.db.services.ConnectionService;
import edu.northeastern.ccs.db.services.GroupService;
import edu.northeastern.ccs.db.services.MembershipService;
import edu.northeastern.ccs.db.services.UserService;
import edu.northeastern.ccs.im.HashGenerator;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.server.ClientRunnable;
import edu.northeastern.ccs.im.server.Prattle;

/**
 * @author Karan Tyagi
 */
class PrintServiceTest {

	private static Connection dbConnection = null;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
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
		GroupService.deleteGroup("dummeeeiiG3435");

		// delete dummy users
		UserService.deleteUser("dummieeUser123xyz");
		UserService.deleteUser("dummieeUser456def");
		UserService.deleteUser("dummieeUser789abc");
		UserService.deleteUser("dummieeUser789abcd");

		// create dummy users
		UserService.createUser("dummieeUser123xyz", HashGenerator.getSHA256("123xyz"));
		UserService.createUser("dummieeUser456def", HashGenerator.getSHA256("456def"));
		UserService.createUser("dummieeUser789abc", HashGenerator.getSHA256("789abc"));
		UserService.createUser("dummieeUser789abcd", HashGenerator.getSHA256("789abcd"));
		// create dummy groups
		GroupService.createNewGroup("dummieeGroup123xyz", "dummieeUser123xyz");

	}

	/**
	 * @author Karan Tyagi
	 * @throws Exception
	 */
	@Test
	void testPrintGroupsForUser() {
		User user = new User();
		assertEquals("You are still not a member of any group!\n0 Groups", PrintService.printGroupsforUser(user));
		User u = UserService.getUserData("dummieeUser123xyz");
		assertEquals("1 Groups\ndummieeGroup123xyz", PrintService.printGroupsforUser(u));

	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		// delete dummy users

		// delete dummy groups from database after testing user services
		GroupService.deleteGroup("dummieeGroup123xyz");

		// delete dummy users
		UserService.deleteUser("dummieeUser123xyz");
		UserService.deleteUser("dummieeUser456def");
	}

}

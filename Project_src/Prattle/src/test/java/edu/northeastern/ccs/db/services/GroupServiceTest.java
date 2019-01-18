package edu.northeastern.ccs.db.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.sql.Connection;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.northeastern.ccs.db.daos.GroupDao;
import edu.northeastern.ccs.im.HashGenerator;
import edu.northeastern.ccs.im.models.User;

public class GroupServiceTest {

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
		GroupService.createNewGroup("dummieeGroup789abc", "dummieeUser789abc");
	}

	@Test
	void testListMembers() {
		assertEquals(1, UserService.joinGroup("dummieeUser789abcd", "dummieeGroup789abc"));
		assertEquals(0, MembershipService.listMembers("dummiep789abc").size());
		assertEquals(2, MembershipService.listMembers("dummieeGroup789abc").size());
		assertFalse(UserService.joinGroup("dummieeUser789abc", "dummieeGroup789abc") == 1);
		assertFalse(UserService.joinGroup("dummieeUser789abcd", "dummieeGroup789abc") == 1);
		assertEquals(1, UserService.myGroups("dummieeUser789abcd").size());

		assertTrue(UserService.leaveGroup("dummieeUser789abcd", "dummieeGroup789abc"));
		assertEquals(1, MembershipService.listMembers("dummieeGroup789abc").size());
		assertFalse(UserService.leaveGroup("dummieeUser789abcd", "dummieeGroup789abc"));
		assertEquals(0, UserService.myGroups("dummieeUser789abcd").size());
	}

	@Test
	void testListAdmins() {
		assertEquals(1, MembershipService.listAdmins("dummieeGroup789abc").size());
	}

	@Test
	void testCreateGroup() {
		 assertTrue(GroupService.createNewGroup("dummieeGroup456def",
		 "dummieeUser456def"));
		 // once created, cannot be created again
		 assertFalse(GroupService.createNewGroup("dummieeGroup456def",
		 "dummieeUser456def"));
		 assertTrue(MembershipService.addMember("dummieeUser456def", "dummieeGroup123xyz") == 1);
		 assertFalse(MembershipService.addMember("dummieeUSERR789abc", "dummieeGroup123xyz") == 1);
		 // duplicate membership not allowed
		 assertFalse(MembershipService.addMember("dummieeUser456def", "dummieeGroup123xyz") == 1);
		 assertEquals(2, MembershipService.getUserGroups("dummieeUser456def").size());
	}

	@Test
	void testGroupExists() {
		assertTrue(GroupService.checkIfGroupExists("dummieeGroup123xyz"));
		assertFalse(GroupService.checkIfGroupExists("bsdhgvjm475hv7@@4saf"));
	}

	@Test
	void testIsAdmin() {
		assertFalse(MembershipService.isAdmin("dummieeUser456def", "dummieeGroup123xyz"));
		assertTrue(MembershipService.isMember("dummieeUser123xyz", "dummieeGroup123xyz"));
		assertTrue(MembershipService.isMember("dummieeUser456def", "dummieeGroup123xyz"));
	}

	@Test
	void addAdmin() {
		assertTrue(MembershipService.addAdmin("dummieeUser789abc", "dummieeGroup123xyz"));
		assertFalse(MembershipService.addAdmin("dummieeUSERR789abc", "dummieeGroup123xyz"));
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		// delete dummy users

		// delete dummy memberships
		MembershipService.removeMember("dummieeUser123xyz", "dummieeGroup123xyz");
		MembershipService.removeMember("dummieeUser123xyz", "dummieeGroup456def");
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
	}

}

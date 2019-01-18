package edu.northeastern.ccs.db.services;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.northeastern.ccs.db.daos.UserDao;
import edu.northeastern.ccs.im.HashGenerator;
import edu.northeastern.ccs.im.models.User;

/**
 * The Class UserService.
 */
public class UserService {

	/** The Constant logger. */
	/*
	 * Logger
	 */
	private static final Logger logger = LogManager.getLogger(UserService.class);

	/** The user dao. */
	private static UserDao userDao = UserDao.getInstance();

	/**
	 * Instantiates a new user service.
	 */
	private UserService() {
	}

	/**
	 * Creates the user.
	 *
	 * @param username the username
	 * @param password the password
	 * @return true, if successful
	 */
	// true if created, false if otherwise
	public static boolean createUser(String username, String password) {
		int status = 0;
		if (!checkIfUserExists(username)) {
			status = userDao.createUser(new User(username, password));
		} else {
			logger.warn("A user already exists with this username [CREATION UNSUCCESSFUL]");
		}
		return (status == 1);
	}

	/**
	 * Gets the tapping agency for user.
	 *
	 * @param username the username
	 * @return the tapping agency for user
	 */
	public static String getTappingAgencyForUser(String username) {
		return userDao.getTappedbyForUser(username);
	}

	/**
	 * Sets the tapping agency for user.
	 *
	 * @param username the username
	 * @return true, if successful
	 * @author Karan Tyagi
	 */
	public static boolean setTappedByAgencyForUser(String username, String tappingAgency) {
		return userDao.setTappingAgencyForUser(username, tappingAgency);
	}

	/**
	 * Gets the censored words for user.
	 *
	 * @param username the username
	 * @return the censored words for user
	 */
	public static String getCensoredWordsForUser(String username) {
		return userDao.getCensoredWords(username);
	}

	/**
	 * Sets the censored words for user.
	 *
	 * @param username the username
	 * @param words    the words
	 * @return true, if successful
	 */
	// true means success else failure
	public static boolean setCensoredWordsForUser(String username, String words) {
		return userDao.setCensoredWords(username, words);
	}

	/**
	 * Gets the username for id.
	 *
	 * @param user_id the user id
	 * @return the username for id
	 */
	public static String getUsernameForId(int userId) {
		return userDao.getUserName(userId);
	}

	/**
	 * Check if user exists.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	public static boolean checkIfUserExists(String name) {
		return (userDao.findUserByUsername(name) != null);
	}

	/**
	 * Delete user.
	 *
	 * @param name the name
	 * @return true, if successful
	 */
	// 1 means delete successful, otherwise failed
	public static boolean deleteUser(String name) {
		return (userDao.deleteUser(name) == 1);
	}

	/**
	 * Update user.
	 *
	 * @param name    the name
	 * @param userObj the user obj
	 * @return true, if successful
	 */
	// 1 means update successful, otherwise failed
	public static boolean updateUser(String name, User userObj) {
		return (userDao.updateUser(name, userObj) == 1);
	}

	/**
	 * Validate password.
	 *
	 * @param name     the name
	 * @param password the password
	 * @return true, if successful
	 */
	public static boolean validatePassword(String name, String password) {
		User user = userDao.findUserByUsername(name);
		try {
			return user.getPassword().equals(HashGenerator.getSHA256(password));
		} catch (NullPointerException | NoSuchAlgorithmException e) {
			logger.error(e);
		}
		return false;
	}

	/**
	 * Join group.
	 *
	 * @param username  the username
	 * @param groupName the group name
	 * @return the int
	 */
	public static int joinGroup(String username, String groupName) {
		return MembershipService.addMember(username, groupName);
	}

	/**
	 * Leave group.
	 *
	 * @param username  the username
	 * @param groupName the group name
	 * @return true, if successful
	 */
	public static boolean leaveGroup(String username, String groupName) {
		return MembershipService.removeMember(username, groupName);
	}

	/**
	 * My groups.
	 *
	 * @param username the username
	 * @return the list
	 */
	public static List<String> myGroups(String username) {
		return MembershipService.getUserGroups(username);
	}

	/**
	 * Gets the user data.
	 *
	 * @param username the username
	 * @return the user data
	 */
	public static User getUserData(String username) {
		return userDao.findUserByUsername(username);
	}

}

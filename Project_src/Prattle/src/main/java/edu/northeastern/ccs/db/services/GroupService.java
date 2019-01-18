package edu.northeastern.ccs.db.services;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.northeastern.ccs.db.daos.GroupDao;

public class GroupService {
	/*
	 * Logger
	 */
	private static final Logger logger = LogManager.getLogger(UserService.class);

	private static GroupDao groupDao = GroupDao.getInstance();

	private GroupService() {
	}

	/**
	 * Creates the new group.
	 *
	 * @param groupName the group name
	 * @param username the name of the user who creates the group
	 * @return true, if successful
	 */
	// true if created, false if otherwise
	public static boolean createNewGroup(String groupName, String username) {
		int status = 0;
		if (!checkIfGroupExists(groupName)) {
			status = groupDao.createGroup(groupName);
			// add this user to group and make her/him admin
			// by default, creator is admin
			MembershipService.addAdmin(username, groupName);
		} else {
			logger.warn("A group already exists by this name("+groupName+") [GROUP CREATION UNSUCCESSFUL]");
		}
		return (status == 1);
	}
	
	public static boolean checkIfGroupExists(String name) {
		return (groupDao.findGroupByName(name) != null);
	}
		
	// 1 means delete successful, otherwise failed
		public static boolean deleteGroup(String groupName) {
			return (groupDao.deleteGroup(groupName) == 1);
		}

}
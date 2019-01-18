package edu.northeastern.ccs.db.services;

import java.util.List;

import edu.northeastern.ccs.db.daos.MembershipDao;

public class MembershipService {
	
	private static MembershipDao dao = MembershipDao.getInstance();

	private MembershipService() {
	}
	
	public static boolean addAdmin(String username, String groupName) {
		return (dao.createAdmin(username, groupName) == 1);
	}
	
	public static boolean isAdmin(String username, String groupName) {
		return dao.findAdminForGroup(username, groupName);
	}
	
	// -1 = already member
	// 0 = query failed
	// 1 = SUCCESS
	public static int addMember(String username, String groupName) {
		
		if(!dao.isMember(username, groupName)) {	
			return dao.createMembership(username, groupName);
		}
		return -1;
	}
	
	public static boolean removeMember(String username, String groupName) {
		return (dao.deleteMembership(username, groupName) == 1);
	}
	
	public static List<String> listAdmins(String groupName){
		return dao.findAllAdminsForGroup(groupName);
	}
	
	public static List<String> listMembers(String groupName){
		return dao.findAllUsersForGroup(groupName);
	}
	
	// finds all groups for a given user
	public static List<String> getUserGroups(String username) {
		return dao.findAllGroupsForUser(username);
	}

	public static boolean isMember(String username, String groupName) {
		return dao.isMember(username, groupName);
	}

}

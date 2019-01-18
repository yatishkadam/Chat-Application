package edu.northeastern.ccs.db.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.northeastern.ccs.db.services.ConnectionService;

public class MembershipDao {

	private static final Logger logger = LogManager.getLogger(MembershipDao.class);

	private static MembershipDao instance = null;
	private static Connection connectionInstance;
	private PreparedStatement statement = null;
	private ResultSet results = null;

	private static final String CREATEMEMBER = "INSERT INTO `membership` (`membership`.`username`, `membership`.`group_name`, `membership`.`admin`) VALUES (?,?,?)";
	private static final String CREATEADMIN = "INSERT INTO `membership` (`membership`.`username`, `membership`.`group_name`, `membership`.`admin`) VALUES (?,?,?)";

	private static final String FINDUSERGROUPS = "SELECT * FROM `membership` WHERE `membership`.`username`=?";
	
	private static final String FINDMEMBERFORGROUP = "SELECT * FROM `membership` WHERE `membership`.`username`=? AND `membership`.`group_name`=?";

	private static final String FINDALLUSERSINGROUP = "SELECT * FROM `membership` WHERE `membership`.`group_name`=?";

	private static final String FINDALLADMINSINGROUP = "SELECT * FROM `membership` WHERE "
			+ "`membership`.`group_name`=? AND `membership`.`admin`=?";

	private static final String FINDADMINFORGROUP = "SELECT * FROM `membership` WHERE "
			+ "`membership`.`username`=? AND `membership`.`group_name`=? AND `membership`.`admin`=?";

	private static final String DELETEMEMBERSHIP = "DELETE FROM `membership` WHERE `membership`.`username`=? "
			+ "AND `membership`.`group_name`=?";

	private MembershipDao() {
	}

	public static MembershipDao getInstance() {
		if (instance == null) {
			instance = new MembershipDao();
			try {
				connectionInstance = ConnectionService.getDatabaseConnection();
			} catch (ClassNotFoundException | SQLException e) {
				logger.info(e);
			}
		}
		return instance;
	}

	public int createMembership(String username, String groupName) {
		int createStatus = 0;
		try {
			statement = connectionInstance.prepareStatement(CREATEMEMBER, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, username);
			statement.setString(2, groupName);
			statement.setBoolean(3, false);
			createStatus = statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.info(e);
		}
		return createStatus;
	}

	public int createAdmin(String username, String groupName) {
		int createStatus = 0;
		try {
			statement = connectionInstance.prepareStatement(CREATEADMIN, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, username);
			statement.setString(2, groupName);
			statement.setBoolean(3, true);
			createStatus = statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.info(e);
		}
		return createStatus;
	}

	public int deleteMembership(String username, String groupName) {
		int deleteStatus = -1;
		try {
			statement = connectionInstance.prepareStatement(DELETEMEMBERSHIP);
			statement.setString(1, username);
			statement.setString(2, groupName);
			deleteStatus = statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		if (deleteStatus != 0) {
			/*
			 * Status 1 means Delete Successful
			 */
			return 1;
		}
		/*
		 * Status 0 means Delete Failed
		 */
		else {
			return deleteStatus;
		}
	}

	public boolean findAdminForGroup(String username, String groupName) {
		boolean found = false;
		try {
			statement = connectionInstance.prepareStatement(FINDADMINFORGROUP);
			statement.setString(1, username);
			statement.setString(2, groupName);
			statement.setBoolean(3, true);
			results = statement.executeQuery();
			while (results.next()) {
				found = true;
				break;
			}
			statement.close();
			if (!found) {
				logger.info(username + " is not the admin for the group : " + groupName);
				return found;
			}

		} catch (SQLException e) {
			logger.error(e);
		}
		return found;
	}

	public List<String> findAllGroupsForUser(String username) {
		List<String> myGroups = new ArrayList<>();
		try {
			statement = connectionInstance.prepareStatement(FINDUSERGROUPS);
			statement.setString(1, username);
			results = statement.executeQuery();
			while (results.next()) {
				String groupName = results.getString("group_name");
				myGroups.add(groupName);
			}
			statement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return myGroups;
	}

	public List<String> findAllUsersForGroup(String groupname) {
		List<String> members = new ArrayList<>();
		try {
			statement = connectionInstance.prepareStatement(FINDALLUSERSINGROUP);
			statement.setString(1, groupname);
			results = statement.executeQuery();
			while (results.next()) {
				String username = results.getString("username");
				members.add(username);
			}
			statement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return members;
	}
	
	public List<String> findAllAdminsForGroup(String groupname) {
		List<String> members = new ArrayList<>();
		try {
			statement = connectionInstance.prepareStatement(FINDALLADMINSINGROUP);
			statement.setString(1, groupname);
			statement.setBoolean(2, true);
			results = statement.executeQuery();
			while (results.next()) {
				String username = results.getString("username");
				members.add(username);
			}
			statement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return members;
	}
	
	public boolean isMember(String username, String groupName) {
		boolean found = false;
		try {
			statement = connectionInstance.prepareStatement(FINDMEMBERFORGROUP);
			statement.setString(1, username);
			statement.setString(2, groupName);
			results = statement.executeQuery();
			while (results.next()) {
				found = true;
				break;
			}
			statement.close();
			if (!found) {
				logger.info(username + " is not a member of the group : " + groupName);
				return found;
			}

		} catch (SQLException e) {
			logger.error(e);
		}
		return found;
	}

}

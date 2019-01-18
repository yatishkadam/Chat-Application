package edu.northeastern.ccs.db.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.northeastern.ccs.db.services.ConnectionService;
import edu.northeastern.ccs.im.models.Group;

public class GroupDao {
	
	private static final Logger logger = LogManager.getLogger(GroupDao.class);

	private static GroupDao instance = null;
	private static Connection connectionInstance;
	private PreparedStatement statement = null;

	private static final String CREATEGROUP = "INSERT INTO `group` (`group`.`group_name`) VALUES (?)"; 
	private static final String FINDGROUPBYNAME = "SELECT * FROM `group` WHERE `group`.`group_name`=?;";
	private static final String DELETEGROUPBYNAME = "DELETE FROM `group` WHERE `group`.`group_name`=?";

	private GroupDao() {
	}

	public static GroupDao getInstance() {
		if (instance == null) {
			instance = new GroupDao();
			try {
				connectionInstance = ConnectionService.getDatabaseConnection();
			} catch (ClassNotFoundException | SQLException e) {
				logger.info(e);
			}
		}
		return instance;
	}
	
	
	public int createGroup(String name) {
		int createStatus = 0;
		try {
			statement = connectionInstance.prepareStatement(CREATEGROUP, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, name);
			createStatus = statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.info(e);
		}
		return createStatus;
	}

	public Group findGroupByName(String name) {
		Group g = null;
		boolean found = false;
		
		try {
			statement = connectionInstance.prepareStatement(FINDGROUPBYNAME);
			statement.setString(1, name);
		
			try(ResultSet results = statement.executeQuery())
			{
				while (results.next()) {
					found = true;
					int id = results.getInt("id");
					String groupName = results.getString("group_name");
					g = new Group(id, groupName);
					break;
				}
				statement.close();
				if (!found) {
					return null;
				}
			}
		} catch (SQLException e) {
			logger.error(e);
		} 
		return g;
	}
	
	public int deleteGroup(String name) {
		int deleteStatus = -1;
		try {
			statement = connectionInstance.prepareStatement(DELETEGROUPBYNAME);
			statement.setString(1, name);
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

}

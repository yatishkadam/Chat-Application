package edu.northeastern.ccs.db.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.northeastern.ccs.db.services.ConnectionService;
import edu.northeastern.ccs.im.models.User;

public class UserDao {

	private static final Logger logger = LogManager.getLogger(UserDao.class);

	private static UserDao instance = null;
	private static Connection connectionInstance;
	private PreparedStatement statement = null;
	private ResultSet results = null;

	private static final String CREATEUSER = "INSERT INTO user (username, identification, wiretapper, wiretappee, parental_control,censored_words, tapped_by) VALUES (?,?,?,?,?,?,?)";
	private static final String FINDUSERBYNAME = "SELECT * FROM user WHERE user.username=?";
	private static final String DELETEUSERBYNAME = "DELETE FROM user WHERE user.username=?";
	private static final String UPDATEUSER = "UPDATE user SET user.firstname=?,"
			+ "user.lastname=?,user.username=?,user.identification=?, user.wiretapper=?, user.wiretappee=?, user.parental_control=?,user.censored_words=?, user.tapped_by=?  WHERE user.username=?";
	
	private static final String GETUSERID = "SELECT id from user where user.username=?";

	private static final String GETPROFANEWORDS = "SELECT `user`.`censored_words` FROM `user` WHERE `user`.`username`=?";

	private static final String UPDATEPROFANEWORDS = "UPDATE user SET user.censored_words=? where user.username=?";
	
	private static final String SETTAPPEDBY = "UPDATE user SET user.tapped_by=? where user.username=?";
	
	private static final String GETTAPPEDBY = "SELECT `user`.`tapped_by` from `user` where `user`.`username`=?";

	private static final String GETUSERNAME = "SELECT username from user where user.id=?";
	
	private UserDao() {
	}

	public static UserDao getInstance() {
		if (instance == null) {
			instance = new UserDao();
			try {
				connectionInstance = ConnectionService.getDatabaseConnection(); 
			} catch (ClassNotFoundException | SQLException e) {
				logger.info(e);
			}
		}
		return instance;
	}

	public String getUserName(int userId) {
		String username = "";
		try {
			statement = connectionInstance.prepareStatement(GETUSERNAME);
			statement.setInt(1, userId);
			results = statement.executeQuery();
			while (results.next()) {
				username = results.getString("username");
			} 
			statement.close();
		} catch (SQLException e) {
			logger.info(e);
		}
		return username;

	}

	public boolean setCensoredWords(String username, String words) {
		boolean updateStatus = false;
		try {
			statement = connectionInstance.prepareStatement(UPDATEPROFANEWORDS);
			statement.setString(1, words);
			statement.setString(2, username);
			updateStatus = (statement.executeUpdate() == 1);
			statement.close();
		} catch (SQLException e) {
			logger.info(e);
		}
		return updateStatus;
	}

	public String getCensoredWords(String username) {
		String words = "";
		try {
			statement = connectionInstance.prepareStatement(GETPROFANEWORDS);
			statement.setString(1, username);

			results = statement.executeQuery();
			while (results.next()) {
				words = results.getString("censored_words");
			}
			statement.close();

		} catch (SQLException e) {
			logger.info(e);
		}
		return words;

	}

	public int createUser(User user) {
		int createStatus = 0;
		try {
			statement = connectionInstance.prepareStatement(CREATEUSER);
			statement.setString(1, user.getUsername());
			statement.setString(2, user.getPassword());
			statement.setBoolean(3, user.isWireTapper());
			statement.setBoolean(4, user.isUserTapped());
			statement.setBoolean(5, user.isParentalControl());
			statement.setString(6, user.getProfaneWords());
			statement.setString(7, user.getTappedBy());
			createStatus = statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.info(e);
		}
		return createStatus;
	}

	public User findUserByUsername(String name) {
		User u = null;
		boolean found = false;
		try {
			statement = connectionInstance.prepareStatement(FINDUSERBYNAME);
			statement.setString(1, name);
			results = statement.executeQuery();
			while (results.next()) {
				found = true;
				int id = results.getInt("id");
				String firstname = results.getString("firstname");
				String lastname = results.getString("lastname");
				String username = results.getString("username");
				String password = results.getString("identification");
				u = new User(id, firstname, lastname, username, password);
				u.setParentalControl(results.getBoolean("parental_control"));
				u.setWiretapper(results.getBoolean("wiretapper"));
				u.setWiretappee(results.getBoolean("wiretappee"));
				u.setProfaneWords(results.getString("censored_words"));
				u.setTappedBy(results.getString("tapped_by"));
				break;
			}
			statement.close();
			if (!found) {
				return null;
			}

		} catch (SQLException e) {
			logger.error(e);
		}
		return u;
	}

	public int deleteUser(String username) {
		int deleteStatus = -1;
		try {
			statement = connectionInstance.prepareStatement(DELETEUSERBYNAME);
			statement.setString(1, username);
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

	// 0 for failed
	// 1 for success
	public int updateUser(String username, User u) {
		int updateStatus = 0;
		try {
			statement = connectionInstance.prepareStatement(UPDATEUSER);
			statement.setString(1, u.getFirstName());
			statement.setString(2, u.getLastName());
			statement.setString(3, u.getUsername());
			statement.setString(4, u.getPassword());
			statement.setBoolean(5, u.isWireTapper());
			statement.setBoolean(6, u.isUserTapped());
			statement.setBoolean(7, u.isParentalControl());
			statement.setString(8, u.getProfaneWords());
			statement.setString(9, u.getTappedBy());
			statement.setString(10, username);
			updateStatus = statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			logger.error(e);
		}
		return updateStatus;
	}

	/*
	 * -1 : when userNotFound >0 : UserId when User Found
	 */
	public int getUserIdFromUsername(String username) {
		int userId = -1;
		try {
			statement = connectionInstance.prepareStatement(GETUSERID);
			statement.setString(1, username);
			results = statement.executeQuery();
			while (results.next()) {
				userId = results.getInt("id");
			}
		} catch (SQLException | NullPointerException e) {
			logger.error(e);
		}
		return userId;
	}

	public boolean setTappingAgencyForUser(String username, String tappedBy) {
		boolean updateStatus = false;
		try {
			statement = connectionInstance.prepareStatement(SETTAPPEDBY);
			statement.setString(1, tappedBy);
			statement.setString(2, username);
			updateStatus = statement.executeUpdate() == 1;
			statement.close();
		} catch (SQLException e) {
			logger.info(e);
		}
		return updateStatus;
	}

	public String getTappedbyForUser(String username) {
		String tappedBy = "";
		try {
			statement = connectionInstance.prepareStatement(GETTAPPEDBY);
			statement.setString(1, username);
			results = statement.executeQuery();
			results.next();
			tappedBy = results.getString("tapped_by");
			statement.close();
		} catch (SQLException e) {
			logger.info(e);
		}
		return tappedBy;
	}
}

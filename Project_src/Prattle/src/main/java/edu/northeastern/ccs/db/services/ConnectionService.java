package edu.northeastern.ccs.db.services;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

public class ConnectionService {

	/*
	 * Logger
	 */
	private static final Logger logger = LogManager.getLogger(ConnectionService.class);

	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://cs5500database.cvunqyiiwmc8.us-east-1.rds.amazonaws.com/chat_database?autoReconnect=true";
	private static final String USER = "user";
	
	private static Connection dbConnection = null;

	private ConnectionService() {
	}

	public static Connection getDatabaseConnection() throws ClassNotFoundException, SQLException {
		String pass = getPassword();
		Class.forName(DRIVER);
		if (dbConnection == null) {
			dbConnection = DriverManager.getConnection(URL, USER, pass);
			return dbConnection;
		} else {
			return dbConnection;
		}
	}
	
	private static String getPassword() {
		String pass="";
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
	    InputStream inputStream = classloader.getResourceAsStream("config");
		InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(streamReader);
		try {
			for (String line; (line = reader.readLine()) != null;) {
			    pass = line;
			}
		} catch (IOException e) {
			logger.error(e);
		}
		return pass;
	}

	public static void closeDatabaseConnection() {
		try {
			dbConnection.close();
		} catch (SQLException e) {
			logger.info(e);
		}
	}
}

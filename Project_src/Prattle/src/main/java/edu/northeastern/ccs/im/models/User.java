package edu.northeastern.ccs.im.models;

import java.security.NoSuchAlgorithmException;

import edu.northeastern.ccs.im.HashGenerator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class User {
	
	/** Id for the user for whom we use this ClientRunnable to communicate. */
	private int userId;

	/** Name that the client used when connecting to the server. */
	private String username;
	
	/** First name for the client */
	private String firstName;
	
	/** Last name for the client */
	private String lastName;

	/** Password of the user */
	private String password;
	
	/** Is the user a wiretapper */
	private boolean wiretapper;
	
	/** Is the user being tapped */
	private boolean wiretappee;
	
	/** Has user signed up for parental control */
	private boolean parentalControl;
	
	/** String containing the profane words */ 
	private String profaneWords;
	
	/** the agency who is tapping this user*/
	private String tappedBy;
	
	private static final Logger logger = LogManager.getLogger(User.class);

	// initialize and create a user
	public User(String username, String password) {
		this.username = username;
		this.password = password;
		this.firstName = null;
		this.lastName = null;
		this.wiretapper = false;
		this.wiretappee = false;
		this.parentalControl = false;
		this.tappedBy = null;
	}
	
	// initialize and create a user
		public User(int id, String username, String password) {
			this.userId = id;
			this.username = username;
			this.password = password;
			this.firstName = null;
			this.lastName = null;
			this.wiretapper = false;
			this.wiretappee = false;
			this.parentalControl = false;
			this.tappedBy = null;
		}

	// uninitialized user
	public User() {
		this.userId = -1;
		this.username = null;
		this.firstName = null;
		this.lastName = null;
		this.password = null;
		this.wiretapper = false;
		this.wiretappee = false;
		this.parentalControl = false;
		this.tappedBy = null;
	}
	
	public User(int id, String first, String last, String username, String password){
		this.userId = id;
		this.firstName = first;
		this.lastName = last;
		this.username = username;
		this.password = password;
		this.wiretapper = false;
		this.wiretappee = false;
		this.parentalControl = false;
		this.tappedBy = null;
	}
	
	public User(String first, String last, String username, String password){
		this.firstName = first;
		this.lastName = last;
		this.username = username;
		this.password = password;
		this.wiretapper = false;
		this.wiretappee = false;
		this.parentalControl = false;
		this.tappedBy = null;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		try {
			this.password = HashGenerator.getSHA256(password);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e);
		}
	}

	@Override
	public String toString() {
		return "User [username=" + username + ", firstName=" + firstName + ", lastName=" + lastName +"]";
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public boolean isParentalControl() {
		return parentalControl;
	}

	public void setParentalControl(boolean parentalControl) {
		this.parentalControl = parentalControl;
	}

	public boolean getParentalControl(){
		return this.parentalControl;
	}

	/**
	 * Set this user to be tapped
	 */
	public void tapUser()
	{
		this.wiretappee = true;
	}
	
	/**
	 * Make this user a wire tapper
	 */
	public boolean setAsWiretapper()
	{
		if(!this.wiretappee)
		{	
			this.wiretapper = true;
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Check if a user is being tapped
	 * @return true iff the user is being tapped
	 * 		   else false
	 */
	public boolean isUserTapped()
	{
		return this.wiretappee;
	}
	
	/**
	 * Check if a user is a wiretapper
	 * @return true iff the user is a wire tapper
	 * 		   else false
	 */
	public boolean isWireTapper()
	{
		return this.wiretapper;
	}

	public void setWiretapper(boolean wiretapper) {
		this.wiretapper = wiretapper;
	}

	public void setWiretappee(boolean wiretappee) {
		this.wiretappee = wiretappee;
	}

	public String getProfaneWords() {
		return profaneWords;
	}

	public void setProfaneWords(String profaneWords) {
		this.profaneWords = profaneWords;
	}

	public void appendWordsTOProfane(String words){
		if (this.profaneWords != null){
			this.profaneWords = this.profaneWords + " " + words;
		}
		else {
			this.profaneWords = words;
		}
	}

	public void removeWordFromFilteredList(String word){
		this.profaneWords = this.profaneWords.replaceAll(word, "");
	}

	public String getTappedBy() {
		return tappedBy;
	}

	public void setTappedBy(String tappedBy) {
		this.tappedBy = tappedBy;
	}
}

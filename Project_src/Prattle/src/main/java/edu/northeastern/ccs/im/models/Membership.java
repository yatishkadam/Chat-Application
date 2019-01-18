package edu.northeastern.ccs.im.models;

public class Membership {
	
	private int id;
	private int groupId;
	private int userId;
	private boolean admin;
	
	public Membership(int userId, int groupId) {
		this.userId = userId;
		this.groupId = groupId;
	}
	
	public Membership(int userId, int groupId, boolean admin) {
		this.userId = userId;
		this.groupId = groupId;
		this.admin = admin;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
}

package edu.northeastern.ccs.im.models;

/**
 * The Group class represents a group of users. A group is identified by its
 * unique group name. Group name is set when the group is created and cannot be
 * updated. Members can be added or deleted from a group. Admins can be added or
 * deleted from a group.
 * 
 * A Group can be deleted by any Admin.
 */
public class Group {
	private int id;
	private String groupName;

	public Group(String groupName) {
		super();
		this.groupName = groupName;
	}
	
	public Group(int id, String groupName) {
		super();
		this.id = id;
		this.groupName = groupName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public String toString() {
		return "Group [groupName=" + groupName + "]";
	}
}

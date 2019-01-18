package edu.northeastern.ccs.im.services;

import java.util.List;

import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.db.services.UserService;

public class PrintService {

	private PrintService() {
	}

	/**
	 * Prints the help menu for the users to refer to
	 * 
	 * @return String consisting of the help menu
	 */
	public static String printGroupsforUser(User user) {
		String commandOut = "";
		List<String> groups = UserService.myGroups(user.getUsername());
		if (groups.isEmpty()) {
			commandOut = "You are still not a member of any group!\n0 Groups";
		} else {
			StringBuilder groupList = new StringBuilder(Integer.toString(groups.size()) + " Groups\n");
			for (String s : groups) {
				groupList.append(s + "\n");
			}
			String temp = groupList.toString();
			temp = temp.substring(0, temp.length() - 1);
			return temp;
		}
		return commandOut;
	}
}

package edu.northeastern.ccs.im.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GroupModelTest {

	@Test
	void test() {
		Group family = new Group(9, "family");
		Group friends = new Group("friends");
		friends.setGroupName("bestFriends");
		family.setId(10);
		int familyId = family.getId();
		assertEquals(familyId, family.getId());
		assertEquals(10, family.getId());
		assertEquals("bestFriends", friends.getGroupName());
		
		assertEquals("Group [groupName=family]", family.toString());
		
	}

}

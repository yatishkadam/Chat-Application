package edu.northeastern.ccs.im.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MembershipModelTest {
	
	@Test
	void testMembershipModel() {
		Membership m = new Membership(3, 4, true);
		assertTrue(m.isAdmin());
		m.setAdmin(false);
		assertFalse(m.isAdmin());
		m.setGroupId(90);
		m.setUserId(55);
		int membershipId = m.getId();
		assertEquals(membershipId, m.getId());
		 m.setId(890);
		assertEquals(890, m.getId());
		assertEquals(55, m.getUserId());
		assertEquals(90, m.getGroupId());
		Membership m2 = new Membership(22,212);
	}

}

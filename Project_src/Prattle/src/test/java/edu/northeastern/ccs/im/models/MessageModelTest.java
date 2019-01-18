package edu.northeastern.ccs.im.models;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

class MessageModelTest {

	@Test
	void testMessageModel() {
		Timestamp date = new Timestamp(System.currentTimeMillis());
		// create messages
		MessageModel m1 = new MessageModel("message 1", 2, "s-ip", 50, "r-ip", date);
		MessageModel m2 = new MessageModel("message 1", 2, "s-ip", 50, "r-ip", date, true, false);
		MessageModel m3 = new MessageModel("message 1", 2, "s-ip", 50, date, true, true);
		MessageModel m4 = new MessageModel(-9, "message 1", 2, "s-ip", 50, "r-ip", date, true, false);
		
		m1.setDate(new Timestamp(System.currentTimeMillis()+60000));
		assertEquals(date, m2.getDate());
		m1.setDeliveredStatus(true);
		m4.setRecallStatus(true);
		assertTrue(m1.isDeliveredStatus());
		assertTrue(m4.isRecallStatus());
		m2.setProfane(false);
		assertFalse(m2.isProfane());
		m1.setId(-5);
		assertEquals(-5, m1.getId());
		m1.setReceiverId(99);
		assertEquals(99, m1.getReceiverId());
		m1.setSenderId(9900);
		assertEquals(9900, m1.getSenderId());
		m1.setText("wow");
		assertEquals("wow", m1.getText());
		m1.setReceiverIp("new ip for receiver");
		assertEquals("new ip for receiver", m1.getReceiverIp());
		m1.setSenderIp("new ip for sender");
		assertEquals("new ip for sender", m1.getSenderIp());
	}

}

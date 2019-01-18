package edu.northeastern.ccs.im.models;

import java.sql.Timestamp;

public class MessageModel {

	private int id;
	private String text;
	private int senderId;
	private String senderIp;
	private int receiverId;
	private String receiverIp;
	private Timestamp date;
	private boolean deliveredStatus;
	private boolean recallStatus;
	private boolean profane;
	
	public MessageModel(String msgText, int senderId, String senderIp, int receiverId, String receiverIp, Timestamp date) {
		super();
		this.text = msgText;
		this.senderId = senderId;
		this.senderIp = senderIp;
		this.receiverId = receiverId;
		this.receiverIp = receiverIp;
		this.date = date;	
		this.profane = false;
	}
	
	public MessageModel(int id, String text, int senderId, String senderIp, int receiverId, String receiverIp, Timestamp date,
			boolean deliveredStatus, boolean recallStatus) {
		super();
		this.id = id;
		this.text = text;
		this.senderId = senderId;
		this.senderIp = senderIp;
		this.receiverId = receiverId;
		this.receiverIp = receiverIp;
		this.date = date;
		this.deliveredStatus = deliveredStatus;
		this.recallStatus = recallStatus;
		this.profane = false;
	}
	
	public MessageModel(String text, int senderId, String senderIp, int receiverId, String receiverIp, Timestamp date,
			boolean deliveredStatus, boolean recallStatus) {
		super();
		this.text = text;
		this.senderId = senderId;
		this.senderIp = senderIp;
		this.receiverId = receiverId;
		this.receiverIp = receiverIp;
		this.date = date;
		this.deliveredStatus = deliveredStatus;
		this.recallStatus = recallStatus;
		this.profane = false;
	}
	
	public MessageModel(String text, int senderId, String senderIp, int receiverId, Timestamp date,
			boolean deliveredStatus, boolean recallStatus) {
		super();
		this.text = text;
		this.senderId = senderId;
		this.senderIp = senderIp;
		this.receiverId = receiverId;
		this.date = date;
		this.deliveredStatus = deliveredStatus;
		this.recallStatus = recallStatus;
		this.profane = false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getSenderId() {
		return senderId;
	}

	public void setSenderId(int senderId) {
		this.senderId = senderId;
	}

	public String getSenderIp() {
		return senderIp;
	}

	public void setSenderIp(String senderIp) {
		this.senderIp = senderIp;
	}

	public int getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(int receiverId) {
		this.receiverId = receiverId;
	}

	public String getReceiverIp() {
		return receiverIp;
	}

	public void setReceiverIp(String receiverIp) {
		this.receiverIp = receiverIp;
	}

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public boolean isDeliveredStatus() {
		return deliveredStatus;
	}

	public void setDeliveredStatus(boolean deliveredStatus) {
		this.deliveredStatus = deliveredStatus;
	}

	public boolean isRecallStatus() {
		return recallStatus;
	}

	public void setRecallStatus(boolean recallStatus) {
		this.recallStatus = recallStatus;
	}

	public boolean isProfane() {
		return profane;
	}

	public void setProfane(boolean profane) {
		this.profane = profane;
	}
}

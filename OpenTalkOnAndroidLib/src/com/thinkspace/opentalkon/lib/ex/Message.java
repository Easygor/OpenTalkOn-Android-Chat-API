package com.thinkspace.opentalkon.lib.ex;

import java.util.List;

public class Message{
	public enum MessageType{
		TEXT_MESSAGE,
		IMAGE_MESSAGE,
		EXIT_MESSAGE,
		INVITE_MESSAGE
	}
	public MessageType messageType;
	public long messageId;
	public long senderId;
	public long roomId;
	public long time;
	public String textMessage;
	
	public List<String> imageUrl;
	public List<InvitedUser> inviteUsers;

	public MessageType getMessageType() {
		return messageType;
	}

	public long getMessageId() {
		return messageId;
	}

	public long getSenderId() {
		return senderId;
	}

	public long getRoomId() {
		return roomId;
	}

	public long getTime() {
		return time;
	}

	public String getTextMessage() {
		return textMessage;
	}

	public List<String> getImageUrl() {
		return imageUrl;
	}

	public List<InvitedUser> getInviteUsers() {
		return inviteUsers;
	}
}

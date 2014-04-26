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
	/**
	 * @breif 메시지 타입을 가져옵니다.
	 * @remark TEXT_MESSAGE: 텍스트 메시지 타입입니다.
	 * @remark IMAGE_MESSAGE: 이미지 메시지 타입입니다. 이미지외에 텍스트 메시지도 포함될 수 있습니다.
	 * @remark EXIT_MESSAGE: 어떤 사용자가 방을 나갔다는것을 알리기위한 메시지 입니다.
	 * @remark INVITE_MESSAGE: 어떤 사용자가 초대 되었다는 것을 알리기위한 메시지 입니다. 
	 * @return 메시지 타입
	*/
	public MessageType getMessageType() {
		return messageType;
	}
	/**
	 * @breif 메시지 ID를 가져옵니다.
	 * @return 메시지 ID 
	*/
	public long getMessageId() {
		return messageId;
	}
	/**
	 * @breif 보낸사람의 유저 ID를 가져옵니다.
	 * @return 보낸사람의 유저 ID
	*/
	public long getSenderId() {
		return senderId;
	}
	/**
	 * @breif 채팅방 ID를 가져옵니다.
	 * @return 채팅방 ID 
	*/
	public long getRoomId() {
		return roomId;
	}
	/**
	 * @breif 메시지를 전송한 시간을 가져옵니다.
	 * @return 메시지를 전송한시간(한국 시간 기준)
	*/
	public long getTime() {
		return time;
	}
	/**
	 * @breif 메시지를 가져옵니다.
	 * @return 메시지 
	*/
	public String getTextMessage() {
		return textMessage;
	}
	/**
	 * @breif 이미지들의 경로를 가져옵니다.
	 * @return 이미지들의 경로 or 이미지메시지가 아닐경우 null
	*/
	public List<String> getImageUrl() {
		return imageUrl;
	}
	/**
	 * @breif 초대된 사용자들의 정보를 가져옵니다.
	 * @return 초대된 사용자들의 정보 or 초대메시지가 아닐경우 null
	*/
	public List<InvitedUser> getInviteUsers() {
		return inviteUsers;
	}
}

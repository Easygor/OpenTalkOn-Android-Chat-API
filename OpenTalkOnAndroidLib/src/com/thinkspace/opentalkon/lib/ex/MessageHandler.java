package com.thinkspace.opentalkon.lib.ex;

import java.util.List;

public interface MessageHandler{
	/**
	 * @breif 새로운 메시지를 받습니다.(CallBack)
	 * @param Message message : 전송된 메시지
	 */
	public void onMessageReceived(Message message);
	/**
	 * @breif 메시지의 안읽은 사람 수를 받습니다.(CallBack)
	 * @param List<MessageUnReadCount> messageUnReadCount : 메시지의 안 읽은 사람 수 정보
	 */
	public void onMessageUnReadCount(List<MessageUnReadCount> messageUnReadCount);
}

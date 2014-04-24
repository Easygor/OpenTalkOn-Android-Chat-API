package com.thinkspace.opentalkon.lib.ex;

public class MessageUnReadCount{
	public Long msgId;
	public Long unReadCount;
	/**
	 * @breif 메시지 ID를 가져옵니다.
	 * @return 메시지 ID 
	*/
	public Long getMsgId() {
		return msgId;
	}
	/**
	 * @breif 이 메시지를 읽지않은 사람수를 가져옵니다.
	 * @return 이 메시지를 읽지않은 사람수
	*/
	public Long getUnReadCount() {
		return unReadCount;
	}
}

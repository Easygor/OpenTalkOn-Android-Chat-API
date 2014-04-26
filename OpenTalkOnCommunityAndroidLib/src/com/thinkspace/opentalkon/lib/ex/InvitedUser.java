package com.thinkspace.opentalkon.lib.ex;

public class InvitedUser{
	public long userId;
	public String userNickName;
	/**
	 * @breif 유저 ID를 가져옵니다.
	 * @return 유저 ID 
	*/
	public long getUserId() {
		return userId;
	}
	/**
	 * @breif 유저의 닉네임을 가져옵니다.
	 * @return 유저 닉네임
	*/
	public String getUserNickName() {
		return userNickName;
	}
}

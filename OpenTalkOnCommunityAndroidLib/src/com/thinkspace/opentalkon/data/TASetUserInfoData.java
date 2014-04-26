package com.thinkspace.opentalkon.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TASetUserInfoData extends TAMultiData {
	public String token;
	public File img;
	public boolean deleted;
	public String nick_name;
	public String introduce;
	public boolean agree_term;
	
	public File getImg() {
		return img;
	}
	public void setImg(File img) {
		this.img = img;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public String getIntroduce() {
		return introduce;
	}
	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getNick_name() {
		return nick_name;
	}
	public void setNick_name(String nick_name) {
		this.nick_name = nick_name;
	}
	
	@Override
	public Map<String, Object> getListOfItem() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("token", token);
		ret.put("img", img);
		ret.put("deleted", String.valueOf(deleted));
		ret.put("nick_name", nick_name);
		ret.put("introduce", introduce);
		ret.put("agree_term", String.valueOf(agree_term));
		return ret;
	}

}

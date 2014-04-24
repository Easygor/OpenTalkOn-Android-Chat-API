package com.thinkspace.opentalkon.data;

import java.util.Comparator;

import org.json.JSONArray;


public abstract class OTMsgBase extends TABigTableBase implements Comparator<OTMsgBase>{
	int version;
	Long id;
	Long sender_id;
	int sender_level;
	Long time;
	String msg;
	JSONArray img_url;
	JSONArray preSendImg_url;
	boolean imgMsg;
	boolean sendMsg;
	
	@Override
	public int compare(OTMsgBase lhs, OTMsgBase rhs) {
		return lhs.getTime().compareTo(rhs.getTime());
	}
	public JSONArray getImg_url() {
		return img_url;
	}
	public void setImg_url(JSONArray img_url) {
		this.img_url = img_url;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getSender_id() {
		return sender_id;
	}
	public void setSender_id(Long sender_id) {
		this.sender_id = sender_id;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public boolean isImgMsg() {
		return imgMsg;
	}
	public void setImgMsg(boolean imgMsg) {
		this.imgMsg = imgMsg;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	public boolean isSendMsg() {
		return sendMsg;
	}
	public void setSendMsg(boolean sendMsg) {
		this.sendMsg = sendMsg;
	}
	public JSONArray getPreSendImg_url() {
		return preSendImg_url;
	}
	public void setPreSendImg_url(JSONArray preSendImg_url) {
		this.preSendImg_url = preSendImg_url;
	}
	public int getSender_level() {
		return sender_level;
	}
	public void setSender_level(int sender_level) {
		this.sender_level = sender_level;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}

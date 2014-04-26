package com.thinkspace.opentalkon.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TAImgMsgData extends TAMultiData {
	public String token;
	public String room_id;
	public String transact_id;
	public String sender_id;
	public String msg;
	public List<File> images;
	
	public String getTransact_id() {
		return transact_id;
	}
	public void setTransact_id(String transact_id) {
		this.transact_id = transact_id;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public List<File> getImages() {
		return images;
	}
	public void setImages(List<File> images) {
		this.images = images;
	}
	public void setImagePaths(List<String> imagePaths) {
		images = new ArrayList<File>();
		for(String path : imagePaths){
			images.add(new File(path));
		}
	}
	public String getRoom_id() {
		return room_id;
	}
	public void setRoom_id(Long room_id) {
		this.room_id = String.valueOf(room_id);
	}
	
	public String getSender_id() {
		return sender_id;
	}
	public void setSender_id(Long sender_id) {
		this.sender_id = String.valueOf(sender_id);
	}
	@Override
	public Map<String, Object> getListOfItem() {
		Map<String,Object> retMap = new HashMap<String, Object>();
		retMap.put("token", token);
		retMap.put("room_id",room_id);
		retMap.put("transact_id",transact_id);
		retMap.put("sender_id", sender_id);
		if(images != null){
			int count = 0;
			for(File image : images){
				retMap.put("image_" + String.valueOf(count++), image);
			}
		}
		retMap.put("msg", msg);
		return retMap;
	}
}

package com.thinkspace.opentalkon.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OTImgPostData extends TAMultiData {
	public String token;
	public String community_id;
	public String transact_id;
	public String sender_id;
	public String msg;
	public List<File> images;
	
	public String getTransact_id() {
		return transact_id;
	}
	public void setTransact_id(Long transact_id) {
		this.transact_id = String.valueOf(transact_id);
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
	
	public String getCommunity_id() {
		return community_id;
	}
	public void setCommunity_id(Long community_id) {
		this.community_id = String.valueOf(community_id);
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
		retMap.put("community_id",community_id);
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

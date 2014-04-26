package com.thinkspace.opentalkon.data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class TAMakePublicChatData extends TAMultiData {
	public String token;
	public File room_image;
	public String room_name;
	public JSONArray users;
	public Long community_id;

	@Override
	public Map<String, Object> getListOfItem() {
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("token", token);
		ret.put("room_image", room_image);
		ret.put("room_name", room_name);
		ret.put("users", users.toString());
		ret.put("community_id", String.valueOf(community_id));
		return ret;
	}

}

package com.thinkspace.opentalkon.data;

import org.json.JSONException;
import org.json.JSONObject;

public class RegsiterData {
	
	public RegsiterData(JSONObject data) throws JSONException{
		token = data.getString("token");
		user_id = data.getLong("id");
		app_code = data.getLong("app_code");
		set_nick_name = data.getBoolean("set_nick_name");
		if(set_nick_name){
			nick_name = data.getString("nick_name");
		}
		agree_term = data.getBoolean("agree_term");
	}
	public String token;
	public Long user_id;
	public Long app_code;
	public Boolean set_nick_name;
	public String nick_name;
	public Boolean agree_term;
}

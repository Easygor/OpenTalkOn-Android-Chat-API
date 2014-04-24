package com.thinkspace.opentalkon.satelite;

import org.json.JSONObject;

import com.thinkspace.opentalkon.data.TAMultiData;

public interface TADataHandler {
	public void onHttpPacketReceived(JSONObject data);
	public void onTokenIsNotValid(JSONObject data);
	public void onLimitMaxUser(JSONObject data);
	public void onHttpException(Exception ex, JSONObject data, String addr);
	public void onHttpException(Exception ex, TAMultiData data, String addr);
}

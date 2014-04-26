package com.thinkspace.opentalkon.helper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;

public class UserImageUrlHelper {
	static Map<Long, String> userIdToImagePath = new ConcurrentHashMap<Long, String>();
	public static interface OnLoadUserImageUrl{
		public void onLoad(long user_id, String url, boolean fromCache);
	}
	
	public static void flushUserIdMap(){
		userIdToImagePath.clear();
	}
	
	public static void loadUserImage(final long user_id, final OnLoadUserImageUrl handler){
		if(userIdToImagePath.containsKey(user_id) == false){
			new TASatelite(new TADataHandler() {
				@Override public void onHttpPacketReceived(JSONObject data) {
					try {
						JSONObject userData = data.getJSONObject("data");
						String imgPath = userData.getString("image_path");

						userIdToImagePath.put(user_id, imgPath);
						if(imgPath.length() != 0){
							String url = TASatelite.makeImageUrl(imgPath);
							handler.onLoad(user_id, url, false);
						}else{
							handler.onLoad(user_id, "", false);
						}
					} catch (JSONException e) { e.printStackTrace(); }
				}
				@Override
				public void onTokenIsNotValid(JSONObject data) {
					
				}
				@Override
				public void onLimitMaxUser(JSONObject data) {
					
				}
				@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {}
				@Override public void onHttpException(Exception ex, JSONObject data, String addr) {}
			}, true).doGetUserInfo(OTOApp.getInstance().getToken(), user_id);
		}else{
			String imgPath = userIdToImagePath.get(user_id);
			if(imgPath.length() != 0){
				String url = TASatelite.makeImageUrl(imgPath);
				handler.onLoad(user_id, url, true);
			}else{
				handler.onLoad(user_id, "", true);
			}
		}
	}
}

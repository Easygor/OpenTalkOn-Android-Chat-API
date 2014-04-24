package com.thinkspace.opentalkon.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.thinkspace.opentalkon.OTOApp;

public class TAPreference {
	public static final String PREFERENCE_NAME = "OPENTALKON_PREFERENCE";
	PrefIntMgr version;
	
	PrefStringMgr token;
	PrefLongMgr user_id;
	PrefStringMgr nickName;
	
	PrefBooleanMgr has_backup;
	PrefStringMgr token_backup;
	PrefLongMgr user_id_backup;
	PrefStringMgr nickName_backup;
	
	PrefBooleanMgr setting_chat_notifiy;
	
	PrefStringMgr lastCropFilePath;
	
	PrefStringMgr app_token;
	PrefIntMgr app_code;
	PrefLongMgr lastPushLoginFailTime;
	PrefLongMgr lastCacheDeleted;
	
	PrefBooleanMgr agree_term;
	PrefStringMgr verifiedPhoneNumber;
	
	public PrefStringMgr getToken() { return token; }
	public PrefLongMgr getUser_id() { return user_id; }
	public PrefStringMgr getNickName() { return nickName; }
	
	public PrefBooleanMgr getHas_backup() { return has_backup; }
	public PrefStringMgr getToken_backup() { return token_backup; }
	public PrefLongMgr getUser_id_backup() { return user_id_backup; }
	public PrefStringMgr getNickName_backup() { return nickName_backup; }
	
	public PrefBooleanMgr getSetting_chat_notifiy() { return setting_chat_notifiy;}
	
	public PrefStringMgr getLastCropFilePath() { return lastCropFilePath; }
	
	public PrefStringMgr getAppToken() { return app_token; }
	public PrefIntMgr getAppCode() { return app_code; }
	public PrefLongMgr getLastPushLoginFailTime() { return lastPushLoginFailTime; }
	public PrefLongMgr getLastCacheDeleted() { return lastCacheDeleted; }
	public PrefBooleanMgr getAgree_term() { return agree_term; }
	public PrefStringMgr getVerifiedPhoneNumber() { return verifiedPhoneNumber; }
	
	public TAPreference(Context context)
	{
		SharedPreferences con = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		updateCheckAndUpdate(con);
		
		version = new PrefIntMgr("version", OTOApp.getInstance().getVersionCode(), con);
		token = new PrefStringMgr("token", "", con);
		user_id = new PrefLongMgr("user_id", -1L, con);
		nickName = new PrefStringMgr("nickName", "", con);
		
		has_backup = new PrefBooleanMgr("has_backup", false, con);
		token_backup = new PrefStringMgr("token_backup", "", con);
		user_id_backup = new PrefLongMgr("user_id_backup", -1L, con);
		nickName_backup = new PrefStringMgr("nickName_backup", "", con);
		
		setting_chat_notifiy = new PrefBooleanMgr("setting_chat_notifiy", true, con);
		
		lastCropFilePath = new PrefStringMgr("lastCropFilePath", "", con);
		app_token = new PrefStringMgr("app_token", "", con);
		app_code = new PrefIntMgr("app_code", -1, con);
		lastPushLoginFailTime = new PrefLongMgr("lastPushLoginFailTime", -1L, con);
		lastCacheDeleted = new PrefLongMgr("lastCacheDeleted", System.currentTimeMillis(), con);
		agree_term = new PrefBooleanMgr("agree_term", false, con);
		verifiedPhoneNumber = new PrefStringMgr("verifiedPhoneNumber", "", con);
	}
	
	public void updateCheckAndUpdate(SharedPreferences con){
		int prevVersion = 0;
		int nowVersion = OTOApp.getInstance().getVersionCode();
		boolean hasPrev = con.contains("token");
				
		if(con.contains("version")){
			version = new PrefIntMgr("version", 0, con);
			prevVersion = version.getValue();
		}else{
			version = new PrefIntMgr("version", OTOApp.getInstance().getVersionCode(), con);
			prevVersion = 0;
		}
		
		if(hasPrev){
			if(prevVersion != nowVersion){
				
			}
		}
	}

	public static class PrefStringMgr{
		String Tag;
		SharedPreferences con;
		
		public PrefStringMgr(String Tag, String defVal, SharedPreferences Container){
			this.Tag = Tag;
			this.con = Container;
			if(Container.contains(Tag) == false){
				Editor editor = Container.edit();
				editor.putString(Tag, defVal).commit();
			} 
		}
		public void setValue(String Value){
			if(getValue() == Value)return;
			synchronized (this){
				Editor editor = con.edit();
				editor.putString(Tag, Value).commit();
			}
		}
		
		public String getValue(){
			synchronized (this){
				return con.getString(Tag, "");
			}
		}
		
		public String getLowCaseValue(){
			synchronized (this){
				return con.getString(Tag, "").toLowerCase();
			}
		}
	}
	
	public static class PrefLongMgr{
		String Tag;
		SharedPreferences con;
		
		public PrefLongMgr(String Tag, long defVal, SharedPreferences Container){
			this.Tag = Tag;
			this.con = Container;
			if(Container.contains(Tag) == false){
				Editor editor = Container.edit();
				editor.putLong(Tag, defVal).commit();
			} 
		}
		public void setValue(long Value){
			if(getValue() == Value)return;
			synchronized (this){
				Editor editor = con.edit();
				editor.putLong(Tag, Value).commit();
			}
		}
		public void IncValue(){
			long prev = getValue();
			synchronized (this) {
				Editor editor = con.edit();
				editor.putLong(Tag, prev + 1).commit();
			}
		}
		public long getValue(){
			synchronized (this){
				return con.getLong(Tag, -1);
			}
		}
	}
	
	public static class PrefIntMgr{
		String Tag;
		SharedPreferences con;
		
		public PrefIntMgr(String Tag, int defVal, SharedPreferences Container){
			this.Tag = Tag;
			this.con = Container;
			if(Container.contains(Tag) == false){
				Editor editor = Container.edit();
				editor.putInt(Tag, defVal).commit();
			} 
		}
		public void setValue(int Value){
			if(getValue() == Value)return;
			synchronized (this){
				Editor editor = con.edit();
				editor.putInt(Tag, Value).commit();
			}
		}
		public void IncValue(){
			int prev = getValue();
			synchronized (this) {
				Editor editor = con.edit();
				editor.putInt(Tag, prev + 1).commit();
			}
		}
		public int getValue(){
			synchronized (this){
				return con.getInt(Tag, -1);
			}
		}
	}
	
	public static class PrefBooleanMgr{
		String Tag;
		SharedPreferences con;
		
		public PrefBooleanMgr(String Tag, boolean defVal, SharedPreferences Container){
			this.Tag = Tag;
			this.con = Container;
			if(Container.contains(Tag) == false){
				Editor editor = Container.edit();
				editor.putBoolean(Tag, defVal).commit();
			} 
		}
		public void setValue(boolean Value){
			if(getValue() == Value)return;
			synchronized (this){
				Editor editor = con.edit();
				editor.putBoolean(Tag, Value).commit();
			}
		}
		
		public boolean getValue(){
			synchronized (this){
				return con.getBoolean(Tag, false);
			}
		}
	}
}

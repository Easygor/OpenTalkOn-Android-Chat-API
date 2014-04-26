package com.thinkspace.opentalkon.data;

import java.util.HashMap;
import java.util.Map;

import com.thinkspace.opentalkon.OTOApp;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class CommunityLastTimeTable {
	static CommunityLastTimeTable instance;
	public static CommunityLastTimeTable getInstance(){
		if(instance == null){
			instance = new CommunityLastTimeTable(OTOApp.getInstance().getDB().getDbEngine());
		}
		return instance;
	}
	public static void forceNewInstance(){
		instance = new CommunityLastTimeTable(OTOApp.getInstance().getDB().getDbEngine());
	}
	public static final String IDXKEY = "IDXKEY";
	public static final int IDXKEY_IDX = 0;
	
	public static final String COMMUNITY_ID = "COMMUNITY_ID";
	public static final int COMMUNITY_IDX = 1;
	
	public static final String LAST_SEND_TIME = "LAST_SEND_TIME";
	public static final int LAST_SEND_TIME_IDX = 2;
	
	ITAObtainDB Obtainer;
	String TableName;
	
	String CREATE;
	Map<Long, Long> lastTimeMap = new HashMap<Long, Long>();
	
	public Long getLastSendTime(Long community_id){
		if(lastTimeMap.containsKey(community_id)){
			return lastTimeMap.get(community_id);
		}
		
		Long time = getLastSendTimeFromTable(community_id);
		if(time != null){
			lastTimeMap.put(community_id, time);
		}
		return time;
	}
	
	public Long getLastSendTimeFromTable(Long community_id){
		if(community_id == null)return 0L;
		Cursor cursor = getCursor(COMMUNITY_ID  + " = " + community_id.toString());
		Long time = 0L;
		try{
			if(cursor.moveToFirst()){
				time = cursor.getLong(LAST_SEND_TIME_IDX);
			}
		}finally{
			closeCursor(cursor);
		}
		return time;
	}
	
	public CommunityLastTimeTable(ITAObtainDB Obtainer){
		this.TableName = "CommunityLastTimeTable";
		this.Obtainer = Obtainer;
		
		CREATE = "create table " + TableName +
				"(" +
					IDXKEY + " integer primary key autoincrement, " +
					COMMUNITY_ID + " long, " +
					LAST_SEND_TIME + " long " +
				");";
		
		Initalize();
	}
	
	public Cursor getCursor(){
		return getCursor(null);
	}
	
	public Cursor getCursor(String statement){
		Cursor ret;
		try{
			ret = Obtainer.AcquireDB().query(TableName,
					new String[]{IDXKEY, COMMUNITY_ID, LAST_SEND_TIME}, statement, null, null, null, null);
		}finally{
			Obtainer.ReleaseDB();
		}
		return ret;
	}
	
	
	public void insertWithBeginTransaction(Long community_id, Long last_time){
		if(lastTimeMap.containsKey(community_id)){
			if(lastTimeMap.get(community_id).equals(last_time)){
				return;
			}
		}
		String delete = "delete from " + TableName + " where COMMUNITY_ID = " + community_id.toString();
		Obtainer.getDB().execSQL(delete);
		
		String insert = "insert into " + TableName + "(COMMUNITY_ID, LAST_SEND_TIME) values('" + community_id.toString() + "','" + last_time.toString() + "');";
		Obtainer.getDB().execSQL(insert);
		
		lastTimeMap.put(community_id, last_time);
	}
	
	public void Initalize(){
		Cursor cur = null;
		try{
			cur = getCursor();
		}catch(SQLException ex){
			if(ex != null){
				if(ex.getMessage().startsWith("no such table")){
					createTable();
				}
			}
		}finally{
			closeCursor(cur);
		}
	}
	
	public void closeCursor(Cursor cur){
		if(cur != null){
			cur.close();
		}
	}
	
	public void createTable(){
		SQLiteDatabase db = Obtainer.AcquireDB();
		db.beginTransaction();
		boolean createComplete = false;
		try{
			Obtainer.getDB().execSQL(CREATE);
			createComplete = true;
		}catch(SQLException ex){
			String msg = null;
			if(ex != null){
				msg = ex.getMessage();
			}
			
			if(msg != null && msg.contains("already exists")){
				createComplete = true;
			}
			else{
				createComplete = false;
			}
		}
		if(createComplete){
			db.setTransactionSuccessful();
		}
		db.endTransaction();
		Obtainer.ReleaseDB();
	}
}

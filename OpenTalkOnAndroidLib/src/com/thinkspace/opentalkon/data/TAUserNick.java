package com.thinkspace.opentalkon.data;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.thinkspace.opentalkon.OTOApp;

public class TAUserNick {
	static TAUserNick instance;
	public static TAUserNick getInstance(){
		if(instance == null){
			instance = new TAUserNick(OTOApp.getInstance().getDB().getDbEngine());
		}
		return instance;
	}
	public static void forceNewInstance(){
		instance = new TAUserNick(OTOApp.getInstance().getDB().getDbEngine());
	}
	public static final String IDXKEY = "IDXKEY";
	public static final int IDXKEY_IDX = 0;
	
	public static final String USER_ID = "USER_ID";
	public static final int USER_ID_IDX = 1;
	
	public static final String NICK_NAME = "NICK_NAME";
	public static final int NICK_NAME_IDX = 2;
	
	public static final String NICK_NAME_MINE = "NICK_NAME_MINE";
	public static final int NICK_NAME_MINE_IDX = 3;
	
	ITAObtainDB Obtainer;
	String TableName;
	
	String CREATE;
	Map<Long, String> cacheMap = new HashMap<Long, String>();
	
	public TAUserNick(ITAObtainDB Obtainer){
		this.TableName = TAUserNick.class.getSimpleName();
		this.Obtainer = Obtainer;
		
		CREATE = "create table " + TableName +
				"(" +
					IDXKEY + " integer primary key autoincrement, " +
					USER_ID + " long, " +
					NICK_NAME + " varchar(255), " +
					NICK_NAME_MINE + " varchar(255) " +
				");";
		
		Initalize();
	}
	
	public String getUserInfo(long user_id){
		if(cacheMap.containsKey(user_id)){
			return cacheMap.get(user_id);
		}
		
		String nick = null;
		String nickMine = null;
		
		Cursor cursor = getCursor(USER_ID  + " = " + user_id);
		try{
			if(cursor.moveToFirst()){
				nick = cursor.getString(NICK_NAME_IDX);
				nickMine = cursor.getString(NICK_NAME_MINE_IDX);
			}
		}finally{
			closeCursor(cursor);
		}
		
		if(nickMine != null){
			cacheMap.put(user_id, nickMine);
		}else{
			cacheMap.put(user_id, nick);
		}
		
		return nickMine != null? nickMine : nick;
	}
	
	public void insertWithBeginTransaction(long user_id, String nickName, String nickNameMine){
		
		String cachedNick = null;
		String cachedNickMine = null;
		
		Cursor cursor = Obtainer.getDB().query(TableName,
			new String[]{IDXKEY, USER_ID, NICK_NAME, NICK_NAME_MINE}, USER_ID  + " = " + user_id, null, null, null, null);
		
		try{
			if(cursor.moveToFirst()){
				cachedNick = cursor.getString(NICK_NAME_IDX);
				cachedNickMine = cursor.getString(NICK_NAME_MINE_IDX);
			}
		}finally{
			closeCursor(cursor);
		}
		
		if(cachedNick != null || cachedNickMine != null){
			ContentValues newVal = new ContentValues();
			if(nickName != null){
				newVal.put(NICK_NAME, nickName);
				cachedNick = nickName;
			}
			if(nickNameMine != null){
				newVal.put(NICK_NAME_MINE, nickNameMine);
				cachedNickMine = nickNameMine;
			}
			Obtainer.getDB().update(TableName, newVal, "user_id = " + user_id, null);
		}else{
			ContentValues newVal = new ContentValues();
			newVal.put(USER_ID, user_id);
			if(nickName != null){
				newVal.put(NICK_NAME, nickName);
				cachedNick = nickName;
			}
			if(nickNameMine != null){
				newVal.put(NICK_NAME_MINE, nickNameMine);
				cachedNickMine = nickNameMine;
			}
			Obtainer.getDB().insert(TableName, null, newVal);
		}
		cursor.close();
		
		if(cachedNickMine != null){
			cacheMap.put(user_id, cachedNickMine);
		}else{
			cacheMap.put(user_id, cachedNick);
		}
	}
	
	Cursor getCursor(){
		return getCursor(null);
	}
	
	Cursor getCursor(String statement){
		Cursor ret;
		try{
			ret = Obtainer.AcquireDB().query(TableName,
					new String[]{IDXKEY, USER_ID, NICK_NAME, NICK_NAME_MINE}, statement, null, null, null, null);
		}finally{
			Obtainer.ReleaseDB();
		}
		return ret;
	}
	
	void Initalize(){
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
	
	void closeCursor(Cursor cur){
		if(cur != null){
			cur.close();
		}
	}
	
	void createTable(){
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

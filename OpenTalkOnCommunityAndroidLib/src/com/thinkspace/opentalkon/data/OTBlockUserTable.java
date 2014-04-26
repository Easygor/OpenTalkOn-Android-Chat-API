package com.thinkspace.opentalkon.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

import com.thinkspace.opentalkon.OTOApp;

public class OTBlockUserTable {
	static OTBlockUserTable instance;
	public static OTBlockUserTable getInstance(){
		if(instance == null){
			instance = new OTBlockUserTable(OTOApp.getInstance().getDB().getDbEngine());
		}
		return instance;
	}
	public static final String IDXKEY = "IDXKEY";
	public static final int IDXKEY_IDX = 0;
	
	public static final String USER_ID = "USER_ID";
	public static final int USER_ID_IDX = 1;
	
	public static final String NICK_NAME = "NICK_NAME";
	public static final int NICK_NAME_IDX = 2;
	
	ITAObtainDB Obtainer;
	String TableName;
	
	String CREATE;
	Map<Long, String> nickNameMap = new HashMap<Long, String>();
	
	public OTBlockUserTable(ITAObtainDB Obtainer){
		this.TableName = OTBlockUserTable.class.getSimpleName();
		this.Obtainer = Obtainer;
		
		CREATE = "create table " + TableName +
				"(" +
					IDXKEY + " integer primary key autoincrement, " +
					USER_ID + " long, " +
					NICK_NAME + " varchar(255) " +
				");";
		
		Initalize();
	}
	
	public String getBlockUserName(Long user_id){
		if(nickNameMap.containsKey(user_id)){
			return nickNameMap.get(user_id);
		}
		
		String nickName = getUserId(user_id);
		if(nickName != null && nickName.length() != 0){
			nickNameMap.put(user_id, nickName);
		}
		return nickName;
	}
	
	public ArrayList<Pair<Integer, String>> getAllBlockUsers(){
		ArrayList<Pair<Integer, String>> ret = new ArrayList<Pair<Integer,String>>();
		Cursor cursor = getCursor(null);
		try{
			if(cursor.moveToFirst()){
				int id = cursor.getInt(USER_ID_IDX);
				String nick = cursor.getString(NICK_NAME_IDX);
				ret.add(Pair.create(id, nick));
			}
		}finally{
			closeCursor(cursor);
		}
		return ret;
	}
	
	public Cursor getCursor(){
		return getCursor(null);
	}
	
	public Cursor getCursor(String statement){
		Cursor ret;
		try{
			ret = Obtainer.AcquireDB().query(TableName,
					new String[]{IDXKEY, USER_ID, NICK_NAME}, statement, null, null, null, null);
		}finally{
			Obtainer.ReleaseDB();
		}
		return ret;
	}
	
	String getUserId(Long user_id){
		if(user_id == null)return "";
		Cursor cursor = getCursor(USER_ID  + " = " + user_id.toString());
		String nick = "";
		try{
			if(cursor.moveToFirst()){
				nick = cursor.getString(NICK_NAME_IDX);
			}
		}finally{
			closeCursor(cursor);
		}
		return nick;
	}
	
	
	public void insertWithBeginTransaction(Long user_id, String nickName){
		if(nickNameMap.containsKey(user_id)){
			if(nickNameMap.get(user_id).equals(nickName))
				return;
		}
		String delete = "delete from " + TableName + " where user_id = " + user_id.toString();
		Obtainer.getDB().execSQL(delete);
		
		String insert = "insert into " + TableName + "(USER_ID, NICK_NAME) values('" + user_id.toString() + "','" + nickName + "');";
		Obtainer.getDB().execSQL(insert);
		
		nickNameMap.put(user_id, nickName);
	}
	
	public void deleteWithBeginTransaction(Long user_id){
		String delete = "delete from " + TableName + " where user_id = " + user_id.toString();
		Obtainer.getDB().execSQL(delete);
		
		if(nickNameMap.containsKey(user_id)){
			nickNameMap.remove(user_id);
		}
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

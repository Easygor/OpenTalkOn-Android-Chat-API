package com.thinkspace.opentalkon.data;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;

public class TABigTableTable {
	public static final String IDXKEY = "IDXKEY";
	public static final int IDXKEY_IDX = 0;
	public static final String DATA = "DATA";
	public static final int DATA_IDX = 1;
	
	ITAObtainDB Obtainer;
	String TableName;
	String CREATE;
	String DELETE;
	String DELETE_ALL;
	String DROP_TABLE;
	
	public TABigTableTable(String TableName, ITAObtainDB Obtainer){
		this.TableName = TableName;
		this.Obtainer = Obtainer;
		
		CREATE = "create table " + TableName +
		"(" + IDXKEY + " integer primary key autoincrement, " + DATA + " blob);";

		DELETE = "delete from " + TableName + " Where " + IDXKEY + " = " + "???";
		
		DELETE_ALL = "delete from " + TableName;
		
		DROP_TABLE = "drop table " + TableName; 
			
		Initalize();
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
	
	public Cursor getCursor(){
		return getCursor(null);
	}
	
	public Cursor getCursor(String statement){
		Cursor ret;
		try{
			ret = Obtainer.AcquireDB().query(TableName,
					new String[]{IDXKEY, DATA}, statement, null, null, null, null);
		}finally{
			Obtainer.ReleaseDB();
		}
		return ret;
	}
	public void closeCursor(Cursor cur){
		if(cur != null){
			cur.close();
		}
	}
	
	public void dropTable(){
		Obtainer.AcquireDB().execSQL(DROP_TABLE);
		Obtainer.ReleaseDB();		
	}
	
	public void deleteAll(){
		Obtainer.AcquireDB().execSQL(DELETE_ALL);
		Obtainer.ReleaseDB();
	}
	
	public void deleteAllWithBeginTransaction(){
		Obtainer.getDB().execSQL(DELETE_ALL);
	}
	
	public byte[] getDataAfterMarshall(TABigTableBase data){
		Parcel parcel = Parcel.obtain();
		parcel.writeValue(data);
		return parcel.marshall();
	}
	public void pushDataWithBeginTransaction(TABigTableBase data){
		ContentValues cv = new ContentValues();
		cv.put(DATA, getDataAfterMarshall(data)); 
		long idxkey = Obtainer.getDB().insert(TableName, null, cv);
		data.setTableIdx((int)idxkey);
	}
	
	public void pushData(TABigTableBase data){
		ContentValues cv = new ContentValues();
		cv.put(DATA, getDataAfterMarshall(data)); 
		SQLiteDatabase db = Obtainer.AcquireDB();
		db.beginTransaction();
		long idxkey = db.insert(TableName, null, cv);
		data.setTableIdx((int)idxkey);
		db.setTransactionSuccessful();
		db.endTransaction();
		Obtainer.ReleaseDB();
	}
	
	public ArrayList<TABigTableBase> getRevArray(ArrayList<TABigTableBase> arr){
		ArrayList<TABigTableBase> ret = new ArrayList<TABigTableBase>();
		for(int i=arr.size()-1;i>=0;--i){
			ret.add(arr.get(i));
		}
		return ret;
	}
	
	public int getMaximumIdxKey(){
		if(getTableCnt() == 0) { return -1; }
		Cursor cur = null;
		try{
			cur = Obtainer.AcquireDB().rawQuery("select max("+ IDXKEY +") from " + TableName, null);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(Obtainer != null){
				Obtainer.ReleaseDB();
			}
		}
		
		if(cur != null){
			if(cur.moveToFirst()){
				return cur.getInt(0);
			}
		}
		return -1;
	}
	
	public int getTableCnt(){
		Cursor cur = null;
		try{
			cur = Obtainer.AcquireDB().rawQuery("select count(*) from " + TableName, null);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(Obtainer != null){
				Obtainer.ReleaseDB();
			}
		}
		
		if(cur != null){
			if(cur.moveToFirst()){
				return cur.getInt(0);
			}
		}
		return 0;		
	}
	
	public TABigTableBase dispatchBaseData(Cursor cur){
		byte[] data = null;
		data = cur.getBlob(DATA_IDX);
		
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(data, 0, data.length);
		parcel.setDataPosition(0);
		TABigTableBase resData =(TABigTableBase)parcel.readValue(TABigTableBase.class.getClassLoader());
		resData.setTableIdx(cur.getInt(IDXKEY_IDX));
		
		return resData;
	}
	
	public TABigTableBase getData(int idx){
		Cursor cur = getCursor(IDXKEY + "=" + String.valueOf(idx));
		if(cur != null){
			if(cur.moveToFirst()){
				return dispatchBaseData(cur);
			}
		}
		
		return null;
	}
	
	public ArrayList<TABigTableBase> getData(){
		ArrayList<TABigTableBase> ret = new ArrayList<TABigTableBase>();
		Cursor cur = getCursor();
		if(cur != null){
			if(cur.moveToFirst()){
				do{
					TABigTableBase resData = dispatchBaseData(cur);
					ret.add(resData);
				}while(cur.moveToNext());
			}
		}
		closeCursor(cur);
		return getRevArray(ret);
	}
	public void deleteData(int idx){
		String myDelete = DELETE.replace("???", String.valueOf(idx));
		Obtainer.AcquireDB().execSQL(myDelete);
		Obtainer.ReleaseDB();
	}
	public void deleteData(TABigTableBase data){
		deleteData(data.getTableIdx());
	}
	
	public void deleteDataWithBeginTransaction(TABigTableBase data){
		String myDelete = DELETE.replace("???", String.valueOf(data.getTableIdx()));
		Obtainer.getDB().execSQL(myDelete);
	}
	
	public void updateData(TABigTableBase data){
		ContentValues cv = new ContentValues();
		cv.put(IDXKEY, data.getTableIdx());
		cv.put(DATA, getDataAfterMarshall(data));
		Obtainer.AcquireDB().update(TableName, cv, IDXKEY + " = " + String.valueOf(data.getTableIdx()), null);
		Obtainer.ReleaseDB();
	}
	public void updateDataWithBeginTransaction(TABigTableBase data){
		ContentValues cv = new ContentValues();
		cv.put(IDXKEY, data.getTableIdx());
		cv.put(DATA, getDataAfterMarshall(data));
		Obtainer.getDB().update(TableName, cv, IDXKEY + " = " + String.valueOf(data.getTableIdx()), null);		
	}
	
	public final static class convertor<E>{
		@SuppressWarnings("unchecked")
		public ArrayList<E> getData(ArrayList<TABigTableBase> nowItem){
			ArrayList<E> ret = new ArrayList<E>();
			for(TABigTableBase now : nowItem){
				ret.add((E)now);
			}
			return ret;
		}
	}
}

package com.thinkspace.opentalkon.data;

import java.util.concurrent.Semaphore;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TABigTableDBBase implements ITAObtainDB {
	final int RETRY_COUNT = 3;
	Context context;
	String dbFileName;
	SQLiteDatabase SqlDB;
	Semaphore SyncObj;
	
	public TABigTableDBBase(Context context, String dbFileName){
		this.context = context;
		this.dbFileName = dbFileName;
		Initialize();
	}
	
	public void Initialize(){
		for(int i=0;i<RETRY_COUNT;++i){
			try{
				SqlDB = context.openOrCreateDatabase(dbFileName, Context.MODE_PRIVATE, null);
				break;
			}catch(Exception ex){
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {}
			}
		}
		SyncObj = new Semaphore(1);
	}
	
	@Override
	public SQLiteDatabase AcquireDB() {
		try {
			SyncObj.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SqlDB.acquireReference();
		return SqlDB;
	}

	@Override
	public SQLiteDatabase getDB() {
		return SqlDB;
	}

	@Override
	public void ReleaseDB() {
		SqlDB.releaseReference();
		SyncObj.release();
	}

	@Override
	public boolean beginTransaction() {
		try {
			SyncObj.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SqlDB.acquireReference();
		for(int i=0;i<RETRY_COUNT;++i){
			try{
				SqlDB.beginTransaction();
				return true;
			}catch(Exception ex){
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {}
			}
		}
		SyncObj.release();
		return false;
	}

	@Override
	public void endTransaction() {
		SqlDB.setTransactionSuccessful();
		for(int i=0;i<RETRY_COUNT;++i){
			try{
				SqlDB.endTransaction();
				break;
			}catch(Exception ex){
				try {
					Thread.sleep(10L);
				} catch (InterruptedException e) {}
			}
		}
		SqlDB.releaseReference();
		SyncObj.release();
	}

	@Override
	public void createTableDiscardException(String tableName, String createTableString) {
		try{
			SqlDB.execSQL(createTableString);
		}catch(SQLException ex){
			ex.printStackTrace();
		}
	}
}

package com.thinkspace.opentalkon.data;

import android.database.sqlite.SQLiteDatabase;

public interface ITAObtainDB {
	public SQLiteDatabase AcquireDB();
	public void ReleaseDB();
	
	public SQLiteDatabase getDB();
	
	public boolean beginTransaction();	
	public void endTransaction();
	public void createTableDiscardException(String tableName, String createTableString);
}

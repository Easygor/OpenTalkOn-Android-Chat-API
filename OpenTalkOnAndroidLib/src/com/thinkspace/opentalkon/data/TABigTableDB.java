package com.thinkspace.opentalkon.data;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;

public class TABigTableDB{
	Map<String, Boolean> existTables;
	Map<String, TABigTableTable> Tables;
	Map<String, String> upGradeTable;
	ITAObtainDB dbEngine;
	
	Context context;
	String dbName;
	
	public TABigTableDB(Context context, String dbName){
		this.context = context;
		this.dbName = dbName;
		init();
	}
	
	public void init(){
		upGradeTable = new HashMap<String, String>();
		dbEngine = new TABigTableDBBase(context, dbName);
		Tables = new HashMap<String, TABigTableTable>();
		checkExistTables(context, dbEngine);
	}
	
	public ITAObtainDB getDbEngine() {
		return dbEngine;
	}

	public void checkExistTables(Context context, ITAObtainDB dbEngine){
		existTables = new HashMap<String, Boolean>();
		
		Cursor cur = null;
		try{
			cur = dbEngine.getDB().rawQuery("select name from sqlite_master where type = 'table'", null);
			try{
				if(cur.moveToFirst()){
					do{
						String tableName = cur.getString(0);
						existTables.put(tableName, true);
					}while(cur.moveToNext());
				}
			}finally{
				if(cur != null){
					cur.close();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public void deleteDB(){
		//TABigTableTable table = TAApp.getInstance().getDB().getTable(TAPostItem.class.getSimpleName());
		//table.deleteAll();
	}
	
	public TABigTableTable getTable(String TableName){
		if(Tables.containsKey(TableName) == false){
			TABigTableTable nowTable = new TABigTableTable(TableName, dbEngine);
			Tables.put(TableName, nowTable);
		}
		
		return Tables.get(TableName);
	}
	
	public boolean beginTransaction(){
		return dbEngine.beginTransaction();
	}
	
	public void endTransaction(){
		dbEngine.endTransaction();
	}
	
	public String getCreateTableString(String table){
		return "create table " + table +
		"(" + TABigTableTable.IDXKEY + " integer primary key autoincrement, " + TABigTableTable.DATA + " blob);";
	}
	
	public void InitalizeTables(){
		dbEngine.beginTransaction();
		dbEngine.createTableDiscardException(OTTalkMsgV2.class.getSimpleName(), getCreateTableString(OTTalkMsgV2.class.getSimpleName()));
		dbEngine.endTransaction();
	}
}

package com.thinkspace.opentalkon.ui;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTAppInfo;
import com.thinkspace.opentalkon.satelite.TASatelite;

public class OTMoreApp extends OTTableActivityBase{
	AppCategory appCategory;
	
	@Override
	protected int getColCount() {
		return 4;
	}
	
	class TableAppInfo implements TableDataInterface{
		public OTAppInfo appInfo;
		
		@Override
		public String getTitle() {
			return appInfo.app_name;
		}

		@Override
		public String getDescription() {
			return appInfo.description;
		}

		@Override
		public String getImagePath() {
			return appInfo.img_path;
		}

		@Override
		public void onButtonClick(int state) {
			switch(state){
			case 0:
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appInfo.package_name));
				marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				startActivity(marketIntent);
				break;
			case 1:
				/*
				Intent intent = new Intent(OTMoreApp.this, OTMoreTabOpentalk.class);
				intent.putExtra("app_info", appInfo);
				startActivity(intent);
				*/
				break;
			}
		}

		@Override
		public String getButtonString(int state) {
			switch(state){
			case 0:
				return getString(R.string.oto_go_market);
			case 1:
				return getString(R.string.oto_go_opentalk);
			}
			return "";
		}
	}
	
	class AppCategory implements CategoryInterface{
		public String [] categoryNames;
		@Override
		public void onCategoryClick(int state) {
			satelite.doGetMoreAppList(OTOApp.getInstance().getToken(), state);
			OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTMoreApp.this);
		}
		
		@Override
		public String getCategoryTitle() {
			return getString(R.string.oto_select_category);
		}
		
		@Override
		public String[] getCategoryNames() {
			return categoryNames;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		onRequestData();
	}

	@Override
	protected void onRequestData() {
		satelite.doGetMoreAppList(OTOApp.getInstance().getToken(), nowCategory);
		OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
	}

	@Override
	protected void onProcessData(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		try{
			String state = data.getString("state");
			String location = data.getString("location");
			if(location.equals(TASatelite.getName(TASatelite.GET_MORE_APP_LIST))){
				if(state.equals("ok")){
					JSONObject rawData = data.getJSONObject("data");
					int nowCategory = rawData.getInt("now_category");
					JSONArray categorys = rawData.getJSONArray("categorys");
					JSONArray list = rawData.getJSONArray("appList");
					
					dataList.clear();
					for(int i=0;i<list.length();++i){
						JSONObject appData = list.getJSONObject(i);
						OTAppInfo app = new OTAppInfo();
						app.id = appData.getLong("id");
						app.app_owner_id = appData.getLong("app_owner_id");
						app.app_name = appData.getString("app_name");
						app.package_name = appData.getString("android_package_name");
						app.img_path = appData.getString("img_path");
						app.description = appData.getString("description");
						
						TableAppInfo tableApp = new TableAppInfo();
						tableApp.appInfo = app;
						dataList.add(tableApp);
					}
					makeLayout(mainView, dataList);
					
					final String [] categoryNames = new String[categorys.length()];
					for(int i=0;i<categorys.length();++i){
						categoryNames[i] = categorys.getString(i);
					}
					if(appCategory == null){
						appCategory = new AppCategory();
						appCategory.categoryNames = categoryNames;
					}
					setCategory(appCategory, nowCategory);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}

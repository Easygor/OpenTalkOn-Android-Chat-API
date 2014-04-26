package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.CommunityData;
import com.thinkspace.opentalkon.data.CommunityLastTimeTable;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;

public class OTComGroupActivity extends OTTableActivityBase {
	long community_group_id;
	long app_code;
	
	@Override
	protected int getColCount() {
		return 4;
	}
	
	class TableComInfo implements TableDataInterface{
		CommunityData comData;
		
		@Override
		public String getTitle() {
			return comData.title;
		}

		@Override
		public String getDescription() {
			return comData.description;
		}

		@Override
		public String getImagePath() {
			return comData.img_url2;
		}

		@Override
		public void onButtonClick(int state) {
			switch(state){
			case 0:
				if(comData.public_opentalk){
					Intent intent = new Intent(OTComGroupActivity.this, OTPublicTalkList.class);
					intent.putExtra("community_data", comData);
					intent.putExtra("authority", OTOApp.getInstance().getAppCode() == app_code);
					startActivity(intent);
				}else{
					Intent intent = null;
					if(comData.need_picture){
						intent = new Intent(OTComGroupActivity.this, OTOpenTalkImageRoom.class);
					}else{
						intent = new Intent(OTComGroupActivity.this, OTOpenTalkRoom.class);
					}
					intent.putExtra("community_data", comData);
					intent.putExtra("authority", OTOApp.getInstance().getAppCode() == app_code);
					startActivity(intent);
				}
				break;
			}
		}

		@Override
		public String getButtonString(int state) {
			switch(state){
			case 0:
				if(comData.public_opentalk){
					return getString(R.string.oto_go_chatroom);
				}else{
					return getString(R.string.oto_go_community);
				}
			case 1:
				return null;
			}
			return null;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if(intent == null || intent.hasExtra("community_group_id") == false || intent.hasExtra("app_code") == false){
			finish();
			return;
		}
		community_group_id = intent.getLongExtra("community_group_id", -1L);
		app_code = intent.getLongExtra("app_code", -1L);
		if(app_code == -1L){
			app_code = OTOApp.getInstance().getAppCode();
		}
		onRequestData();
	}

	@Override
	protected void onRequestData() {
		satelite.doGetSubCommunityList(OTOApp.getInstance().getToken(), app_code, community_group_id);
		OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(dataList.size() > 0){
			refreshNew();
		}
	}
	
	public void refreshNew(){
		for(int i=0;i<dataList.size();++i){
			TableComInfo com = (TableComInfo) dataList.get(i);
			long lastSendTime = CommunityLastTimeTable.getInstance().getLastSendTime(com.comData.id);
			if(lastSendTime == 0L || lastSendTime < com.comData.last_time){
				newLayout.get(i).setVisibility(View.VISIBLE);
			}else{
				newLayout.get(i).setVisibility(View.INVISIBLE);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onProcessData(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
		if(dData.isOK()){
			ArrayList<CommunityData> comList = (ArrayList<CommunityData>) dData.getData();
			dataList.clear();
			for(CommunityData com : comList){
				TableComInfo tableCom = new TableComInfo();
				tableCom.comData = com;
				dataList.add(tableCom);
			}
			makeLayout(mainView, dataList);
			refreshNew();
		}
	}
	
}

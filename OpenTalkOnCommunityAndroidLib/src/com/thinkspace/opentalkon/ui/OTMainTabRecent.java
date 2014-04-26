package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.CommunityData;
import com.thinkspace.opentalkon.data.OTComMsg;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAPublicRoomInfo;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroupView;

public class OTMainTabRecent extends PLActivityGroupView implements TADataHandler {
	TASatelite satelite;
	ListView listView;
	TextView emptyView;
	View progressView;
	ListAdapter listAdapter;
	boolean finishGuard = false;
	List<RecentDataInterface> recentMsgs = new ArrayList<RecentDataInterface>();
	
	public static enum RecentDataType{
		CHAT_COMMUNITY,
		POST_COMMUNITY,
		PICTURE_COMMUNITY,
		CHAT_ROOM
	}
	
	interface RecentDataInterface{
		public RecentDataType getType();
		public String getName();
		public String getUserName();
		public int getUserLevel();
		public String getMainImagePath();
		public long getTime();
		public boolean showLocker();
		public String showSubImage();
		public long getPostId();
		public long getPublicChatRoomId();
		public String getDate();
	}
	
	class CommunityRecent implements RecentDataInterface{
		public OTComMsg comMsg;
		public CommunityData comData;
		
		@Override
		public int getUserLevel() {
			return comMsg.getSenderInfo().level;
		}

		@Override
		public String getUserName() {
			return comMsg.getSenderInfo().getNickName();
		}

		@Override
		public String getDate() {
			return PLEtcUtilMgr.getDateFormat(comMsg.getTime());
		}

		@Override
		public RecentDataType getType() {
			if(comData.need_picture){
				return RecentDataType.PICTURE_COMMUNITY;
			}
			if(comData.write_method_chat){
				return RecentDataType.CHAT_COMMUNITY;
			}else{
				return RecentDataType.POST_COMMUNITY;
			}
		}

		@Override
		public String getName() {
			if(comMsg.isImgMsg()){
				return "      [" + comData.title + "] " + getString(R.string.oto_picture);
			}else{
				return "      [" + comData.title + "] " + comMsg.getMsg();
			}
		}

		@Override
		public String getMainImagePath() {
			return comMsg.getSenderInfo().getImagePath();
		}

		@Override
		public long getTime() {
			return comMsg.getTime();
		}

		@Override
		public boolean showLocker() {
			return false;
		}

		@Override
		public String showSubImage() {
			try{
				if(comMsg.isImgMsg()){
					return comMsg.getImg_url().getString(0);
				}else{
					return null;
				}
			}catch(Exception ex){
				return null;
			}
		}

		@Override
		public long getPostId() {
			return comMsg.getId();
		}

		@Override
		public long getPublicChatRoomId() {
			return -1;
		}
		
	}
	
	class ChatRoomRecent implements RecentDataInterface{
		public TAPublicRoomInfo roomInfo;
		public CommunityData comData;
		
		@Override
		public int getUserLevel() {
			return roomInfo.getOwnerInfo().level;
		}
		
		@Override
		public String getUserName() {
			return roomInfo.getOwnerInfo().getNickName();
		}
		
		@Override
		public String getDate() {
			return PLEtcUtilMgr.getDateFormat(roomInfo.getLast_msg_time());
		}
		
		@Override
		public RecentDataType getType() {
			return RecentDataType.CHAT_ROOM;
		}

		@Override
		public String getName() {
			return "      [" + comData.title + "] " + roomInfo.getName();
		}

		@Override
		public String getMainImagePath() {
			return roomInfo.getImg_url();
		}

		@Override
		public long getTime() {
			return roomInfo.getLast_msg_time();
		}

		@Override
		public boolean showLocker() {
			return true;
		}

		@Override
		public String showSubImage() {
			return null;
		}

		@Override
		public long getPostId() {
			return -1L;
		}

		@Override
		public long getPublicChatRoomId() {
			return roomInfo.getRoom_id();
		}
		
	}
	
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		try{
			String location = data.getString("location");
			String state = data.getString("state");
			if(TASatelite.GET_COMMUNITY_RECENT_LIST.endsWith(location)){
				if(state.equals("ok")){
					JSONArray realData = data.getJSONArray("data");
					
					recentMsgs = new ArrayList<RecentDataInterface>();
					OTOApp.getInstance().getDB().beginTransaction();
					for(int i=0;i<realData.length();++i){
						JSONObject recentData = realData.getJSONObject(i);
						if(recentData.has("comMsg")){
							CommunityRecent recent = new CommunityRecent();
							recent.comMsg = OTOpenTalkRoom.parseComMsg(recentData.getJSONObject("comMsg"));
							recent.comData = TASateliteDispatcher.dispatchComData(recentData.getJSONObject("comData"), false);
							recentMsgs.add(recent);
						}else{
							ChatRoomRecent recent = new ChatRoomRecent();
							recent.roomInfo = TASateliteDispatcher.dispatchPublicRoomInfo(recentData.getJSONObject("roomInfo"));
							recent.comData = TASateliteDispatcher.dispatchComData(recentData.getJSONObject("comData"), false);
							recentMsgs.add(recent);
						}
					}
					OTOApp.getInstance().getDB().endTransaction();
					
					setState(2);
					listView.setAdapter(listAdapter = new ListAdapter(OTMainTabRecent.this, -1, recentMsgs));
				}
			}else if(TASatelite.ENTER_PUBLIC_CHAT.endsWith(location)){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					long room_id = realData.getLong("room_id");
					
					Intent intent = new Intent(OTMainTabRecent.this, OTPublicChatRoom.class);
					intent.putExtra("room_id", room_id);
					startActivity(intent);
				}else{
					if(state.equals("you has kicked")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_public_opentalk), getString(R.string.oto_already_kicked));
					}else if(state.equals("room is hidden")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_public_opentalk), getString(R.string.oto_room_is_hidden));
					}
				}
			}
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}
	
	class ListHolder{
		public TextView title;
		public ImageView titleImage;
		public TextView description;
		public TextView date;
		public ImageView image;
		public ImageView subImage;
		public ImageView locker;
		public RecentDataInterface recentData;
		public View mainView;
		
		public ListHolder(LayoutInflater li){
			mainView = li.inflate(R.layout.ot_recent_list_elem, null);
			mainView.setTag(this);
			
			title = (TextView) mainView.findViewById(R.id.oto_recent_elem_title);
			titleImage = (ImageView)mainView.findViewById(R.id.oto_recent_elem_title_image);
			description = (TextView) mainView.findViewById(R.id.oto_recent_elem_description);
			date = (TextView) mainView.findViewById(R.id.oto_recent_elem_date);
			image = (ImageView) mainView.findViewById(R.id.oto_recent_elem_img);
			subImage = (ImageView) mainView.findViewById(R.id.oto_recent_elem_sub_image);
			locker = (ImageView) mainView.findViewById(R.id.oto_recent_elem_locker);
			
			mainView.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					Intent intent = null;
					switch(recentData.getType()){
					case CHAT_ROOM:
						OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTMainTabRecent.this);
						satelite.doEnterPublicChat(OTOApp.getInstance().getToken(), recentData.getPublicChatRoomId());
						break;
					case CHAT_COMMUNITY:
						intent = new Intent(OTMainTabRecent.this, OTOpenTalkDetail.class);
						intent.putExtra("post_id", recentData.getPostId());
						intent.putExtra("authority", true);
						startActivity(intent);
						break;
					case PICTURE_COMMUNITY:
						intent = new Intent(OTMainTabRecent.this, OTOpenTalkDetail.class);
						intent.putExtra("post_id", recentData.getPostId());
						intent.putExtra("authority", true);
						startActivity(intent);
						break;
					case POST_COMMUNITY:
						intent = new Intent(OTMainTabRecent.this, OTOpenTalkDetail.class);
						intent.putExtra("post_id", recentData.getPostId());
						intent.putExtra("authority", true);
						startActivity(intent);
						break;
					}
				}
			});
		}
	}
	
	class ListAdapter extends ArrayAdapter<RecentDataInterface>{
		List<RecentDataInterface> recentMsgs;
		public ListAdapter(Context context, int textViewResourceId,List<RecentDataInterface> objects) {
			super(context, textViewResourceId, objects);
			this.recentMsgs = objects;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListHolder holder = null;
			if(convertView == null){
				holder = new ListHolder(getLayoutInflater());
			}else{
				holder = (ListHolder) convertView.getTag();
			}
			
			RecentDataInterface data = recentMsgs.get(position);
			holder.recentData = data;
			
			holder.date.setText(data.getDate());
			holder.description.setText(data.getName());
			
			if(data.getType() == RecentDataType.CHAT_ROOM){
				if(data.getMainImagePath() == null || data.getMainImagePath().length() == 0){
					holder.image.setImageResource(R.drawable.oto_oto_logo);
				}else{
					String realUrl = TASatelite.makeImageUrl(data.getMainImagePath());
					loadImageOnList(realUrl, holder.image, R.drawable.oto_oto_logo, this, true, false);
				}
			}else{
				if(data.getMainImagePath() == null || data.getMainImagePath().length() == 0){
					holder.image.setImageResource(R.drawable.oto_friend_img_01);
				}else{
					String realUrl = TASatelite.makeImageUrl(data.getMainImagePath());
					loadImageOnList(realUrl, holder.image, R.drawable.oto_friend_img_01, this, true, false);
				}
			}
			
			if(data.showLocker()){
				holder.locker.setVisibility(View.VISIBLE);
			}else{
				holder.locker.setVisibility(View.GONE);
			}
			
			if(data.showSubImage() == null || data.showSubImage().length() == 0){
				holder.subImage.setVisibility(View.GONE);
			}else{
				holder.subImage.setVisibility(View.VISIBLE);
				String realUrl = TASatelite.makeImageUrl(data.showSubImage());
				loadImageOnList(realUrl, holder.subImage, R.drawable.oto_oto_logo, this, true, false);
			}
			
			holder.title.setText(data.getUserName());
			holder.titleImage.setVisibility(View.VISIBLE);
			switch(data.getType()){
			case CHAT_COMMUNITY:
				holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_chat);
				break;
			case POST_COMMUNITY:
				holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_post);
				break;
			case CHAT_ROOM:
				holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_chatrooms);
				break;
			case PICTURE_COMMUNITY:
				holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_images);
				break;
			}
			
			return holder.mainView;
		}
	}
	
	@Override
	public void onTokenIsNotValid(JSONObject data) {
		if(finishGuard) return;
		finish();
		finishGuard = true;
	}
	@Override
	public void onLimitMaxUser(JSONObject data) {
		if(finishGuard) return;
		startActivity(new Intent(this, OTLimit.class));
		finish();
		finishGuard = true;
	}
	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		setState(1);
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		setState(1);
	}
	
	public void setState(int state){
		switch(state){
		case 0:
			listView.setVisibility(View.GONE);
			emptyView.setVisibility(View.GONE);
			progressView.setVisibility(View.VISIBLE);
			break;
		case 1:
			listView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
			progressView.setVisibility(View.GONE);
			break;
		case 2:
			listView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
			progressView.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_sub_tab_recent);
		
		listView = (ListView)findViewById(R.id.oto_community_list);
		emptyView = (TextView)findViewById(R.id.oto_community_empty);
		progressView = findViewById(R.id.oto_community_progress);
		
		setState(0);
		satelite = new TASatelite(this);
		satelite.doGetCommunityRecentList(OTOApp.getInstance().getToken());
	}
}

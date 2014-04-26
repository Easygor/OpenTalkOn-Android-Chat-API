package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.CommunityData;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAPublicRoomInfo;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.ui.helper.ImageCacheActivity;

public class OTPublicTalkList extends ImageCacheActivity implements TADataHandler {
	CommunityData data;
	ListAdapter listAdapter;
	ListView listView;
	TextView emptyView;
	View progressView;
	View makeChatView;
	
	EditText searchEdit;
	View searchEditDelete;
	TASatelite satelite;
	
	boolean endList;
	boolean networking;
	
	ArrayList<TAPublicRoomInfo> rooms = new ArrayList<TAPublicRoomInfo>();
	Map<Long, Boolean> dupChecker = new HashMap<Long, Boolean>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_publictalk_list);
		
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		
		if(intent.hasExtra("community_data") == false){
			finish();
			return;
		}
		data = intent.getParcelableExtra("community_data");
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		TextView title = (TextView) findViewById(R.id.oto_base_tab_title);
		title.setText(data.title);
		
		setSearchLayout();
		listView = (ListView)findViewById(R.id.oto_main_conv_list);
		emptyView = (TextView)findViewById(R.id.oto_main_conv_empty);
		progressView = findViewById(R.id.oto_publictalk_progress);
		makeChatView = findViewById(R.id.oto_subtab_new_conv_button);
		makeChatView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(OTPublicTalkList.this, OTMakePublicChat.class);
				intent.putExtra("community_id", data.id);
				startActivity(intent);
			}
		});
		
		listView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(listView.getChildCount() > 0 && listView.getLastVisiblePosition() == listView.getAdapter().getCount() -1 &&
						listView.getChildAt(listView.getChildCount() - 1).getBottom() <= listView.getHeight()){
					loadNextRoom();
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {}
		});
		
		satelite = new TASatelite(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		initAndGetRooms();
	}

	public void initAndGetRooms(){
		setState(2);
		endList = false;
		networking = false;
		rooms.clear();
		loadNextRoom();
	}
	
	public void loadNextRoom(){
		if(searchEdit.getText().toString().length() == 0){
			if(networking == false && endList == false){
				networking = true;
				satelite.doGetPublicChats(OTOApp.getInstance().getToken(), rooms.size(), data.id);
			}
		}
	}
	
	public void onSearch(String value){
		setState(2);
		if(value.length() == 0){
			initAndGetRooms();
		}else{
			endList = false;
			satelite.doSearchPublicRoom(OTOApp.getInstance().getToken(), value, data.id);
		}
	}
	
	public void setState(int state){
		switch(state){
		case 0:
			listView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
			progressView.setVisibility(View.GONE);
			break;
		case 1:
			listView.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
			progressView.setVisibility(View.GONE);
			break;
		case 2:
			listView.setVisibility(View.GONE);
			emptyView.setVisibility(View.GONE);
			progressView.setVisibility(View.VISIBLE);
			break;
		}
	}
	
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		try{
			String location = data.getString("location");
			String state = data.getString("state");
			if(TASatelite.ENTER_PUBLIC_CHAT.endsWith(location)){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					long room_id = realData.getLong("room_id");
					
					Intent intent = new Intent(OTPublicTalkList.this, OTPublicChatRoom.class);
					intent.putExtra("room_id", room_id);
					startActivity(intent);
				}else{
					if(state.equals("you has kicked")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_public_opentalk), getString(R.string.oto_already_kicked));
					}else if(state.equals("room is hidden")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_public_opentalk), getString(R.string.oto_room_is_hidden));
					}
				}
			}else if(TASatelite.GET_PUBLIC_CHATS.endsWith(location) || TASatelite.SEARCH_PUBLIC_ROOM.endsWith(location)){
				networking = false;
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					JSONArray result = realData.getJSONArray("result");
					int preCount = 0;
					if(TASatelite.GET_PUBLIC_CHATS.endsWith(location)){
						preCount = realData.getInt("preCount");
						if(result.length() == 0) endList = true;
					}else{
						String keyword = realData.getString("keyword");
						if(keyword.equals(searchEdit.getText().toString()) == false){
							setState(1);
						}
					}
					
					if(preCount == 0){
						dupChecker.clear();
						rooms.clear();
					}
					
					OTOApp.getInstance().getDB().beginTransaction();
					for(int i=0;i<result.length();++i){
						JSONObject rData = result.getJSONObject(i);
						TAPublicRoomInfo room = TASateliteDispatcher.dispatchPublicRoomInfo(rData);
						if(dupChecker.containsKey(room.getRoom_id())) continue;
						dupChecker.put(room.getRoom_id(), true);
						rooms.add(room);
					}
					OTOApp.getInstance().getDB().endTransaction();
					
					if(rooms.size() == 0){
						setState(1);
					}else{
						setState(0);
						if(listAdapter == null){
							listView.setAdapter(listAdapter = new ListAdapter(this, R.layout.ot_conv_list_elem, rooms));
						}else{
							listAdapter.notifyDataSetChanged();
						}
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void onTokenIsNotValid(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onLimitMaxUser(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	class ListViewHolder{
		public View mainView;
		public ViewGroup layoutView;
		public TextView roomName;
		public ImageView roomImage;
		public TextView msgDate;
		public TextView userCount;
		public TextView ownerName;
		public ImageView alarmView;
		ImageView lock;
		TAPublicRoomInfo roomInfo;
		
		View titleLayout1;
		TextView titleLayout2;
		
		public ListViewHolder(View mainView){
			this.mainView = mainView;
			layoutView = (LinearLayout)mainView.findViewById(R.id.oto_conv_elem_layout);
			roomImage = (ImageView)mainView.findViewById(R.id.oto_conv_elem_img);
			ownerName = (TextView)mainView.findViewById(R.id.oto_conv_elem_lasttalk);
			msgDate = (TextView)mainView.findViewById(R.id.oto_conv_elem_date);
			userCount = (TextView)mainView.findViewById(R.id.oto_conv_elem_cnt);
			roomName = (TextView)mainView.findViewById(R.id.oto_conv_elem_talker_name);
			alarmView = (ImageView)mainView.findViewById(R.id.oto_conv_elem_alarm);
			lock = (ImageView)mainView.findViewById(R.id.oto_conv_elem_lock);
			
			titleLayout1 = mainView.findViewById(R.id.oto_conv_elem_title_layout);
			titleLayout2 = (TextView) mainView.findViewById(R.id.oto_conv_elem_title_layout2);
			mainView.setTag(this);
			
			roomName.setSingleLine(false);
			roomName.setMaxLines(2);
			
			layoutView.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTPublicTalkList.this);
					satelite.doEnterPublicChat(OTOApp.getInstance().getToken(), roomInfo.getRoom_id());
				}
			});
		}
	}
	
	class ListAdapter extends ArrayAdapter<TAPublicRoomInfo>{
		ArrayList<TAPublicRoomInfo> items;

		public ListAdapter(Context context, int textViewResourceId, ArrayList<TAPublicRoomInfo> objects) {
			super(context, textViewResourceId, objects);
			items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListViewHolder holder = null;
			if(convertView == null){
				LayoutInflater vi = getLayoutInflater();
				convertView = vi.inflate(R.layout.ot_conv_list_elem, null);
				holder = new ListViewHolder(convertView);
			}else{
				holder = (ListViewHolder)convertView.getTag();
			}
			TAPublicRoomInfo roomInfo = items.get(position); 
			holder.roomInfo = roomInfo;
			
			holder.titleLayout1.setVisibility(View.VISIBLE);
			holder.titleLayout2.setVisibility(View.GONE);
			
			if(roomInfo.getImg_url() == null || roomInfo.getImg_url().length() == 0){
				holder.roomImage.setImageResource(R.drawable.oto_oto_logo);
			}else{
				String realUrl = TASatelite.makeImageUrl(roomInfo.getImg_url());
				loadImageOnList(realUrl, holder.roomImage, R.drawable.oto_oto_logo, this, true, false);
			}
			
			holder.lock.setVisibility(View.VISIBLE);
			if(holder.roomInfo.isHidden()){
				holder.lock.setImageResource(R.drawable.oto_lock);
			}else{
				holder.lock.setImageResource(R.drawable.oto_unlock);
			}
			
			holder.roomName.setText( "("+ String.valueOf(roomInfo.getUsers().size()) + ")"+roomInfo.getName());
			String nickName = TAUserNick.getInstance().getUserInfo(roomInfo.getOwner());
			holder.ownerName.setText(nickName);
			holder.alarmView.setVisibility(View.GONE);
			
			if(roomInfo.getLast_msg_time() == 0L){
				holder.msgDate.setText(getString(R.string.oto_none));
			}else{
				holder.msgDate.setText(PLEtcUtilMgr.getDateFormat(roomInfo.getLast_msg_time()));
			}
			
			holder.userCount.setVisibility(View.GONE);
			
			return holder.mainView;
		}
	}
	
	protected void setSearchLayout(){
		searchEdit = (EditText)findViewById(R.id.oto_subtab_search_edit);
		searchEditDelete = (ImageView)findViewById(R.id.oto_subtab_search_edit_delete);
		
		searchEdit.addTextChangedListener(new TextWatcher() {
			@Override public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override public void beforeTextChanged(CharSequence s, int start, int count,int after) {}

			@Override public void afterTextChanged(Editable s) {
				if (s.length() != 0) {
					searchEditDelete.setVisibility(View.VISIBLE);
				} else {
					searchEditDelete.setVisibility(View.GONE);
				}
				onSearch(searchEdit.getText().toString());
			}
		});
		
		searchEdit.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					onSearch(searchEdit.getText().toString());
					return true;
				}

				return false;
			}
		});
		searchEditDelete.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				searchEdit.getText().clear();
			}
		});
	}
}

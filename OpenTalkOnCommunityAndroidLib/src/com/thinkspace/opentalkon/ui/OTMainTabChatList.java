package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import com.thinkspace.opentalkon.data.OTMsgBase;
import com.thinkspace.opentalkon.data.OTTalkMsgV2;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAPublicRoomInfo;
import com.thinkspace.opentalkon.data.TARoomInfo;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper.OnLoadUserImageUrl;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.ui.OTMainTabChatList.RoomElem.RoomType;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroupView;
import com.thinkspace.pushservice.satelite.PLMsgHandler;
public class OTMainTabChatList extends PLActivityGroupView implements PLMsgHandler, TADataHandler{
	ListView listView;
	ListAdapter listAdapter;
	TextView emptyView;
	View makeChatView;
	TASatelite satelite;
	
	EditText searchEdit;
	View searchEditDelete;
	View subTabMoreButton;
	
	ArrayList<RoomElem> rooms = new ArrayList<RoomElem>();
	ArrayList<RoomElem> rooms_original = new ArrayList<RoomElem>();
	Map<Integer, listElemViewHolder> posBaseViewHolder;
	Handler handler = new Handler();
	
	public static class RoomElem implements Comparable<RoomElem>{
		public enum RoomType{
			PRIVATE_ROOM_LABEL,
			PRIVATE_ROOM,
			PUBLIC_ROOM_LABEL,
			PUBLIC_ROOM
		}
		public RoomType roomType;
		public TARoomInfo roomInfo;
		public String roomLabel;
		
		public int getPoint(){
			return roomType.ordinal();
		}
		
		@Override
		public int compareTo(RoomElem another) {
			if(getPoint() == another.getPoint()){
				if(roomInfo != null && another.roomInfo != null){
					return roomInfo.compareTo(another.roomInfo); 
				}else{
					return 0;
				}
			}else{
				return getPoint() < another.getPoint()?-1:1;
			}
		}
	}
	
	@Override
	public void onTabDestoryed() {
		super.onTabDestoryed();
		clearCacheFromThis();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_sub_tab_talk);
		
		satelite = new TASatelite(this);
		listView = (ListView)findViewById(R.id.oto_main_conv_list);
		emptyView = (TextView)findViewById(R.id.oto_main_conv_empty);
		makeChatView = findViewById(R.id.oto_subtab_new_conv_button);
		UserImageUrlHelper.flushUserIdMap();
		
		makeChatView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				getParent().startActivity(new Intent(getParent(), OTMakeChat.class));
			}
		});
		
		setUseMenu(true);
		setSearchLayout();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().unRegisterMsgHandler(this);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().registerMsgHandler(this);
		}
		
		doRefreshPrivateRoom(null);
		doRefreshPublicRoom(null);
	}

	@Override
	public void onHttpPacketReceived(JSONObject data) {
		try{
			String location = data.getString("location");
			String state = data.getString("state");
			if(TASatelite.GET_MY_PUBLIC_ROOMS.endsWith(location)){
				if(state.equals("ok")){
					JSONArray result = data.getJSONArray("data");
					List<TARoomInfo> rawRooms = new ArrayList<TARoomInfo>();
					for(RoomElem room : rooms_original){
						if(room.roomInfo == null) continue;
						if(room.roomInfo instanceof TAPublicRoomInfo)continue;
						rawRooms.add(room.roomInfo);
					}
					
					OTOApp.getInstance().getDB().beginTransaction();
					for(int i=0;i<result.length();++i){
						JSONObject rData = result.getJSONObject(i);
						TAPublicRoomInfo room = TASateliteDispatcher.dispatchPublicRoomInfo(rData);
						OTOApp.getInstance().getCacheCtrl().addNewRoomInfo(room);
						rawRooms.add(room);
					}
					OTOApp.getInstance().getDB().endTransaction();
					
					//DELETE NOT VALID CACHE
					Map<Long, Boolean> roomValidation = new HashMap<Long, Boolean>();
					for(TARoomInfo room : OTOApp.getInstance().getCacheCtrl().getRoomList()){
						if(room instanceof TAPublicRoomInfo){
							roomValidation.put(room.getRoom_id(), false);
						}
					}
					for(TARoomInfo room : rawRooms){
						if(room instanceof TAPublicRoomInfo){
							if(OTOApp.getInstance().getCacheCtrl().getRoomMap().containsKey(room.getRoom_id())){
								roomValidation.put(room.getRoom_id(), true);
							}
						}
					}
					for(Entry<Long, Boolean> roomEnt : roomValidation.entrySet()){
						if(roomEnt.getValue() == false){
							TARoomInfo roomInfo = OTOApp.getInstance().getCacheCtrl().getRoomMap().get(roomEnt.getKey());
							OTOApp.getInstance().getCacheCtrl().removeRoom(roomInfo);
						}
					}
					//DELETE NOT VALID CACHE
					
					setupRoom(rawRooms, rooms);
					if(rooms.size() == 0){
						emptyView.setVisibility(View.VISIBLE);
						listView.setVisibility(View.GONE);
					}else{
						rooms_original.clear();
						rooms_original.addAll(rooms);
						
						String searchVal = searchEdit.getText().toString();
						if(searchVal.length() > 0){
							onSearch(searchVal);
						}
						
						posBaseViewHolder = new HashMap<Integer, OTMainTabChatList.listElemViewHolder>();
						listView.setVisibility(View.VISIBLE);
						if(listAdapter == null){
							listView.setAdapter(listAdapter = new ListAdapter(this,R.layout.ot_conv_list_elem, rooms));
						}else{
							listAdapter.notifyDataSetChanged();
						}
						emptyView.setVisibility(View.GONE);
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	@Override public void onTokenIsNotValid(JSONObject data) {}
	@Override public void onLimitMaxUser(JSONObject data) {}
	@Override public void onHttpException(Exception ex, JSONObject data, String addr) {}
	@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {}

	public void onSearch(String value){
		if(listAdapter == null) return;
		
		rooms.clear();
		if(value.length() == 0){
			rooms.addAll(rooms_original);
		}else{
			List<TARoomInfo> rawRooms = new ArrayList<TARoomInfo>();
			for(RoomElem room : rooms_original){
				if(room.roomInfo == null) continue;
				for(Long user_id : room.roomInfo.getUsers()){
					String nickName = TAUserNick.getInstance().getUserInfo(user_id);
					if(nickName == null || nickName.length() == 0) continue;
					if(nickName.contains(value)){
						rawRooms.add(room.roomInfo);
						break;
					}
				}
			}
			setupRoom(rawRooms, rooms);
		}
		listAdapter.notifyDataSetChanged();
	}
	
	public void setupRoom(List<TARoomInfo> roomInfo, ArrayList<RoomElem> target){
		boolean hasPrivate = false;
		boolean hasPublic = false;
		target.clear();
		for(TARoomInfo room : roomInfo){
			RoomElem elem = new RoomElem();
			elem.roomInfo = room;
			if(room instanceof TAPublicRoomInfo){
				hasPublic = true;
				elem.roomType = RoomType.PUBLIC_ROOM;
				target.add(elem);
			}else{
				hasPrivate = true;
				elem.roomType = RoomType.PRIVATE_ROOM;
				target.add(elem);
			}
		}
		if(hasPrivate){
			RoomElem elem = new RoomElem();
			elem.roomType = RoomType.PRIVATE_ROOM_LABEL;
			elem.roomLabel = getString(R.string.oto_private_room_label);
			target.add(elem);
		}
		if(hasPublic){
			RoomElem elem = new RoomElem();
			elem.roomType = RoomType.PUBLIC_ROOM_LABEL;
			elem.roomLabel = getString(R.string.oto_public_room_label);
			target.add(elem);	
		}
		Collections.sort(target);
	}
	
	protected void setSearchLayout(){
		searchEdit = (EditText)findViewById(R.id.oto_subtab_search_edit);
		searchEditDelete = (ImageView)findViewById(R.id.oto_subtab_search_edit_delete);
		subTabMoreButton = findViewById(R.id.oto_subtab_more_button);
		
		subTabMoreButton.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				startActivity(new Intent(OTMainTabChatList.this, OTMoreApp.class));
			}
		});
		
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().unRegisterMsgHandler(this);
		}
	}


	@Override
	public void onShowOptionMenu(Menu menu, MenuInflater inf) {
		super.onShowOptionMenu(menu, inf);
		inf.inflate(R.menu.oto_main_tab_menu, menu);
	}

	@Override
	public void onSelectedOptionItem(MenuItem item) {
		super.onSelectedOptionItem(item);
		switch(item.getItemId()){
		case 0:
			getParent().startActivity(new Intent(getParent(), OTMakeChat.class));
			break;
		}
	}

	@Override
	public void onMsgReceived(OTMsgBase msg) {
		if(msg instanceof OTTalkMsgV2){
			OTTalkMsgV2 talkMsg = (OTTalkMsgV2) msg;
			doRefreshPrivateRoom(talkMsg);
			doRefreshPublicRoom(talkMsg);
		}
	}
	
	public void doRefreshPublicRoom(OTTalkMsgV2 lastMsg){
		if(lastMsg != null && lastMsg.publicRoom == false )return;
		
		boolean refresh = true;
		if(lastMsg != null){
			for(RoomElem room : rooms){
				if(room.roomInfo == null) continue;
				if(room.roomInfo.getRoom_id().equals(lastMsg.room_id)){
					refresh = false;
					break;
				}
			}
		}
		if(listAdapter == null){
			refresh = true;
		}
		
		if(refresh){
			satelite.doGetMyPublicRooms(OTOApp.getInstance().getToken());
		}else{
			for(RoomElem elem : rooms){
				if(elem.roomInfo == null) continue;
				elem.roomInfo = OTOApp.getInstance().getCacheCtrl().getRoomMap().get(elem.roomInfo.getRoom_id());
			}
			Collections.sort(rooms);
			rooms_original.clear();
			rooms_original.addAll(rooms);
			
			listAdapter.notifyDataSetChanged();
		}
	}

	public void doRefreshPrivateRoom(OTTalkMsgV2 lastMsg){
		if(lastMsg != null && lastMsg.publicRoom) return;
		
		boolean refresh = true;
		if(lastMsg != null){
			for(RoomElem room : rooms){
				if(room.roomInfo == null) continue;
				if(room.roomInfo.getRoom_id().equals(lastMsg.room_id)){
					refresh = false;
					break;
				}
			}
		}
		if(listAdapter == null){
			refresh = true;
		}
		if(refresh){
			List<TARoomInfo> rawRooms = new ArrayList<TARoomInfo>();
			for(final TARoomInfo room : OTOApp.getInstance().getCacheCtrl().getRoomList()){
				if(room instanceof TAPublicRoomInfo == false){
					rawRooms.add(room);
				}
			}
			setupRoom(rawRooms, rooms);
			
			rooms_original.clear();
			if(rooms.size() == 0){
				emptyView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			}else{
				rooms_original.addAll(rooms);
				String searchVal = searchEdit.getText().toString();
				if(searchVal.length() > 0){
					onSearch(searchVal);
				}
				
				posBaseViewHolder = new HashMap<Integer, OTMainTabChatList.listElemViewHolder>();
				listView.setVisibility(View.VISIBLE);
				if(listAdapter == null){
					listView.setAdapter(listAdapter = new ListAdapter(this,R.layout.ot_conv_list_elem, rooms));
				}else{
					listAdapter.notifyDataSetChanged();
				}
				emptyView.setVisibility(View.GONE);
			}
		}else{
			Collections.sort(rooms);
			rooms_original.clear();
			rooms_original.addAll(rooms);
			
			listAdapter.notifyDataSetChanged();
		}
	}

	class listElemViewHolder{
		LinearLayout layout;
		ImageView img;
		TextView last;
		TextView date;
		TextView cnt;	
		RoomElem room;
		View mainView;
		ImageView alarm;
		TextView talker_name;
		ImageView lock;
		
		View labelView;
		TextView labelTextView;
		
		View titleLayout1;
		TextView titleLayout2;

		public listElemViewHolder(View mainView){
			this.mainView = mainView;
			
			layout = (LinearLayout)mainView.findViewById(R.id.oto_conv_elem_layout);
			img = (ImageView)mainView.findViewById(R.id.oto_conv_elem_img);
			last = (TextView)mainView.findViewById(R.id.oto_conv_elem_lasttalk);
			date = (TextView)mainView.findViewById(R.id.oto_conv_elem_date);
			cnt = (TextView)mainView.findViewById(R.id.oto_conv_elem_cnt);
			talker_name = (TextView)mainView.findViewById(R.id.oto_conv_elem_talker_name);
			alarm = (ImageView)mainView.findViewById(R.id.oto_conv_elem_alarm);
			labelView = mainView.findViewById(R.id.oto_conv_list_info_layout);
			labelTextView = (TextView)mainView.findViewById(R.id.oto_conv_list_info_layout_text);
			lock = (ImageView)mainView.findViewById(R.id.oto_conv_elem_lock);
			
			titleLayout1 = mainView.findViewById(R.id.oto_conv_elem_title_layout);
			titleLayout2 = (TextView) mainView.findViewById(R.id.oto_conv_elem_title_layout2);
			
			talker_name.setSingleLine(false);
			talker_name.setMaxLines(2);
			
			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(room.roomInfo == null) return;
					if(room.roomInfo instanceof TAPublicRoomInfo){
						Intent intent = new Intent(OTMainTabChatList.this, OTPublicChatRoom.class);
						intent.putExtra("room_id", listElemViewHolder.this.room.roomInfo.getRoom_id());
						OTMainTabChatList.this.getContext().startActivity(intent);
					}else{
						Intent intent = new Intent(OTMainTabChatList.this, OTChatRoom.class);
						intent.putExtra("room_id", listElemViewHolder.this.room.roomInfo.getRoom_id());
						OTMainTabChatList.this.getContext().startActivity(intent);
					}
				}
			});
			
			layout.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					if(room.roomInfo == null) return false;
					AlertDialog.Builder ab = new Builder(OTMainTabChatList.this);
					ab.setTitle(getString(R.string.oto_conv_list_modify));
					ab.setItems(getResources().getStringArray(R.array.oto_conv_list_modify_method),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(listElemViewHolder.this.room == null || listElemViewHolder.this.room.roomInfo == null) return;
							switch(which){
							case 0:
								new TASatelite(new TADataHandler() {
									@Override public void onHttpPacketReceived(JSONObject data) {
										try{
											String state = data.getString("state");
											if(state.equals("ok")){
												OTOApp.getInstance().getCacheCtrl().removeRoom(listElemViewHolder.this.room.roomInfo);
												doRefreshPrivateRoom(null);
												doRefreshPublicRoom(null);
												getMyActivityGroup().onChangeView("exit_room", null);
											}
										}catch(Exception ex){
											ex.printStackTrace();
										}
									}
									@Override public void onHttpException(Exception ex, TAMultiData data, String addr) { }
									@Override public void onHttpException(Exception ex, JSONObject data, String addr) { }
									@Override public void onTokenIsNotValid( JSONObject data) {}
									@Override public void onLimitMaxUser(JSONObject data) {}
								}).doExitRoom(OTOApp.getInstance().getToken(), listElemViewHolder.this.room.roomInfo.getRoom_id());
								break;
							}
						}
					});
					ab.show();
					return false;
				}
			});
		}
		
		public View getLabelView() {
			return labelView;
		}

		public void setLabelView(View labelView) {
			this.labelView = labelView;
		}

		public TextView getLabelTextView() {
			return labelTextView;
		}

		public void setLabelTextView(TextView labelTextView) {
			this.labelTextView = labelTextView;
		}

		public View getMainView() {
			return mainView;
		}
		public ImageView getAlarm() {
			return alarm;
		}
		public void setAlarm(ImageView alarm) {
			this.alarm = alarm;
		}
		public void setMainView(View mainView) {
			this.mainView = mainView;
		}
		public LinearLayout getLayout() {
			return layout;
		}
		public void setLayout(LinearLayout layout) {
			this.layout = layout;
		}
		public ImageView getImg() {
			return img;
		}
		public void setImg(ImageView img) {
			this.img = img;
		}
		public TextView getDate() {
			return date;
		}
		public void setDate(TextView date) {
			this.date = date;
		}
		public TextView getCnt() {
			return cnt;
		}
		public void setCnt(TextView cnt) {
			this.cnt = cnt;
		}
		public RoomElem getRoom() {
			return room;
		}

		public void setRoom(RoomElem room) {
			this.room = room;
		}

		public TextView getTalker_name() {
			return talker_name;
		}

		public void setTalker_name(TextView talker_name) {
			this.talker_name = talker_name;
		}
		
	}
	class ListAdapter extends ArrayAdapter<RoomElem>{
		ArrayList<RoomElem> items;

		public ListAdapter(Context context, int textViewResourceId, ArrayList<RoomElem> objects) {
			super(context, textViewResourceId, objects);
			items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			listElemViewHolder holder;
			if(convertView == null){
				LayoutInflater vi = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.ot_conv_list_elem, null);
				holder = new listElemViewHolder(convertView);
				convertView.setTag(holder);
			}else{
				holder = (listElemViewHolder)convertView.getTag();
			}
			
			holder.setRoom(items.get(position));
			
			if(holder.room.roomInfo != null){
				holder.layout.setVisibility(View.VISIBLE);
				holder.labelView.setVisibility(View.GONE);
				final ImageView img = holder.img;
				TextView date = holder.date;
				TextView cnt = holder.cnt;
				TARoomInfo room = holder.room.roomInfo;
				
				if(OTOApp.getInstance().getCacheCtrl().getTalkAlarm(room.getRoom_id())){
					holder.getAlarm().setImageResource(R.drawable.oto_alarm_small_on);
					holder.getAlarm().setVisibility(View.GONE);
				}else{
					holder.getAlarm().setImageResource(R.drawable.oto_alarm_small_off);
					holder.getAlarm().setVisibility(View.VISIBLE);
				}
				
				if(room instanceof TAPublicRoomInfo){
					holder.titleLayout1.setVisibility(View.VISIBLE);
					holder.titleLayout2.setVisibility(View.GONE);
					TAPublicRoomInfo publicRoom = (TAPublicRoomInfo) room;
					if(publicRoom.getImg_url() == null || publicRoom.getImg_url().length() == 0){
						img.setImageResource(R.drawable.oto_oto_logo);
					}else{
						String realUrl = TASatelite.makeImageUrl(publicRoom.getImg_url());
						loadImageOnList(realUrl, img, R.drawable.oto_oto_logo, this, true, false);
					}
					
					holder.lock.setVisibility(View.VISIBLE);
					if(publicRoom.isHidden()){
						holder.lock.setImageResource(R.drawable.oto_lock_black);
					}else{
						holder.lock.setImageResource(R.drawable.oto_unlock_black);
					}
					
					holder.getTalker_name().setText( "("+ String.valueOf(publicRoom.getUsers().size()) + ")"+publicRoom.getName());
					String nickName = TAUserNick.getInstance().getUserInfo(publicRoom.getOwner());
					holder.last.setText(nickName);
					
					if(publicRoom.getLast_msg_time() == 0L){
						date.setText(getString(R.string.oto_none));
					}else{
						date.setText(PLEtcUtilMgr.getDateFormat(publicRoom.getLast_msg_time()));
					}
				}else{
					holder.titleLayout1.setVisibility(View.GONE);
					holder.titleLayout2.setVisibility(View.VISIBLE);
					holder.lock.setVisibility(View.GONE);
					
					if(room == null || room.getLastMsg() == null || room.getLastMsg().getMsg() == null){
						return holder.getMainView();
					}
					if(room.getUsers().size() == 2){
						Long user_id = room.getUsers().get(0);
						if(user_id == OTOApp.getInstance().getId()){
							user_id = room.getUsers().get(1);
						}
						String nickName = TAUserNick.getInstance().getUserInfo(user_id);
						holder.titleLayout2.setText(nickName);
					}else if(room.getUsers().size() > 2){
						String userNames = "(" + String.valueOf(room.getUsers().size()) + ")";
						boolean f = false;
						for(Long user_id : room.getUsers()){
							if(user_id == OTOApp.getInstance().getId()) continue;
							if(f) userNames += ", ";
							String nickName = TAUserNick.getInstance().getUserInfo(user_id);
							userNames += nickName;
							f = true;
						}
						holder.titleLayout2.setText(userNames);
					}else{
						holder.titleLayout2.setText(getString(R.string.oto_unknown));
					}
					
					img.setImageResource(R.drawable.oto_friend_img_01);
					if(room.getUsers().size() == 2){
						long tUserId = room.getUsers().get(0);
						if(tUserId == OTOApp.getInstance().getId()) { tUserId = room.getUsers().get(1);}
						final long userId = tUserId;
						
						UserImageUrlHelper.loadUserImage(userId, new OnLoadUserImageUrl() {
							@Override public void onLoad(final long user_id, final String url, final boolean fromCache) {
								loadImageOnList(url, img, R.drawable.oto_friend_img_01, listAdapter, true, false);
							}
						});
					}
					
					if(room.getLastMsg().getMsg().length() == 0){
						if(room.getLastMsg().isImgMsg()){
							holder.last.setText(getString(R.string.oto_picture));
						}else if(room.getLastMsg().isInviteMsg()){
							holder.last.setText(getString(R.string.oto_context_menu_4));
						}else if(room.getLastMsg().isExitMsg()){
							holder.last.setText(getString(R.string.oto_exit_room));
						}
					}else{
						holder.last.setText(room.getLastMsg().getMsg());
					}
					date.setText(PLEtcUtilMgr.getDateFormat(room.getLastMsg().getTime()));
				}
				
				if(room.getUnread_msg_cnt() > 0){
					cnt.setText(String.valueOf(room.getUnread_msg_cnt()));
					cnt.setVisibility(View.VISIBLE);
				}else{
					cnt.setVisibility(View.INVISIBLE);
				}
			}else{
				holder.layout.setVisibility(View.GONE);
				holder.labelView.setVisibility(View.VISIBLE);
				holder.labelTextView.setText(holder.room.roomLabel);
			}
			return holder.getMainView();
		}
	}
}

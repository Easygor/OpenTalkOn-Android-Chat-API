package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.helper.UserElemClickListener;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListElem;
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListElem.LabelType;
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListViewHolder;

public class OTMakeChat extends OTImageLoadBase implements UserElemClickListener, TADataHandler {
	ListView listView;
	TASatelite satelite;
	
	TextView emptyView;
	Button okBtn;
	Button cancelBtn;
	
	ArrayList<CheckFriendListElem> friendListElems = null;
	ArrayList<CheckFriendListElem> ongoingFriendListElems = null;
	ArrayList<CheckFriendListElem> listDataSet = null;
	ListAdapter adapter = null;
	
	ArrayList <Long> ongoingUsers = null;
	Map<Long, Boolean> ongoingUsersMap = null;
	
	Map<CheckFriendListElem, FriendListViewHolder> itemToViewHolder= new HashMap<CheckFriendListElem, FriendListViewHolder>();
	Map<TAUserInfo, CheckFriendListElem> userItemToElem= new HashMap<TAUserInfo, CheckFriendListElem>();
	boolean inviteAcitivity;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_invite_chat_layout);
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		
		listView = (ListView) findViewById(R.id.oto_make_chat_list_list);
		emptyView = (TextView) findViewById(R.id.oto_make_chat_list_empty);
		okBtn = (Button) findViewById(R.id.oto_make_chat_ok_btn);
		cancelBtn = (Button) findViewById(R.id.oto_make_chat_cancel_btn);
		
		satelite = new TASatelite(this);
		listDataSet = new ArrayList<CheckFriendListElem>();
		adapter = new ListAdapter(this, R.layout.ot_elem_friend, listDataSet);
		listView.setAdapter(adapter);

		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		if(intent.hasExtra("invite")){
			inviteAcitivity = true;
			ongoingUsers = (ArrayList <Long>)intent.getSerializableExtra("users");
			ongoingUsersMap = new HashMap<Long, Boolean>();
			for(long user_id : ongoingUsers){
				ongoingUsersMap.put(user_id, true);
			}
		}else{
			inviteAcitivity = false;
		}
		
		getFriendsList();
		
		okBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				onConfirm();
			}
		});
		
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	public void onConfirm(){
		ArrayList<Long> users = new ArrayList<Long>();
		for(CheckFriendListElem elem : friendListElems){
			if(elem.isCheck()){
				users.add(elem.getInfo().getId());
			}
		}
		if(users.size() == 0){
			OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTMakeChat.this, getString(R.string.oto_add_chat_users), getString(R.string.oto_add_friend_at_least_one));
			return;
		}
		
		if(inviteAcitivity){
			Intent intent = new Intent();
			intent.putExtra("invite_users", users);
			setResult(RESULT_OK, intent);
			finish();
		}else{
			users.add(OTOApp.getInstance().getId());
			Intent intent = new Intent(OTMakeChat.this, OTChatRoom.class);
			intent.putExtra("user_list", users);
			OTMakeChat.this.startActivity(intent);
			finish();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		DispatchedData dispatchData = TASateliteDispatcher.dispatchSateliteData(data);
		if(dispatchData.getLocation().equals(TASatelite.getName(TASatelite.GET_USER_INFOS_URL))){
			if(dispatchData.isOK()){
				ArrayList<TAUserInfo> userInfos = (ArrayList<TAUserInfo>)dispatchData.getData();
				ongoingFriendListElems = makeFriendList(userInfos, true);
				for(CheckFriendListElem elem : ongoingFriendListElems){
					userItemToElem.put(elem.info, elem);
				}
				dataToListView();
			}
		}
		if(dispatchData.getLocation().equals(TASatelite.getName(TASatelite.GET_FRIENDS_URL))){
			if(dispatchData.isOK()){
				ArrayList<TAUserInfo> userInfos = (ArrayList<TAUserInfo>)dispatchData.getData();
				friendListElems = makeFriendList(userInfos, false);
				for(CheckFriendListElem elem : friendListElems){
					userItemToElem.put(elem.info, elem);
				}
				dataToListView();
			}else{
				String state = dispatchData.getState();
				if(state.equals("user_id is not valid")){
				}else if(state.equals("can't add yourself")){
				}else if(state.equals("already added")){
				}else if(state.equals("token is not valid")){
				}
			}
		}
	}
	
	boolean finishGuard = false;
	
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
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
	
	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
	
	ArrayList<CheckFriendListElem> getLayoutedFriendList(ArrayList<CheckFriendListElem> friendList){
		ArrayList<CheckFriendListElem> layoutedList = new ArrayList<CheckFriendListElem>();
		int bestFriendCount = 0;
		int friendCount = 0;
		
		for(CheckFriendListElem elem : friendList){
			if(elem.info != null && elem.info.is_friend()){
				if(elem.info.isFriend_best()){
					++bestFriendCount;
				}else{
					++friendCount;
				}
				layoutedList.add(elem);
			}
		}
		
		if(bestFriendCount > 0){
			CheckFriendListElem bestFriendLabel = new CheckFriendListElem();
			bestFriendLabel.setDivider(true);
			bestFriendLabel.setFriendLabelType(LabelType.TYPE_BEST_FRIEND);
			bestFriendLabel.setDivider_text(String.format(getString(R.string.oto_friend_label_1), bestFriendCount));
			layoutedList.add(bestFriendLabel);
		}
		if(friendCount > 0){
			CheckFriendListElem friendLabel = new CheckFriendListElem();
			friendLabel.setDivider(true);
			friendLabel.setFriendLabelType(LabelType.TYPE_FRIEND);
			friendLabel.setDivider_text(String.format(getString(R.string.oto_friend_label_2), friendCount));
			layoutedList.add(friendLabel);
		}
		
		Collections.sort(layoutedList);
		return layoutedList;
	}
	
	public void dataToListView(){
		if(ongoingUsers == null){
			if(friendListElems != null){
				if(friendListElems.size() == 0){
					listView.setVisibility(View.GONE);
					emptyView.setVisibility(View.VISIBLE);
				}else{
					listView.setVisibility(View.VISIBLE);
					emptyView.setVisibility(View.GONE);
					
					listDataSet.clear();
					listDataSet.addAll(getLayoutedFriendList(friendListElems));
					Collections.sort(listDataSet);
					adapter.notifyDataSetChanged();
				}
			}else{
				listView.setVisibility(View.GONE);
				emptyView.setVisibility(View.VISIBLE);
				emptyView.setText(getString(R.string.oto_network_fail));
			}
		}else{
			listView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
			
			listDataSet.clear();
			if(ongoingFriendListElems != null){
				listDataSet.addAll(ongoingFriendListElems);
				CheckFriendListElem friendLabel = new CheckFriendListElem();
				friendLabel.setInviteRoomDivider(true);
				friendLabel.setInviteRoomLabelType(CheckFriendListElem.InviteRoomLabelType.TYPE_ONGOING);
				friendLabel.setDivider_text(String.format(getString(R.string.oto_invite_label_1), ongoingFriendListElems.size()));
				listDataSet.add(friendLabel);
			}
			
			if(friendListElems != null){
				listDataSet.addAll(getLayoutedFriendList(friendListElems));
			}
			Collections.sort(listDataSet);
			adapter.notifyDataSetChanged();
		}
	}
	
	public ArrayList<CheckFriendListElem> makeFriendList(ArrayList<TAUserInfo> friendList, boolean ongoing){
		ArrayList<CheckFriendListElem> list = new ArrayList<CheckFriendListElem>();
		for(TAUserInfo user : friendList){
			if(ongoing == false && ongoingUsersMap !=null &&
				ongoingUsersMap.containsKey(user.getId())) continue;
			CheckFriendListElem item = new CheckFriendListElem();
			item.setOngoing(ongoing);
			item.setInfo(user);
			list.add(item);
		}
		
		return list;
	}
	
	public void getFriendsList(){
		OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
		satelite.doGetFriends(OTOApp.getInstance().getToken());
		if(ongoingUsers != null){
			satelite.doGetUserInfos(OTOApp.getInstance().getToken(), ongoingUsers);
		}
	}
	
	@Override
	public void elemClicked(TAUserInfo info) {
		if(userItemToElem.containsKey(info)){
			CheckFriendListElem elem = userItemToElem.get(info);
			if(elem.isOngoing()){
				Intent intent = new Intent(this, OTFriendPopup.class);
				intent.putExtra("user_id", info.getId());
				startActivity(intent);
			}else{
				elem.setCheck(!elem.isCheck());
				if(adapter != null){
					adapter.notifyDataSetChanged();
				}
			}
		}
	}
	
	@Override
	public void elemLongClicked(TAUserInfo info) {
		
	}

	public static class CheckFriendListElem extends FriendListElem{
		enum InviteRoomLabelType{
			TYPE_ONGOING
		}
		boolean check;
		boolean ongoing;
		
		boolean inviteRoomDivider;
		InviteRoomLabelType inviteRoomLabelType;
		
		public boolean isInviteRoomDivider() {
			return inviteRoomDivider;
		}

		public void setInviteRoomDivider(boolean inviteRoomDivider) {
			this.inviteRoomDivider = inviteRoomDivider;
		}

		public InviteRoomLabelType getInviteRoomLabelType() {
			return inviteRoomLabelType;
		}

		public void setInviteRoomLabelType(InviteRoomLabelType inviteRoomLabelType) {
			this.inviteRoomLabelType = inviteRoomLabelType;
		}

		public boolean isCheck() {
			return check;
		}

		public void setCheck(boolean check) {
			this.check = check;
		}
		
		public boolean isOngoing() {
			return ongoing;
		}

		public void setOngoing(boolean ongoing) {
			this.ongoing = ongoing;
		}
		
		@Override
		public int compareTo(FriendListElem another) {
			return compare(this, (CheckFriendListElem)another);
		}
		
		int getSortPoint(CheckFriendListElem elem){
			int point;
			if(elem.inviteRoomDivider && elem.inviteRoomLabelType == InviteRoomLabelType.TYPE_ONGOING){
				point = 0;
			}else if(elem.info != null && elem.ongoing){
				point = 1;
			}else{
				point = 2;
			}
			return point;
		}

		int compare(CheckFriendListElem lhs, CheckFriendListElem rhs) {
			int leftSortPoint = getSortPoint(lhs);
			int rightSortPoint = getSortPoint(rhs);
			
			if(leftSortPoint != rightSortPoint){
				return leftSortPoint < rightSortPoint?-1:1;
			}else{
				if(leftSortPoint == 1){
					return lhs.info.compareTo(rhs.info);
				}else{
					return super.compare(lhs, rhs);
				}
			}
		}
	}

	class ListAdapter extends ArrayAdapter<CheckFriendListElem>{
		ArrayList<CheckFriendListElem> items;

		public ListAdapter(Context context, int textViewResourceId,
				ArrayList<CheckFriendListElem> objects) {
			super(context, textViewResourceId, objects);
			items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FriendListViewHolder holder = null;
			if(convertView == null){
				holder = new FriendListViewHolder(R.layout.ot_elem_friend, getLayoutInflater(), OTMakeChat.this);
				convertView = holder.getMainView();
			}else{
				holder = (FriendListViewHolder)convertView.getTag();
			}
			CheckFriendListElem item = items.get(position);
			holder.setItem(item);
			itemToViewHolder.put(item, holder);
			
			View layout1 = holder.getLayout1();
			TextView layout1Text = holder.getLayout1Text();
			LinearLayout layout2 = holder.getLayout2();
			
			final ImageView userImg = holder.getLeftImg();
			TextView user_id = holder.user_id;
			TextView introduce = holder.getIntroduce();
			
			ViewGroup checkLayout = holder.getCheckLayout();
			ImageView check = holder.getCheck();
			
			if(item.isDivider()){
				layout1.setVisibility(View.VISIBLE);
				layout1Text.setText(item.getDivider_text());
				layout2.setVisibility(View.GONE);
				introduce.setVisibility(View.GONE);
			} else if(item.isInviteRoomDivider()){
				layout1.setVisibility(View.VISIBLE);
				layout1Text.setText(item.getDivider_text());
				layout2.setVisibility(View.GONE);
				introduce.setVisibility(View.GONE);
			}else{
				if(item.ongoing){
					checkLayout.setVisibility(View.GONE);
				}else{
					checkLayout.setVisibility(View.VISIBLE);
					if(item.isCheck()){
						check.setImageResource(R.drawable.oto_check_s);
					}else{
						check.setImageResource(R.drawable.oto_check_n);
					}
				}
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.VISIBLE);
				introduce.setVisibility(View.VISIBLE);
				
				if(item.getInfo().getImagePath().length() != 0){
					String url = TASatelite.makeImageUrl(item.getInfo().getImagePath());
					loadImageOnList(url, userImg, R.drawable.oto_friend_img_01, adapter, true, false);
				}else{
					userImg.setImageResource(R.drawable.oto_friend_img_01);
				}
				
				user_id.setText(item.getInfo().getNickName());
				
				String intro = item.getInfo().getIntroduce();
				if(intro == null || intro.length() == 0){
					introduce.setVisibility(View.GONE);
				}else{
					introduce.setText(intro);
				}
			}
			
			return holder.getMainView();
		}
	}
}

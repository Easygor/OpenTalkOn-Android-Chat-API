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
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListViewHolder;

public class OTSelectUser extends OTImageLoadBase implements UserElemClickListener, TADataHandler{
	ListView listView;
	ListAdapter adapter = null;
	
	TextView emptyView;
	Button okBtn;
	Button cancelBtn;
	TASatelite satelite;
	
	ArrayList <Long> users = null;
	ArrayList<CheckFriendListElem> listDataSet = null;
	Map<TAUserInfo, CheckFriendListElem> userItemToElem= new HashMap<TAUserInfo, CheckFriendListElem>();
	
	boolean single;
	boolean checked = false;
	long checked_user_id;
	
	@SuppressWarnings("unchecked")
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
		listDataSet = new ArrayList<CheckFriendListElem>();
		adapter = new ListAdapter(this, R.layout.ot_elem_friend, listDataSet);
		listView.setAdapter(adapter);
		satelite = new TASatelite(this);

		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		
		Intent intent = getIntent();
		if(intent == null || !intent.hasExtra("users")){
			finish();
			return;
		}
		
		single = intent.getBooleanExtra("single", false);
		users = (ArrayList <Long>)intent.getSerializableExtra("users");
		satelite.doGetUserInfos(OTOApp.getInstance().getToken(), users);
		
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		DispatchedData dispatchData = TASateliteDispatcher.dispatchSateliteData(data);
		if(dispatchData.getLocation().equals(TASatelite.getName(TASatelite.GET_USER_INFOS_URL))){
			if(dispatchData.isOK()){
				ArrayList<TAUserInfo> userInfos = (ArrayList<TAUserInfo>)dispatchData.getData();
				for(TAUserInfo user : userInfos){
					CheckFriendListElem elem = new CheckFriendListElem();
					elem.setInfo(user);
					listDataSet.add(elem);
				}
				for(CheckFriendListElem elem : listDataSet){
					userItemToElem.put(elem.info, elem);
				}
				
				listView.setVisibility(View.VISIBLE);
				emptyView.setVisibility(View.GONE);
				
				Collections.sort(listDataSet);
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	@Override
	public void onTokenIsNotValid(JSONObject data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLimitMaxUser(JSONObject data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		// TODO Auto-generated method stub
		
	}

	public void onConfirm(){
		ArrayList<Long> users = new ArrayList<Long>();
		if(single){
			if(checked == false){
				OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTSelectUser.this, getString(R.string.oto_add_chat_users), getString(R.string.oto_add_friend_at_least_one));
				return;
			}
			users.add(checked_user_id);
		}else{
			for(CheckFriendListElem elem : listDataSet){
				if(elem.isCheck()){
					users.add(elem.getInfo().getId());
				}
			}
			if(users.size() == 0){
				OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTSelectUser.this, getString(R.string.oto_add_chat_users), getString(R.string.oto_add_friend_at_least_one));
				return;
			}
		}
		
		Intent intent = new Intent();
		intent.putExtra("selected_users", users);
		setResult(RESULT_OK, intent);
		finish();
	}

	public static class CheckFriendListElem extends FriendListElem{
		boolean check;
		
		public boolean isCheck() {
			return check;
		}
		public void setCheck(boolean check) {
			this.check = check;
		}
	}

	@Override
	public void elemClicked(TAUserInfo info) {
		if(userItemToElem.containsKey(info)){
			CheckFriendListElem elem = userItemToElem.get(info);
			if(single){
				checked = true;
				checked_user_id = info.getId();
			}else{
				elem.setCheck(!elem.isCheck());
			}
			if(adapter != null){
				adapter.notifyDataSetChanged();
			}
		}
	}

	@Override public void elemLongClicked(TAUserInfo info) {}

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
				holder = new FriendListViewHolder(R.layout.ot_elem_friend, getLayoutInflater(), OTSelectUser.this);
				convertView = holder.getMainView();
			}else{
				holder = (FriendListViewHolder)convertView.getTag();
			}
			CheckFriendListElem item = items.get(position);
			holder.setItem(item);
			
			View layout1 = holder.getLayout1();
			LinearLayout layout2 = holder.getLayout2();
			
			final ImageView userImg = holder.getLeftImg();
			TextView user_id = holder.user_id;
			TextView introduce = holder.getIntroduce();
			
			ViewGroup checkLayout = holder.getCheckLayout();
			ImageView check = holder.getCheck();
			
			if(single){
				if(checked){
					TAUserInfo userInfo = item.getInfo();
					if(checked_user_id == userInfo.getId()){
						checkLayout.setVisibility(View.VISIBLE);
						check.setImageResource(R.drawable.oto_check_s);
					}else{
						checkLayout.setVisibility(View.VISIBLE);
						check.setImageResource(R.drawable.oto_check_n);
					}
				}else{
					checkLayout.setVisibility(View.VISIBLE);
					check.setImageResource(R.drawable.oto_check_n);
				}
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
			
			return holder.getMainView();
		}
	}
}

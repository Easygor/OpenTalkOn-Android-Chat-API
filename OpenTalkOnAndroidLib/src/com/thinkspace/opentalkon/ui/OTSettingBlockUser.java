package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAIgnore;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.helper.UserElemClickListener;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListElem;
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListViewHolder;
import com.thinkspace.opentalkon.ui.helper.ImageCacheActivity;

public class OTSettingBlockUser extends ImageCacheActivity implements UserElemClickListener, TADataHandler {
	ListView listView;
	View cancel;
	View ok;
	View empty;
	
	TASatelite satelite; 
	Map<FriendListElem, FriendListViewHolder> itemToViewHolder= new HashMap<FriendListElem, FriendListViewHolder>();
	ArrayList<FriendListElem> userList = new ArrayList<FriendListElem>();
	ListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_invite_chat_layout);
		listView = (ListView) findViewById(R.id.oto_make_chat_list_list);
		cancel = findViewById(R.id.oto_make_chat_cancel_btn);
		empty = findViewById(R.id.oto_ignore_list_empty);
		ok = findViewById(R.id.oto_make_chat_ok_btn);
		
		cancel.setVisibility(View.GONE);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		satelite = new TASatelite(this);
	}
	
	boolean finishGuard;
	
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
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		try{
			String state = data.getString("state");
			String location = data.getString("location");
			if(location.equals(TASatelite.getName(TASatelite.GET_USER_INFOS_URL))){
				if(state.equals("ok")){
					JSONArray userData = data.getJSONArray("data");
					userList.clear();
					for(int i=0;i<userData.length();++i){
						TAUserInfo user = TASateliteDispatcher.dispatchUserInfo(userData.getJSONObject(i), false);
						FriendListElem elem = new FriendListElem();
						elem.setInfo(user);
						userList.add(elem);
					}
					listView.setAdapter(adapter = new ListAdapter(this, -1, userList));
				}
			}
		}catch(JSONException ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	public void doGetIgnoreUserInfos(){
		ArrayList<Long> Ids = new ArrayList<Long>();
		for(TAIgnore ig : OTOApp.getInstance().getCacheCtrl().getIgnoreList()){
			Ids.add(ig.getUser_id());
		}
		if(Ids.size() != 0){
			listView.setVisibility(View.VISIBLE);
			empty.setVisibility(View.GONE);
			OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
			satelite.doGetUserInfos(OTOApp.getInstance().getToken(), Ids);
		}else{
			listView.setVisibility(View.GONE);
			empty.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void elemClicked(TAUserInfo info) {
		Intent intent = new Intent(this, OTFriendPopup.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable("user_info", info);
		intent.putExtras(bundle);
		startActivityForResult(intent, 100);
	}
	
	@Override
	public void elemLongClicked(TAUserInfo info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		doGetIgnoreUserInfos();
	}



	class ListAdapter extends ArrayAdapter<FriendListElem>{
		ArrayList<FriendListElem> items;

		public ListAdapter(Context context, int textViewResourceId, ArrayList<FriendListElem> objects) {
			super(context, textViewResourceId, objects);
			items = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FriendListViewHolder holder = null;
			if(convertView == null){
				holder = new FriendListViewHolder(R.layout.ot_elem_friend, getLayoutInflater(), OTSettingBlockUser.this);
				convertView = holder.getMainView();
			}else{
				holder = (FriendListViewHolder)convertView.getTag();
			}
			FriendListElem item = items.get(position);
			holder.setItem(item);
			itemToViewHolder.put(item, holder);
			
			View layout1 = holder.getLayout1();
			LinearLayout layout2 = holder.getLayout2();
			
			final ImageView userImg = holder.getLeftImg();
			TextView user_id = holder.user_id;
			TextView introduce = holder.getIntroduce();
			
			holder.getCheckLayout().setVisibility(View.GONE);
			
			if(item.isDivider()){
				layout1.setVisibility(View.VISIBLE);
				layout2.setVisibility(View.GONE);
				introduce.setVisibility(View.GONE);
			}else{
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

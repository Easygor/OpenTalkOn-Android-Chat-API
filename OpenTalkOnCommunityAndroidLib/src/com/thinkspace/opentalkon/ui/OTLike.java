package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
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
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListElem;
import com.thinkspace.opentalkon.ui.OTFriendListBase.FriendListViewHolder;
import com.thinkspace.opentalkon.ui.helper.ImageCacheActivity;

public class OTLike extends ImageCacheActivity implements UserElemClickListener, TADataHandler{
	Map<FriendListElem, FriendListViewHolder> itemToViewHolder = new HashMap<FriendListElem, FriendListViewHolder>();;
	
	long post_id;
	long last_id;
	ListView list;
	TextView empty;
	TASatelite satelite;
	boolean networking = false;
	boolean finished = false;
	
	ArrayList<FriendListElem> userList = new ArrayList<FriendListElem>();
	ListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_like_layout);
		
		TextView title = (TextView)findViewById(R.id.oto_base_tab_title);
		title.setText(getString(R.string.oto_like_people));
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		last_id = -1;
		Intent intent = getIntent();
		if(intent == null || intent.hasExtra("post_id") == false){
			finish();
			return;
		}
		post_id = intent.getLongExtra("post_id", -1L);
		
		list = (ListView)findViewById(R.id.oto_ot_like_layout_list);
		empty = (TextView)findViewById(R.id.oto_ot_like_layout_empty);
		
		list.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(list.getChildCount() > 0 && list.getLastVisiblePosition() == list.getAdapter().getCount() -1 &&
						list.getChildAt(list.getChildCount() - 1).getBottom() <= list.getHeight()){
					doGetNextLikePeoples();
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				
			}
		});
		
		satelite = new TASatelite(this);
		doGetNextLikePeoples();
	}
	
	public void doGetNextLikePeoples(){
		if(networking == false && finished == false){
			networking = true;
			OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
			satelite.doGetCommunityLikePeoples(OTOApp.getInstance().getToken(), post_id, last_id);
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
	public void onHttpPacketReceived(JSONObject data) {
		networking = false;
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		try{
			String state = data.getString("state");
			if(state.equals("ok")){
				JSONArray retData = data.getJSONArray("data");
				if(retData.length() == 0){
					finished = true;
				}else{
					if(OTOApp.getInstance().getDB().beginTransaction()){
						for(int i=0;i<retData.length();++i){
							TAUserInfo userInfo = TASateliteDispatcher.dispatchUserInfo(retData.getJSONObject(i), true);
							FriendListElem elem = new FriendListElem();
							elem.setInfo(userInfo);
							userList.add(elem);
							last_id = retData.getJSONObject(i).getLong("like_id");
						}
						OTOApp.getInstance().getDB().endTransaction();
					}
					
					if(adapter == null){
						list.setAdapter(adapter = new ListAdapter(OTLike.this, -1, userList));
					}else{
						adapter.notifyDataSetChanged();
					}
				}
			}
		}catch(Exception ex){}
	}
	
	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		networking = false;
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
	
	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		networking = false;
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
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
				holder = new FriendListViewHolder(R.layout.ot_elem_friend, getLayoutInflater(), OTLike.this);
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

	@Override
	public void elemClicked(TAUserInfo info) {
		Intent intent = new Intent(this, OTFriendPopup.class);
		intent.putExtra("user_id", info.getId());
		startActivity(intent);
	}

	@Override
	public void elemLongClicked(TAUserInfo info) {
		
	}
}

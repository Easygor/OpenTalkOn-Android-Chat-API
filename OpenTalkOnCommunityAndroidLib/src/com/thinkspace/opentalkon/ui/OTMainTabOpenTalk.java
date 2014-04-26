package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.List;

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

import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.CommunityData;
import com.thinkspace.opentalkon.data.CommunityLastTimeTable;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TARoomInfo;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroupView;
import com.thinkspace.pushservice.satelite.PLNotifyHandler;

public class OTMainTabOpenTalk extends PLActivityGroupView implements TADataHandler, PLNotifyHandler {
	ListView listView;
	ListAdapter listAdapter;
	TextView emptyView;
	View progressView;
	EditText searchEdit;
	View searchEditDelete;
	View subTabMoreButton;
	
	View subTabNotiButton;
	ImageView subTabNotiImage;
	
	TASatelite satelite;
	ArrayList<CommunityData> comDatas = new ArrayList<CommunityData>();
	ArrayList<CommunityData> comDatas_Original;
	boolean first;
	
	int app_code;
	
	@Override
	public void onTabDestoryed() {
		super.onTabDestoryed();
		clearCacheFromThis();
	}



	public void onSearch(String value){
		if(comDatas == null) return;
		if(comDatas_Original == null) return;
		
		comDatas.clear();
		if(value.length() == 0){
			comDatas.addAll(comDatas_Original);
		}else{
			for(CommunityData elem : comDatas_Original){
				String title = elem.title;
				if(title != null && title.length() != 0){
					if(title.contains(value)){
						comDatas.add(elem);
					}
					continue;
				}
			}
		}
		if(listAdapter != null){
			listAdapter.notifyDataSetChanged();
		}
	}
	
	
	
	@Override
	public void onNotify(Notify packet) {
		if(packet.hasLikeMine() || packet.hasCommentMine() || packet.hasNewReply()){
			if(OTOApp.getInstance().getCacheCtrl().hasNewNotification()){
				subTabNotiImage.setImageResource(R.drawable.oto_notification_new_icon);
			}else{
				subTabNotiImage.setImageResource(R.drawable.oto_notification_icon);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_sub_tab_community);
		first = true;
		
		listView = (ListView)findViewById(R.id.oto_community_list);
		emptyView = (TextView)findViewById(R.id.oto_community_empty);
		progressView = findViewById(R.id.oto_community_progress);
		searchEdit = (EditText) findViewById(R.id.oto_subtab_search_edit);
		searchEditDelete = findViewById(R.id.oto_subtab_search_edit_delete);
		subTabMoreButton = findViewById(R.id.oto_subtab_more_button);
		subTabNotiButton = findViewById(R.id.oto_subtab_notification_button);
		subTabNotiImage = (ImageView)findViewById(R.id.oto_subtab_notification_img);
		setSearchLayout();
		
		Intent intent = getIntent();
		app_code = (int)intent.getLongExtra("app_id", -1L);
		
		if(app_code != -1L){
			subTabMoreButton.setVisibility(View.GONE);
		}else{
			app_code = OTOApp.getInstance().getAppCode();
			subTabMoreButton.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					startActivity(new Intent(OTMainTabOpenTalk.this, OTMoreApp.class));
				}
			});
		}
		
		subTabNotiButton.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				startActivity(new Intent(OTMainTabOpenTalk.this, OTNotification.class));
			}
		});
		
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().registerNotifyHandler(this);
		}
		
		satelite = new TASatelite(this);
		UserImageUrlHelper.flushUserIdMap();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(first){
			loadCommunitys(true);
			first = false;
		}else{
			if(searchEdit.length() == 0){
				loadCommunitys(false);
			}
		}
		if(listAdapter != null){
			listAdapter.notifyDataSetChanged();
		}
		
		if(OTOApp.getInstance().getCacheCtrl().hasNewNotification()){
			subTabNotiImage.setImageResource(R.drawable.oto_notification_new_icon);
		}else{
			subTabNotiImage.setImageResource(R.drawable.oto_notification_icon);
		}
	}

	void setSearchLayout(){
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
	
	class ListViewHolder{
		LinearLayout layout;
		ImageView img;
		TextView last;
		TextView date; 
		TextView cnt;	
		TARoomInfo room;
		View mainView;
		TextView titleName;
		ImageView titleImage;
		ImageView alarm;
		CommunityData data;
		View rightLayout1;
		View rightLayout2;
		
		View titleLayout1;
		TextView titleLayout2;

		public ListViewHolder(View mainView){
			this.mainView = mainView;
			
			layout = (LinearLayout)mainView.findViewById(R.id.oto_conv_elem_layout);
			img = (ImageView)mainView.findViewById(R.id.oto_conv_elem_img);
			last = (TextView)mainView.findViewById(R.id.oto_conv_elem_lasttalk);
			date = (TextView)mainView.findViewById(R.id.oto_conv_elem_date);
			cnt = (TextView)mainView.findViewById(R.id.oto_conv_elem_cnt);
			titleName = (TextView)mainView.findViewById(R.id.oto_conv_elem_talker_name);
			titleImage = (ImageView)mainView.findViewById(R.id.oto_conv_elem_title_image);
			alarm = (ImageView)mainView.findViewById(R.id.oto_conv_elem_alarm);
			rightLayout1 = mainView.findViewById(R.id.oto_conv_elem_right_layout_1);
			rightLayout2 = mainView.findViewById(R.id.oto_conv_elem_right_layout_2);
			
			titleLayout1 = mainView.findViewById(R.id.oto_conv_elem_title_layout);
			titleLayout2 = (TextView) mainView.findViewById(R.id.oto_conv_elem_title_layout2);
			titleLayout1.setVisibility(View.VISIBLE);
			titleLayout2.setVisibility(View.GONE);
			
			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = null;
					if(data.community_group){
						intent = new Intent(OTMainTabOpenTalk.this, OTComGroupActivity.class);
						intent.putExtra("community_group_id", data.id);
						intent.putExtra("app_code", app_code);
					}else if(data.public_opentalk){
						intent = new Intent(OTMainTabOpenTalk.this, OTPublicTalkList.class);
						intent.putExtra("community_data", data);
						intent.putExtra("authority", OTOApp.getInstance().getAppCode() == app_code);
					}else{
						if(data.need_picture){
							intent = new Intent(OTMainTabOpenTalk.this, OTOpenTalkImageRoom.class);
						}else{
							intent = new Intent(OTMainTabOpenTalk.this, OTOpenTalkRoom.class);
						}
						intent.putExtra("community_data", data);
						intent.putExtra("authority", OTOApp.getInstance().getAppCode() == app_code);
					}
					OTMainTabOpenTalk.this.startActivity(intent);
				}
			});
			
			layout.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					return false;
				}
			});
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().unRegisterNotifyHandler(this);
		}
	}

	class ListAdapter extends ArrayAdapter<CommunityData>{
		List<CommunityData> objects;
		public ListAdapter(Context context, int textViewResourceId, List<CommunityData> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ListViewHolder holder;
			if(convertView == null){
				LayoutInflater vi = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.ot_conv_list_elem, null);
				holder = new ListViewHolder(convertView);
				convertView.setTag(holder);
			}else{
				holder = (ListViewHolder)convertView.getTag();
			}
			
			final CommunityData comData = objects.get(position);
			holder.data = comData;
			
			final ImageView image = holder.img;
			TextView date = holder.date;
			TextView title = holder.titleName;
			TextView count = holder.cnt;
			
			count.setText("N");
			if(comData.img_url2.length() == 0){
				image.setImageResource(R.drawable.oto_oto_logo);
			}else{
				String realUrl = TASatelite.makeCommonImageUrl(comData.img_url2);
				loadImageOnList(realUrl, image, R.drawable.oto_oto_logo, this, true, false);
			}
			title.setText(comData.title);
			holder.last.setText(comData.description);
			
			if(comData.alarm){
				holder.alarm.setImageResource(R.drawable.oto_alarm_small_on);
				holder.alarm.setVisibility(View.VISIBLE);
			}else{
				holder.alarm.setImageResource(R.drawable.oto_alarm_small_off);
				holder.alarm.setVisibility(View.GONE);
			}
			
			holder.titleImage.setVisibility(View.VISIBLE);
			if(comData.public_opentalk){
				holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_chatrooms);
				holder.rightLayout1.setVisibility(View.GONE);
				holder.rightLayout2.setVisibility(View.VISIBLE);
			}else if(comData.community_group){
				holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_category);
				holder.rightLayout1.setVisibility(View.GONE);
				holder.rightLayout2.setVisibility(View.VISIBLE);
			}else if(comData.need_picture){
				holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_images);
				holder.rightLayout1.setVisibility(View.VISIBLE);
				holder.rightLayout2.setVisibility(View.GONE);
			}else{
				holder.rightLayout1.setVisibility(View.VISIBLE);
				holder.rightLayout2.setVisibility(View.GONE);
				if(comData.write_method_chat){
					holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_chat);
				}else{
					holder.titleImage.setImageResource(R.drawable.oto_opentalk_icon_post);
				}
			}
			
			if(comData.public_opentalk == false || comData.community_group == false){
				date.setVisibility(View.VISIBLE);
				if(comData.last_time == 0L){
					date.setText(getString(R.string.oto_none));
					count.setVisibility(View.INVISIBLE);
				}else{
					date.setText(PLEtcUtilMgr.getDateFormat(comData.last_time));
					long lastSendTime = CommunityLastTimeTable.getInstance().getLastSendTime(comData.id);
					if(lastSendTime == 0L || lastSendTime < comData.last_time){
						count.setVisibility(View.VISIBLE);
					}else{
						count.setVisibility(View.INVISIBLE);
					}
				}
			}
			
			return holder.mainView;
		}
	}
	
	public void loadCommunitys(boolean progress){
		if(progress){
			listView.setVisibility(View.GONE);
			emptyView.setVisibility(View.GONE);
			progressView.setVisibility(View.VISIBLE);
		}
		satelite.doGetCommunityList(OTOApp.getInstance().getToken(), app_code);
	}
	
	void doBodyView(int type){
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
		if(dData.isOK()){
			comDatas.clear();
			comDatas.addAll((ArrayList<CommunityData>) dData.getData());
			comDatas_Original = new ArrayList<CommunityData>();
			comDatas_Original.addAll(comDatas);
			
			if(comDatas.size() == 0){
				listView.setVisibility(View.GONE);
				emptyView.setVisibility(View.VISIBLE);
				progressView.setVisibility(View.GONE);
			}else{
				listView.setVisibility(View.VISIBLE);
				emptyView.setVisibility(View.GONE);
				progressView.setVisibility(View.GONE);
				if(listAdapter == null){
					listView.setAdapter(listAdapter = new ListAdapter(this,R.layout.ot_conv_list_elem, comDatas));
				}
				listAdapter.notifyDataSetChanged();
			}
		}else{
			
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
	public void onHttpException(Exception ex, JSONObject data, String addr) {
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
	}
}

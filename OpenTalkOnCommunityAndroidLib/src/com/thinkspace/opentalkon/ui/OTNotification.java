package com.thinkspace.opentalkon.ui;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TANotification;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper.OnLoadUserImageUrl;
import com.thinkspace.opentalkon.ui.helper.ImageCacheActivity;
import com.thinkspace.pushservice.satelite.PLNotifyHandler;

public class OTNotification extends ImageCacheActivity implements PLNotifyHandler {
	ListView list;
	NotiListAdapter listAdapter;
	Button clearAll;
	TextView empty;
	
	class NotiViewHolder{
		ImageView userImg;
		TextView desc;
		TextView date;
		View newView;
		
		View closeButton;
		View mainView;
		TANotification data;
		
		public ImageView getUserImg() {
			return userImg;
		}
		public TextView getDesc() {
			return desc;
		}
		public TextView getDate() {
			return date;
		}
		public View getCloseButton() {
			return closeButton;
		}
		public View getNewView() {
			return newView;
		}
		public View getMainView() {
			return mainView;
		}
		public TANotification getData() {
			return data;
		}
		public void setData(TANotification data) {
			this.data = data;
		}

		public NotiViewHolder(LayoutInflater li){
			mainView = li.inflate(R.layout.ot_information_elem, null);
			mainView.setTag(this);
			
			userImg = (ImageView) mainView.findViewById(R.id.oto_ot_information_user_img);
			desc = (TextView) mainView.findViewById(R.id.oto_ot_information_desc);
			date = (TextView) mainView.findViewById(R.id.oto_ot_information_time);
			closeButton = mainView.findViewById(R.id.oto_ot_information_close);
			newView = mainView.findViewById(R.id.oto_ot_information_new);
			
			mainView.findViewById(R.id.oto_ot_information_click_layout).setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					OTOApp.getInstance().getCacheCtrl().setCheckNotification(data.getTableIdx(), true);
					Intent intent = new Intent(OTNotification.this, OTOpenTalkDetail.class);
					intent.putExtra("post_id", data.getPost_id());
					if(data.getType() != TANotification.TYPE_LIKE_MINE){
						intent.putExtra("click_reply", true);
					}
					startActivity(intent);
				}
			});
			closeButton.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					OTOApp.getInstance().getCacheCtrl().deleteNotification(data.getTableIdx());
					setupView();
				}
			});
			userImg.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					Intent intent = new Intent(OTNotification.this, OTFriendPopup.class);
					Bundle bundle = new Bundle();
					bundle.putLong("user_id", data.getUser_id());
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
		}
	}
	
	class NotiListAdapter extends ArrayAdapter<TANotification>{
		List<TANotification> objects;
		public NotiListAdapter(Context context, int textViewResourceId, List<TANotification> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			NotiViewHolder holder = null;
			if(convertView == null){
				holder = new NotiViewHolder(getLayoutInflater());
				convertView = holder.getMainView();
			}else{
				holder = (NotiViewHolder) convertView.getTag();
			}
			TANotification nowData = objects.get(position);
			holder.setData(nowData);
			
			final ImageView userImg = holder.getUserImg();
			
			UserImageUrlHelper.loadUserImage(nowData.getUser_id(), new OnLoadUserImageUrl() {
				@Override
				public void onLoad(final long user_id, String url, boolean fromCache) {
					loadImageOnList(url, userImg, R.drawable.oto_friend_img_01, listAdapter, true, false);
				}
			});
			
			switch(nowData.getType()){
			case TANotification.TYPE_LIKE_MINE:
				holder.getDesc().setText(String.format(getString(R.string.oto_like_notification), nowData.getNick_name()));
				break;
			case TANotification.TYPE_REPLY_MINE:
				holder.getDesc().setText(String.format(getString(R.string.oto_reply_notification), nowData.getNick_name()));
				break;
			case TANotification.TYPE_REPLY:
				holder.getDesc().setText(String.format(getString(R.string.oto_reply_notification2), nowData.getNick_name()));
				break;
			}
			
			holder.getDate().setText(PLEtcUtilMgr.getDateFormat(nowData.getDate()));
			
			if(nowData.isCheck()){
				holder.getNewView().setVisibility(View.GONE);
			}else{
				holder.getNewView().setVisibility(View.VISIBLE);
			}
			
			return holder.getMainView();
		}
	}
	
	@Override
	public void onNotify(Notify packet) {
		if(packet.hasLikeMine() || packet.hasCommentMine() || packet.hasNewReply()){
			setupView();
		}
	}

	public void setupView(){
		List<TANotification> notiList = OTOApp.getInstance().getCacheCtrl().getNotificationList();
		if(notiList.size() == 0){
			empty.setVisibility(View.VISIBLE);
			list.setVisibility(View.GONE);
		}else{
			empty.setVisibility(View.GONE);
			list.setVisibility(View.VISIBLE);
			if(listAdapter == null){
				list.setAdapter(listAdapter = new NotiListAdapter(this, R.layout.ot_information_elem, notiList));
			}else{
				listAdapter.notifyDataSetChanged();
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().unRegisterNotifyHandler(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setupView();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_information_layout);
		
		list = (ListView)findViewById(R.id.oto_ot_information_listview);
		empty = (TextView)findViewById(R.id.oto_ot_information_empty);
		clearAll = (Button)findViewById(R.id.oto_oto_information_clear_all);
		
		clearAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OTOApp.getInstance().getCacheCtrl().deleteAllNotification();
				setupView();
			}
		});
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().registerNotifyHandler(this);
		}
	}
}

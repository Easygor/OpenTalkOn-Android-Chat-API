package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroupView;

public class OTAddFriend extends PLActivityGroupView implements TADataHandler {
	EditText idEditText;
	View searchButton;
	TextView caption;
	TASatelite satelite;
	
	ListView friendList;
	ArrayAdapter<TAUserInfo> listAdapter;
	TextView addText;
	TextView tab1;
	TextView tab2;
	int tabState = 0;
	
	void setTab(int state){
		if(state == 0){
			tabState = 0;
			tab1.setTextColor(Color.rgb(0x11, 0x11, 0x11));
			tab2.setTextColor(Color.rgb(0xaa, 0xaa, 0xaa));
			addText.setText(getString(R.string.oto_input_nickname));
			idEditText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		}else{
			tabState = 1;
			tab1.setTextColor(Color.rgb(0xaa, 0xaa, 0xaa));
			tab2.setTextColor(Color.rgb(0x11, 0x11, 0x11));
			addText.setText(getString(R.string.oto_input_id));
			idEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		}
		idEditText.setText("");
		friendList.setVisibility(View.GONE);
		caption.setVisibility(View.VISIBLE);
		caption.setText(getString(R.string.oto_add_friend_alert));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_add_friend);
		
		friendList = (ListView)findViewById(R.id.oto_add_friend_list);
		addText = (TextView) findViewById(R.id.oto_add_friend_search_text);
		tab1 = (TextView) findViewById(R.id.oto_add_friend_tab1);
		tab2 = (TextView) findViewById(R.id.oto_add_friend_tab2);
		idEditText = (EditText) findViewById(R.id.oto_add_friend_edittext);
		searchButton = findViewById(R.id.oto_add_friend_search);
		caption = (TextView) findViewById(R.id.oto_add_friend_caption);
		satelite = new TASatelite(this);
		
		setTab(0);
		OnClickListener tabClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(v.getId() == R.id.oto_add_friend_tab1){
					setTab(0);
				}else{
					setTab(1);
				}
			}
		};
		
		tab1.setOnClickListener(tabClick);
		tab2.setOnClickListener(tabClick);
		
		searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String text = idEditText.getText().toString();
				if(text.length() == 0) return;
				if(tabState == 0){
					satelite.doGetUserInfoByNick(OTOApp.getInstance().getToken(), text);
					OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTAddFriend.this);
				}else{
					try{
						long user_id = Long.valueOf(text);
						satelite.doGetUserInfo(OTOApp.getInstance().getToken(), user_id);
						OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTAddFriend.this);
					}catch(NumberFormatException ex){
						friendList.setVisibility(View.GONE);
						caption.setVisibility(View.VISIBLE);
						caption.setText(getString(R.string.oto_only_number));
					}
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
		
		if(dData.hasLocation(TASatelite.getName(TASatelite.GET_USER_INFO_BY_NICK_URL))){
			if(dData.isOK()){
				ArrayList<TAUserInfo> userInfos = (ArrayList<TAUserInfo>)dData.getData();
				if(userInfos.size() == 1){
					Intent intent = new Intent(this, OTFriendPopup.class);
					intent.putExtra("user_info", userInfos.get(0));
					startActivity(intent);
				}else{
					friendList.setVisibility(View.VISIBLE);
					caption.setVisibility(View.GONE);
					friendList.setAdapter(listAdapter = new UserListAdapter(this, -1, userInfos));
				}
			}else{
				if(dData.getState().equals("user_id is not valid")){
					friendList.setVisibility(View.GONE);
					caption.setVisibility(View.VISIBLE);
					caption.setText(getString(R.string.oto_add_friend_alert2));
				}
			}
		}else if(dData.hasLocation(TASatelite.getName(TASatelite.GET_USER_INFO_URL))){
			if(dData.isOK()){
				TAUserInfo userInfo = (TAUserInfo)dData.getData();
				Intent intent = new Intent(this, OTFriendPopup.class);
				intent.putExtra("user_info", userInfo);
				startActivity(intent);
			}else{
				if(dData.getState().equals("user_id is not valid")){
					friendList.setVisibility(View.GONE);
					caption.setVisibility(View.VISIBLE);
					caption.setText(getString(R.string.oto_add_friend_alert2));
				}
			}
		}
	}
	
	public class FriendListViewHolder{
		View layout1;
		TextView layout1Text;
		
		LinearLayout layout2;
		
		ImageView leftImg;
		public TextView user_id;
		TextView introduce;
		
		ViewGroup checkLayout;
		ImageView check;
		
		View mainView;
		TAUserInfo item;
		
		public ViewGroup getCheckLayout() {
			return checkLayout;
		}
		public void setCheckLayout(ViewGroup checkLayout) {
			this.checkLayout = checkLayout;
		}
		public ImageView getCheck() {
			return check;
		}
		public void setCheck(ImageView check) {
			this.check = check;
		}
		public TextView getLayout1Text() {
			return layout1Text;
		}
		public void setLayout1Text(TextView layout1Text) {
			this.layout1Text = layout1Text;
		}
		public TextView getIntroduce() {
			return introduce;
		}
		public void setIntroduce(TextView introduce) {
			this.introduce = introduce;
		}
		public View getLayout1() {
			return layout1;
		}
		public void setLayout1(View layout1) {
			this.layout1 = layout1;
		}
		public LinearLayout getLayout2() {
			return layout2;
		}
		public void setLayout2(LinearLayout layout2) {
			this.layout2 = layout2;
		}
		public View getMainView() {
			return mainView;
		}
		public void setMainView(View mainView) {
			this.mainView = mainView;
		}
		public TAUserInfo getItem() {
			return item;
		}
		public void setItem(TAUserInfo item) {
			this.item = item;
		}
		public ImageView getLeftImg() {
			return leftImg;
		}
		public void setLeftImg(ImageView leftImg) {
			this.leftImg = leftImg;
		}
		
		public FriendListViewHolder(int res_id, LayoutInflater li){
			mainView = li.inflate(res_id, null);
			mainView.setTag(this);
			
			layout1 = (View)mainView.findViewById(R.id.oto_friend_list_elem_layout_1);
			layout2 = (LinearLayout)mainView.findViewById(R.id.oto_friend_list_elem_layout_2);
			layout1Text = (TextView)mainView.findViewById(R.id.oto_friend_list_elem_layout_1_text);
			
			leftImg = (ImageView)mainView.findViewById(R.id.oto_friend_list_elem_left_img);
			user_id = (TextView)mainView.findViewById(R.id.oto_friend_list_elem_id);
			
			checkLayout = (ViewGroup)mainView.findViewById(R.id.oto_friend_list_elem_check_layout);
			check = (ImageView)mainView.findViewById(R.id.oto_friend_list_elem_check);
			
			introduce = (TextView) mainView.findViewById(R.id.oto_friend_list_elem_locale);
			introduce.setMaxLines(2);
			
			layout2.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					Intent intent = new Intent(OTAddFriend.this, OTFriendPopup.class);
					intent.putExtra("user_info", item);
					startActivity(intent);
				}
			});
		}
	}
	
	public class UserListAdapter extends ArrayAdapter<TAUserInfo>{
		List<TAUserInfo> items;
		public UserListAdapter(Context context, int textViewResourceId, List<TAUserInfo> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects; 
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FriendListViewHolder holder = null;
			if(convertView == null){
				holder = new FriendListViewHolder(R.layout.ot_elem_friend, getLayoutInflater());
				convertView = holder.getMainView();
			}else{
				holder = (FriendListViewHolder)convertView.getTag();
			}
			
			TAUserInfo item = items.get(position);
			holder.setItem(item);
			
			View layout1 = holder.getLayout1();
			LinearLayout layout2 = holder.getLayout2();
			
			final ImageView userImg = holder.getLeftImg();
			TextView user_id = holder.user_id;
			TextView introduce = holder.getIntroduce();
			
			holder.getCheckLayout().setVisibility(View.GONE);
			
			layout1.setVisibility(View.GONE);
			layout2.setVisibility(View.VISIBLE);
			introduce.setVisibility(View.VISIBLE);
			
			if(item.getImagePath().length() != 0){
				String url = TASatelite.makeImageUrl(item.getImagePath());
				loadImageOnList(url, userImg, R.drawable.oto_friend_img_01, listAdapter, true, false);
			}else{
				userImg.setImageResource(R.drawable.oto_friend_img_01);
			}
			
			user_id.setText(item.getNickName());
			
			String intro = item.getIntroduce();
			if(intro == null || intro.length() == 0){
				introduce.setVisibility(View.GONE);
			}else{
				introduce.setText(intro);
			}
			
			return holder.getMainView();
		}
	}

	@Override
	public void onTokenIsNotValid(JSONObject data) {
		finish();
	}

	@Override
	public void onLimitMaxUser(JSONObject data) {
		finish();
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
}

package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.helper.UserElemClickListener;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroupView;

public abstract class OTFriendListBase extends PLActivityGroupView implements TADataHandler, UserElemClickListener {
	public final static int OT_CHECK_IF_RESUME = 100;
	TASatelite satelite;
	ListView listView;
	TextView emptyView;
	ViewGroup progView;
	FriendListAdapter listAdapter;
	
	Map<FriendListElem, FriendListViewHolder> itemToViewHolder= new HashMap<OTFriendListBase.FriendListElem, OTFriendListBase.FriendListViewHolder>();
	ArrayList<FriendListElem> friendListElems = new ArrayList<FriendListElem>();
	ArrayList<FriendListElem> friendListElems_Original = new ArrayList<FriendListElem>();
	
	ViewGroup searchLayout;
	EditText searchEdit;
	View searchEditDelete;
	
	SearchHandler searchHandler;
	
	public static interface SearchHandler{
		public void onSearch(String value);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_sub_tab_friend);
		
		listView = (ListView) findViewById(R.id.oto_friend_list_list);
		emptyView = (TextView) findViewById(R.id.oto_friend_list_empty);
		progView =  (ViewGroup) findViewById(R.id.oto_friend_list_prog);
		
		searchLayout = (ViewGroup)findViewById(R.id.oto_subtab_list_search);
		searchLayout.setVisibility(View.GONE);
		emptyView.setText(getEmptylistString());
		
		satelite = new TASatelite(this);
		setProg(true);
		doRequestList();
	}
	
	protected void setSearchLayout(SearchHandler handler){
		searchLayout.setVisibility(View.VISIBLE);
		this.searchHandler = handler;
		
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
				searchHandler.onSearch(searchEdit.getText().toString());
			}
		});
		
		searchEdit.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					if(searchHandler != null){
						searchHandler.onSearch(searchEdit.getText().toString());
					}
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

	public abstract void doRequestList();
	public abstract void dispatchResponse(DispatchedData dispatchData);
	public abstract String getEmptylistString();
	
	public void listDataReceiveDone(){
		setProg(false);
		if(friendListElems.size() == 0){
			setEmpty();
		}else{
			if(listAdapter == null){
				listView.setAdapter(listAdapter = new FriendListAdapter(this, R.layout.ot_elem_friend, friendListElems));
			}
			listAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		DispatchedData dispatchData = TASateliteDispatcher.dispatchSateliteData(data);
		dispatchResponse(dispatchData);
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_network_fail), OTOApp.getInstance().getContext());
		onFail();
	}
	
	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_network_fail), OTOApp.getInstance().getContext());
		onFail();
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

	public void onFail(){
		listView.setVisibility(View.GONE);
		emptyView.setVisibility(View.VISIBLE);
		progView.setVisibility(View.GONE);
		
		emptyView.setText(getString(R.string.oto_network_fail));
	}
	
	public void setProg(boolean enable){
		if(enable){
			listView.setVisibility(View.GONE);
			emptyView.setVisibility(View.GONE);
			progView.setVisibility(View.VISIBLE);	
		}else{
			listView.setVisibility(View.VISIBLE);
			emptyView.setVisibility(View.GONE);
			progView.setVisibility(View.GONE);
		}
	}
	
	public void setEmpty(){
		listView.setVisibility(View.GONE);
		emptyView.setVisibility(View.VISIBLE);
		progView.setVisibility(View.GONE);
	}
	
	public static class FriendListElem implements Comparable<FriendListElem>{
		enum LabelType{
			TYPE_MYPROFILE,
			TYPE_BEST_FRIEND,
			TYPE_FRIEND,
			TYPE_REVERSE_FRIEND
		}
		boolean isDivider;
		LabelType friendLabelType;
		String divider_text;
		
		TAUserInfo info;
		
		@Override
		public int compareTo(FriendListElem another) {
			return compare(this, another);
		}
		
		int getSortPoint(FriendListElem elem){
			int point;
			if(elem.isDivider && elem.friendLabelType == LabelType.TYPE_MYPROFILE){
				point = 0;
			}else if(elem.info != null && elem.info.getId() == OTOApp.getInstance().getId()){
				point = 1;
			}else if(elem.isDivider && elem.friendLabelType == LabelType.TYPE_BEST_FRIEND){
				point = 2;
			}else if(elem.info != null && elem.info.isFriend_best()){
				point = 3;
			}else if(elem.isDivider && elem.friendLabelType == LabelType.TYPE_FRIEND){
				point = 4;
			}else if(elem.info != null && elem.info.is_friend()){
				point = 5;
			}else if(elem.isDivider && elem.friendLabelType == LabelType.TYPE_REVERSE_FRIEND){
				point = 6;
			}else{
				point = 7;
			}
			return point;
		}

		int compare(FriendListElem lhs, FriendListElem rhs) {
			int leftSortPoint = getSortPoint(lhs);
			int rightSortPoint = getSortPoint(rhs);
			
			if(leftSortPoint != rightSortPoint){
				return leftSortPoint < rightSortPoint?-1:1;
			}else{
				int point = leftSortPoint;
				if(point == 3 || point == 5 || point == 7){
					return lhs.info.compareTo(rhs.info);
				}else{
					return -1;
				}
			}
		}
		
		int compares(FriendListElem lhs, FriendListElem rhs) {			
			if(lhs.isDivider && rhs.isDivider){
				return (lhs.friendLabelType.ordinal() < rhs.friendLabelType.ordinal())?-1:1;
			}
			
			if(lhs.isDivider && lhs.friendLabelType == LabelType.TYPE_MYPROFILE) return -1;
			if(rhs.isDivider && rhs.friendLabelType == LabelType.TYPE_MYPROFILE) return 1;
			if(lhs.info != null){
				if(lhs.info.getId() == OTOApp.getInstance().getId()) return -1;
			}
			if(rhs.info != null){
				if(rhs.info.getId() == OTOApp.getInstance().getId()) return 1;
			}
			
			if(lhs.isDivider && lhs.friendLabelType == LabelType.TYPE_BEST_FRIEND) return -1;
			if(rhs.isDivider && rhs.friendLabelType == LabelType.TYPE_BEST_FRIEND) return 1;
			
			if(lhs.isDivider){
				if(rhs.info.isFriend_best() == false) return -1;
				else return 1;
			}
			
			if(rhs.isDivider){
				if(lhs.info.isFriend_best() == false) return 1;
				else return -1;
			}
			
			return lhs.info.compareTo(rhs.info);
		}
		
		public LabelType getFriendLabelType() {
			return friendLabelType;
		}

		public void setFriendLabelType(LabelType friendLabelType) {
			this.friendLabelType = friendLabelType;
		}

		public boolean isDivider() {
			return isDivider;
		}
		public void setDivider(boolean isDivider) {
			this.isDivider = isDivider;
		}
		public String getDivider_text() {
			return divider_text;
		}
		public void setDivider_text(String divider_text) {
			this.divider_text = divider_text;
		}
		public TAUserInfo getInfo() {
			return info;
		}
		public void setInfo(TAUserInfo info) {
			this.info = info;
		}
	}
	
	public static class FriendListViewHolder{
		View layout1;
		TextView layout1Text;
		
		LinearLayout layout2;
		
		ImageView leftImg;
		public TextView user_id;
		TextView introduce;
		
		ViewGroup checkLayout;
		ImageView check;
		
		View mainView;
		FriendListElem item;
		UserElemClickListener listener;
		
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
		public FriendListElem getItem() {
			return item;
		}
		public void setItem(FriendListElem item) {
			this.item = item;
		}
		public ImageView getLeftImg() {
			return leftImg;
		}
		public void setLeftImg(ImageView leftImg) {
			this.leftImg = leftImg;
		}
		
		public FriendListViewHolder(int res_id, LayoutInflater li, UserElemClickListener listener){
			mainView = li.inflate(res_id, null);
			mainView.setTag(this);
			this.listener = listener;
			
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
					FriendListViewHolder.this.listener.elemClicked(item.getInfo());
				}
			});
			layout2.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					FriendListViewHolder.this.listener.elemLongClicked(item.getInfo());
					return false;
				}
			});
		}
	}
	
	public class FriendListAdapter extends ArrayAdapter<FriendListElem>{
		List<FriendListElem> items;
		public FriendListAdapter(Context context, int textViewResourceId, List<FriendListElem> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects; 
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FriendListViewHolder holder = null;
			if(convertView == null){
				holder = new FriendListViewHolder(R.layout.ot_elem_friend, getLayoutInflater(), OTFriendListBase.this);
				convertView = holder.getMainView();
			}else{
				holder = (FriendListViewHolder)convertView.getTag();
			}
			
			FriendListElem item = items.get(position);
			holder.setItem(item);
			itemToViewHolder.put(item, holder);
			
			View layout1 = holder.getLayout1();
			TextView layout1Text = holder.getLayout1Text();
			LinearLayout layout2 = holder.getLayout2();
			
			final ImageView userImg = holder.getLeftImg();
			TextView user_id = holder.user_id;
			TextView introduce = holder.getIntroduce();
			
			holder.getCheckLayout().setVisibility(View.GONE);
			
			if(item.isDivider()){
				layout1.setVisibility(View.VISIBLE);
				layout1Text.setText(item.getDivider_text());
				layout2.setVisibility(View.GONE);
				introduce.setVisibility(View.GONE);
			}else{
				layout1.setVisibility(View.GONE);
				layout2.setVisibility(View.VISIBLE);
				introduce.setVisibility(View.VISIBLE);
				
				if(item.getInfo().getImagePath().length() != 0){
					String url = TASatelite.makeImageUrl(item.getInfo().getImagePath());
					loadImageOnList(url, userImg, R.drawable.oto_friend_img_01, listAdapter, true, false);
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

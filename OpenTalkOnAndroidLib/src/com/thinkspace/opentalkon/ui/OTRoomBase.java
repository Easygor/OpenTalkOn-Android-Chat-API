package com.thinkspace.opentalkon.ui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.ClipboardManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.common.util.PLDialogListener;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTMsgBase;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.helper.OTComConvHandler;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper.OnLoadUserImageUrl;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TAImgDownloader;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.ui.OTImageLoadBase.ImageLoadHandler;
import com.thinkspace.opentalkon.ui.helper.PLKeyBoardView;
import com.thinkspace.opentalkon.ui.helper.PLOnKeyBoardUpListener;
import com.thinkspace.pushservice.satelite.PLMsgHandler;
import com.thinkspace.pushservice.satelite.PLNotifyHandler;

public abstract class OTRoomBase extends OTImageLoadBase implements TADataHandler, PLMsgHandler, PLOnKeyBoardUpListener, PLNotifyHandler, PLDialogListener, ImageLoadHandler{
	public final static int DIALOG_RESEND = 0;
	
	TASatelite satelite;
	TAUserNick nickTable;
	Queue<Runnable> lazyWorkQueue = new LinkedBlockingQueue<Runnable>();
	
	ImageView leftTopView;
	TextView titleView;
	TextView adminOnlyView;
	Button sendBtnView;
	EditText sendBodyView;
	Button optionBtnView;
	ListView convListView;
	TextView convEmptyView;
	ViewGroup imageBarView;
	View imageBarLayout;
	ViewGroup contextMenuLayout;
	
	ViewGroup sendLayout;
	
	ViewGroup searchLayout;
	EditText searchBody;
	Button searchCondition;
	View searchClose;
	View searchButton;
	
	ViewGroup mySearchLayout;
	View mySearchClose;
	Button mySearchWordBtn;
	Button mySearchReplyBtn;
	
	ViewGroup bestLayout;
	View bestClose;
	Button bestWeekBtn;
	Button bestMonthBtn;
	
	View progressView;
	ListView searchListView;
	GridView gridView;
	
	ViewGroup convListViewLayout;
	ViewGroup convlistAppendViewLayout;
	ImageView convListAppendViewImage;
	TextView convListAppendViewText;
	Map<Long, Bitmap> iconMap = new HashMap<Long,Bitmap>();
	Map<String, Bitmap> imageMap = new HashMap<String, Bitmap>();
	boolean contextMenuSw = false;
	boolean finishGuard;
	
	protected MsgListAdapater msgListAdapter;
	protected List<ListElem> elemLists = new LinkedList<OTRoomBase.ListElem>();
	OTComConvHandler comConvHandler;
	
	protected Handler handler = new Handler();
	
	public OTComConvHandler getComConvHandler() { return comConvHandler; }
	public void setComConvHandler(OTComConvHandler comConvHandler) { this.comConvHandler = comConvHandler; }
	
	public abstract boolean OnSendMsg(String msg);
	public abstract boolean OnSendImgMsg(List<String> imagePaths, String msg);
	public abstract int OnLoadNextMsg();
	public abstract void OnReSendButtonPressed(long transact_id);
	public abstract void OnDelMsgButtonPressed(long transact_id);
	public abstract void OnUserPressed(long user_id);
	public abstract void OnDeleteMsg(OTMsgBase msg);
	public abstract void setupListElem(OTMsgBase msg, ListElem elem);
	public abstract void setupConvView(ListElem elem, MsgHolder holder, ArrayAdapter<?> adapter);
	public abstract boolean parseIntentExtra();
	public abstract void onListViewScrollBottom();
	
	public void OnLikeClick(OTMsgBase msg){}
	public void OnCommentClick(OTMsgBase msg){}
	public void OnBodyClick(OTMsgBase msg){}
	
	public OTRoomBase() {
		setImageLoadHandler(this);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String msg = savedInstanceState.getString("msg");
		sendBodyView.setText(msg);
		ArrayList<String> imagePaths = savedInstanceState.getStringArrayList("imagePaths");
		for(String imagePath : imagePaths){
			OnImageLoadComplete(imagePath);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		ArrayList<String> imagePaths = new ArrayList<String>();
		for(int i=0;i<imageBarView.getChildCount();++i){
			String imgPath = (String) imageBarView.getChildAt(i).getTag();
			imagePaths.add(imgPath);
		}
		outState.putStringArrayList("imagePaths", imagePaths);
		outState.putString("msg", sendBodyView.getText().toString());
	}
	
	public void OnOptionButtonPressed() {
		//contextMenuLayout
		if(contextMenuSw == false){
			boolean result = OnMakeOptionButton(contextMenuLayout);
			if(result){
				switchOption(true);
			}
		}else{
			switchOption(false);
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU){
			if(event.getAction() == KeyEvent.ACTION_UP){
				OnOptionButtonPressed();
				return false;
			}
		}
		return super.onKeyUp(keyCode, event);
	}
	
	void switchOption(boolean enable){
		if(enable){
			contextMenuSw = true;
			contextMenuLayout.setVisibility(View.VISIBLE);
			Animation anim = (Animation)AnimationUtils.loadAnimation(this, R.anim.com_thinkspace_slide_in_from_top);
			contextMenuLayout.setAnimation(anim);
		}else{
			contextMenuSw = false;
			contextMenuLayout.setVisibility(View.GONE);
		}
	}
	
	public abstract boolean OnMakeOptionButton(ViewGroup contextMenuLayout);

	public void applyNoticeLayout(){
		int visible = sendLayout.getVisibility();
		sendLayout.setVisibility(View.GONE);
		sendLayout = (ViewGroup)findViewById(R.id.oto_conv_detail_send_layout_notice);
		sendLayout.setVisibility(visible);
		
		findViewById(R.id.oto_conv_detail_option_btn_notice).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				OnOptionButtonPressed();
			}
		});
	}
	
	public void setVisibilitySendNoticeButton(int visibility){
		findViewById(R.id.oto_conv_detail_send_btn_notice).setVisibility(visibility);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(OTOApp.getInstance().getPref().getNickName().getValue().length() == 0){
			startActivity(new Intent(this, OTSettingMyInfo.class));
			finishGuard = true;
			finish();
			return;
		}
		
		PLKeyBoardView view = new PLKeyBoardView(this, this, R.layout.ot_conv_inside_room);
		setContentView(view);
		
		gridView = (GridView)findViewById(R.id.oto_conv_detail_body_grid);
		leftTopView = (ImageView)findViewById(R.id.oto_conv_room_left_top_img);
		sendBtnView = (Button)findViewById(R.id.oto_conv_detail_send_btn);
		sendBodyView = (EditText)findViewById(R.id.oto_conv_detail_body_edit);
		titleView = (TextView)findViewById(R.id.oto_base_tab_title);
		adminOnlyView = (TextView)findViewById(R.id.oto_conv_detail_admin_only_title);
		optionBtnView = (Button)findViewById(R.id.oto_conv_detail_option_btn);
		convListView = (ListView)findViewById(R.id.oto_conv_detail_body_list);
		imageBarView = (ViewGroup)findViewById(R.id.oto_conv_detail_image_bar);
		imageBarLayout = findViewById(R.id.oto_conv_detail_image_bar_layout);
		contextMenuLayout = (ViewGroup)findViewById(R.id.oto_conv_detail_context_menu);
		sendLayout = (ViewGroup)findViewById(R.id.oto_conv_detail_send_layout);
		searchLayout = (ViewGroup)findViewById(R.id.oto_conv_detail_search_layout);
		searchBody = (EditText)findViewById(R.id.oto_conv_detail_search_body);
		searchCondition = (Button)findViewById(R.id.oto_conv_detail_search_condition);
		searchClose = findViewById(R.id.oto_conv_detail_search_close);
		searchButton = findViewById(R.id.oto_conv_detail_search_button);
		progressView = findViewById(R.id.oto_conv_detail_progress);
		searchListView = (ListView)findViewById(R.id.oto_conv_detail_body_list2);
		convEmptyView = (TextView)findViewById(R.id.oto_conv_detail_empty);
		mySearchLayout = (ViewGroup)findViewById(R.id.oto_conv_detail_my_search_layout);
		mySearchClose = findViewById(R.id.oto_conv_detail_my_search_close);
		mySearchWordBtn = (Button)findViewById(R.id.oto_conv_detail_my_word_btn);
		mySearchReplyBtn = (Button)findViewById(R.id.oto_conv_detail_my_reply_btn);
		convListViewLayout = (ViewGroup) findViewById(R.id.oto_conv_detail_body_list_layout);
		convlistAppendViewLayout = (ViewGroup) findViewById(R.id.oto_conv_detail_body_list_append_layout);
		convListAppendViewImage = (ImageView) findViewById(R.id.oto_conv_detail_body_list_append_layout_img);
		convListAppendViewText = (TextView) findViewById(R.id.oto_conv_detail_body_list_append_layout_text);
		bestLayout = (ViewGroup)findViewById(R.id.oto_conv_detail_best_layout);
		bestClose = findViewById(R.id.oto_conv_detail_best_close);
		bestWeekBtn = (Button)findViewById(R.id.oto_conv_detail_week_best_btn);
		bestMonthBtn = (Button)findViewById(R.id.oto_conv_detail_month_best_btn);
		satelite = new TASatelite(this, true);
		
		if(parseIntentExtra() == false){
			finish();
			return;
		}
		setupHandler();
		contextMenuLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switchOption(false);
			}
		});
		
		convlistAppendViewLayout.setClickable(true);
		convlistAppendViewLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				convListScrollToBottom();
			}
		});
		
		onSetupMainAdapter();
		
		Intent intent = getIntent();
		boolean joinWithNotification = intent.getBooleanExtra("notificationJoin", false);
		if(joinWithNotification){
			OTOApp.getInstance().getConvMgr().setJoinWithNotification();
		}
		
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().registerMsgHandler(this);
			OTOApp.getInstance().getPushClient().registerNotifyHandler(this);
		}	
	}
	
	class ConvScrollListener implements OnScrollListener{
		boolean viewLast;
		public ConvScrollListener(boolean viewLast){
			this.viewLast = viewLast;
		}
		@Override
		public void onScrollStateChanged(AbsListView view, int state) {
			if(viewLast == false){
				if(state == OnScrollListener.SCROLL_STATE_IDLE){
					if(view.getFirstVisiblePosition() == 0 && view.getCount() != 0){
						int scrollPos = OnLoadNextMsg(); 
						if(scrollPos > 0){
							convListView.setSelection(scrollPos);
						}
					}
				}
			}
		}
		@Override public void onScroll(AbsListView lw, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
			int lastItem = firstVisibleItem + visibleItemCount;
            if(lastItem == totalItemCount) {
            	if(viewLast){
    				int scrollPos = OnLoadNextMsg(); 
    				if(scrollPos > 0){
    					convListView.setSelection(scrollPos);
    				}
    			}else{
	            	convlistAppendViewLayout.setVisibility(View.GONE);
	            	onListViewScrollBottom();
    			}
            }
		}
	}
	
	protected void onSetupMainAdapter(){
		convListView.setOnScrollListener(new ConvScrollListener(false));
		msgListAdapter = new MsgListAdapater(this, -1, elemLists);
		convListView.setAdapter(msgListAdapter);
		convListView.setSelector(android.R.color.transparent);
	}
	
	@Override
	protected void onDestroy() {
		if(OTOApp.getInstance().getPushClient() != null){
			OTOApp.getInstance().getPushClient().unRegisterMsgHandler(this);
			OTOApp.getInstance().getPushClient().unRegisterNotifyHandler(this);
		}
		//PLEtcUtilMgr.deleteAllFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +BASE_SEND_IMG_PATH);
		super.onDestroy();
	}
	
	protected boolean hasOwnBackPressedProcess(){
		return false;
	}

	@Override
	public void onBackPressed() {
		if(contextMenuSw){
			switchOption(false);
			return;
		}
		
		if(hasOwnBackPressedProcess()){
			return;
		}else{
			super.onBackPressed();
			if(OTOApp.getInstance().getMainActivityCount() == 0){
				Intent acintent = new Intent(this, OTMain.class);
				startActivity(acintent);
			}
		}
	}
	
	public void onMsgListNotifyDataSetChanged(){
		msgListAdapter.notifyDataSetChanged();
	}
	
	public void removeListElem(long msg_id){
		OTMsgBase msg = null;
		for(int i=0;i<elemLists.size();++i){
			ListElem elem = elemLists.get(i);
			if(elem.elemType == ListElemType.MSG){
				if(elem.msg.getId() == msg_id){
					msg = elem.msg;
					break;
				}
			}
		}
		removeListElem(msg);
	}
	
	public void removeListElem(OTMsgBase msg){
		OnDeleteMsg(msg);
		for(int i=0;i<elemLists.size();++i){
			ListElem elem = elemLists.get(i);
			if(elem.elemType == ListElemType.MSG){
				if(elem.msg == msg){
					elemLists.remove(i);
					break;
				}
			}
		}
		onMsgListNotifyDataSetChanged();
	}
	
	public static interface OnAddListElem{
		public void onDone(OTMsgBase msg);
	}
	
	public void setupHandler(){
		sendBtnView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String body = sendBodyView.getEditableText().toString();
				if(imageBarView.getChildCount() == 0){
					if(body.length() == 0) return;
					if(OnSendMsg(body)){
						sendBodyView.setText("");
					}
				}else{
					List<String> imagePaths = new ArrayList<String>();
					for(int i=0;i<imageBarView.getChildCount();++i){
						View view = imageBarView.getChildAt(i);
						String imagePath = (String)view.getTag();
						imagePaths.add(imagePath);
					}
					imageBarView.removeAllViews();
					imageBarLayout.setVisibility(View.GONE);
					if(OnSendImgMsg(imagePaths, body)){
						sendBodyView.setText("");
					}
				}
			}
		});
		optionBtnView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				OnOptionButtonPressed();
			}
		});
	}
	
	public void setupConvListAppendLayout(long user_id, String msg){
		convlistAppendViewLayout.setVisibility(View.VISIBLE);
		UserImageUrlHelper.loadUserImage(user_id, new OnLoadUserImageUrl() {
			@Override
			public void onLoad(long user_id, String url, boolean fromCache) {
				loadImageOnList(url, convListAppendViewImage, R.drawable.oto_friend_img_01, msgListAdapter, true, false);
			}
		});
		convListAppendViewText.setText(msg);
	}
	
	class MsgHolder{
		public final static int STATUS_SENDING = 0;
		public final static int STATUS_NOT_READ = 1;
		public final static int STATUS_READ = 2;
		
		public final static int IMG_UPLOADING = 0;
		public final static int IMG_DOWNLOADING = 1;
		public final static int IMG_COMPLETE = 2;
		LinearLayout canvas;
		View mainView;
		Button reSend;
		TextView user_name;
		ListElem listElem;
		ImageView user_img;
		
		ConvDirectionResource nowConvResource;
		ConvDirectionResource leftConvResource;
		ConvDirectionResource rightConvResource;
		
		ViewGroup leftView;
		ViewGroup rightView;
		TextView infoView;
		
		public ConvDirectionResource getNowConvResource() {
			return nowConvResource;
		}

		public class ConvDirectionResource{
			public TextView body;
			public RelativeLayout statusLayout;
			public TextView time;
			public TextView status;
			public ImageView bodyImg;
			public ImageView bodyImg2;
			public TextView bodyImgDetail;
			public ProgressBar bodyProg;
			public View msgBody;
			public ViewGroup msgBodyParent;
			public ViewGroup bottomLayout;
			public View bottomButton1;
			public View bottomButton2;
			public TextView likeCnt;
			public TextView commentCnt;
		}
		
		public void setInfoMsg(String msg){
			infoView.setText(msg);
			leftView.setVisibility(View.GONE);
			rightView.setVisibility(View.GONE);
			infoView.setVisibility(View.VISIBLE);
		}

		public void setListElem(ListElem listElem) {
			this.listElem = listElem;
			if(listElem.elemType == ListElemType.MSG){
				if(listElem.msg.isSendMsg()){
					nowConvResource = rightConvResource;
					leftView.setVisibility(View.GONE);
					rightView.setVisibility(View.VISIBLE);
					infoView.setVisibility(View.GONE);
				}else{
					nowConvResource = leftConvResource;
					leftView.setVisibility(View.VISIBLE);
					rightView.setVisibility(View.GONE);
					infoView.setVisibility(View.GONE);
				}
				
				if(listElem.msg.isImgMsg()){
					setAsImgMsg();
				}else{
					setAsDefaultMsg();
				}	
			}else if(listElem.elemType == ListElemType.MSG_DATE){
				infoView.setText(listElem.msgDate);
				leftView.setVisibility(View.GONE);
				rightView.setVisibility(View.GONE);
				infoView.setVisibility(View.VISIBLE);	
			}else if(listElem.elemType == ListElemType.INFO){
				infoView.setText(listElem.infoString);
				leftView.setVisibility(View.GONE);
				rightView.setVisibility(View.GONE);
				infoView.setVisibility(View.VISIBLE);
			}
		}

		public MsgHolder(View mainView){
			this.mainView = mainView;
			leftView = (ViewGroup) mainView.findViewById(R.id.oto_conv_left_view);
			rightView = (ViewGroup) mainView.findViewById(R.id.oto_conv_right_view);
			infoView = (TextView)mainView.findViewById(R.id.oto_conv_information_view);
			user_img = (ImageView)mainView.findViewById(R.id.oto_conv_detail_elem_user_img);
			
			leftConvResource = new ConvDirectionResource();
			rightConvResource = new ConvDirectionResource();
			
			rightConvResource.body = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_body_r);
			rightConvResource.statusLayout = (RelativeLayout)mainView.findViewById(R.id.oto_conv_detail_elem_time_status_layout_r);
			rightConvResource.time = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_time_r);
			rightConvResource.status = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_status_r);
			rightConvResource.bodyImg = (ImageView)mainView.findViewById(R.id.oto_conv_detail_elem_body_img_r);
			rightConvResource.bodyImg2 = (ImageView)mainView.findViewById(R.id.oto_conv_detail_elem_body_img_r_2);
			rightConvResource.bodyImgDetail = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_body_img_detail_r);
			rightConvResource.bodyProg = (ProgressBar)mainView.findViewById(R.id.oto_conv_detail_elem_body_prog_r);
			rightConvResource.msgBody = mainView.findViewById(R.id.oto_conv_detail_elem_msg_r);
			rightConvResource.msgBodyParent = (ViewGroup) mainView.findViewById(R.id.oto_conv_detail_elem_msg_parent_r);
			rightConvResource.bottomLayout = (ViewGroup)mainView.findViewById(R.id.oto_conv_detail_bottom_layout_r);
			rightConvResource.bottomButton1 = mainView.findViewById(R.id.oto_conv_detail_bottom_button1_r);
			rightConvResource.bottomButton2 = mainView.findViewById(R.id.oto_conv_detail_bottom_button2_r);
			rightConvResource.likeCnt = (TextView)mainView.findViewById(R.id.oto_conv_detail_like_cnt_r);
			rightConvResource.commentCnt = (TextView)mainView.findViewById(R.id.oto_conv_detail_comment_cnt_r);
			
			leftConvResource.body = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_body);
			leftConvResource.statusLayout = (RelativeLayout)mainView.findViewById(R.id.oto_conv_detail_elem_time_status_layout);
			leftConvResource.time = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_time);
			leftConvResource.status = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_status);
			leftConvResource.bodyImg = (ImageView)mainView.findViewById(R.id.oto_conv_detail_elem_body_img);
			leftConvResource.bodyImg2 = (ImageView)mainView.findViewById(R.id.oto_conv_detail_elem_body_img_2);
			leftConvResource.bodyImgDetail = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_body_img_detail);
			leftConvResource.bodyProg = (ProgressBar)mainView.findViewById(R.id.oto_conv_detail_elem_body_prog);
			leftConvResource.msgBody = mainView.findViewById(R.id.oto_conv_detail_elem_msg);
			leftConvResource.msgBodyParent = (ViewGroup) mainView.findViewById(R.id.oto_conv_detail_elem_msg_parent);
			leftConvResource.bottomLayout = (ViewGroup)mainView.findViewById(R.id.oto_conv_detail_bottom_layout);
			leftConvResource.bottomButton1 = mainView.findViewById(R.id.oto_conv_detail_bottom_button1);
			leftConvResource.bottomButton2 = mainView.findViewById(R.id.oto_conv_detail_bottom_button2);
			leftConvResource.likeCnt = (TextView)mainView.findViewById(R.id.oto_conv_detail_like_cnt);
			leftConvResource.commentCnt = (TextView)mainView.findViewById(R.id.oto_conv_detail_comment_cnt);
				
			reSend = (Button)mainView.findViewById(R.id.oto_conv_detail_elem_time_re_send_btn_r);
			user_name = (TextView)mainView.findViewById(R.id.oto_conv_detail_elem_user_name);
			
			reSend.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					OTOApp.getInstance().getDialogMaker().makeReSendDialog(getString(R.string.oto_message_resend),
							getString(R.string.oto_message_resend_popup), OTRoomBase.this, OTRoomBase.this, DIALOG_RESEND, (long)MsgHolder.this.listElem.msg.getTableIdx());
				}
			});
			View [] msgBodys = new View[4];
			msgBodys[0] = leftConvResource.msgBody;
			msgBodys[1] = rightConvResource.msgBody;
			msgBodys[2] = leftConvResource.body;
			msgBodys[3] = rightConvResource.body;
			for(int i=0;i<msgBodys.length;++i){
				msgBodys[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						OnBodyClick(listElem.msg);
					}
				});
				msgBodys[i].setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View arg0) {
						onLongClickMsgBody();
						return true;
					}
				});
			}
			
			View [] likeButton = new View[2];
			likeButton[0] = leftConvResource.bottomButton1;
			likeButton[1] = rightConvResource.bottomButton1;
			for(int i=0;i<likeButton.length;++i){
				likeButton[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						OnLikeClick(listElem.msg);
					}
				});
			}
			
			View [] commentButton = new View[2];
			commentButton[0] = leftConvResource.bottomButton2;
			commentButton[1] = rightConvResource.bottomButton2;
			for(int i=0;i<commentButton.length;++i){
				commentButton[i].setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						OnCommentClick(listElem.msg);
					}
				});
			}
			
			ImageView [] bodyImages =new ImageView [4];
			bodyImages[0] = leftConvResource.bodyImg;
			bodyImages[1] = rightConvResource.bodyImg;
			bodyImages[2] = leftConvResource.bodyImg2;
			bodyImages[3] = rightConvResource.bodyImg2;
			for(int i=0;i<bodyImages.length;++i){
				bodyImages[i].setOnClickListener(new OnClickListener() {
					@Override public void onClick(View v) {
						if(MsgHolder.this.listElem == null) return;
						if(MsgHolder.this.listElem.msg == null) return;
						if(MsgHolder.this.listElem.msg.getImg_url() == null) return;
						
						Intent intent = new Intent(OTRoomBase.this, OTGallerySelect.class);
						intent.putExtra("isWeb", true);
						intent.putExtra("isImageViewer", true);
						ArrayList<String> arr = new ArrayList<String>();
						for(int i=0;i<MsgHolder.this.listElem.msg.getImg_url().length();++i){
							try {
								arr.add(MsgHolder.this.listElem.msg.getImg_url().getString(i));
							} catch (JSONException e) {}
						}
						intent.putExtra("imageList", arr);
						intent.putExtra("showFolderName", getString(R.string.oto_image_viewer_title));
						
						OTRoomBase.this.startActivity(intent);
					}
				});
			}
			
			if(user_img != null){
				user_img.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						OnUserPressed(listElem.msg.getSender_id());
					}
				});
			}
		}
		
		public void onLongClickMsgBody(){
			if(listElem.msg.isImgMsg()) return;
			AlertDialog.Builder ab = new Builder(OTRoomBase.this);
			ab.setTitle(getString(R.string.oto_select_recommend_task));
			ab.setItems(getResources().getStringArray(R.array.oto_chat_msg_menu),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					CharSequence text = nowConvResource.body.getText();
					ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
					switch(which){
					case 0:
						cm.setText(text);
						break;
					}
				}
			});
			ab.show();
		}
		
		public ImageView getUser_img() {
			return user_img;
		}
		public void setUser_img(ImageView user_img) {
			this.user_img = user_img;
		}
		public void setAsDefaultMsg(){
			nowConvResource.body.setVisibility(View.VISIBLE);
			nowConvResource.bodyImg.setVisibility(View.GONE);
			nowConvResource.bodyImg2.setVisibility(View.GONE);
			nowConvResource.bodyImgDetail.setVisibility(View.GONE);
			nowConvResource.bodyProg.setVisibility(View.GONE);
		}
		
		public void setAsImgMsg(){
			nowConvResource.body.setVisibility(View.GONE);
			nowConvResource.bodyImg.setVisibility(View.GONE);
			nowConvResource.bodyImg2.setVisibility(View.GONE);
			nowConvResource.bodyImgDetail.setVisibility(View.GONE);
			nowConvResource.bodyProg.setVisibility(View.GONE);
		}
		
		public void setImgBodyProg(boolean enable){
			if(enable){
				nowConvResource.body.setVisibility(View.GONE);
				nowConvResource.bodyImg.setVisibility(View.GONE);
				nowConvResource.bodyImg2.setVisibility(View.GONE);
				nowConvResource.bodyImgDetail.setVisibility(View.GONE);
				nowConvResource.bodyProg.setVisibility(View.VISIBLE);
			}else{
				nowConvResource.body.setVisibility(View.VISIBLE);
				nowConvResource.bodyProg.setVisibility(View.GONE);
			}
		}
		
		public void setMsg(String msg){
			nowConvResource.body.setText(msg);
		}
		
		public void setTime(long msgTime){
			nowConvResource.time.setText(getSimpleDatePartTwo(msgTime));
		}
		
		public void setStatus(int type, long count){
			setLayoutFail(false);
			switch(type){
			case STATUS_SENDING:
				nowConvResource.status.setVisibility(View.VISIBLE);
				nowConvResource.status.setText(getString(R.string.oto_sending));
				break;
			case STATUS_NOT_READ:
			case STATUS_READ:
				setNotReadCount(count);
				break;
			}
		}
		
		public void setNotReadCount(long count){
			if(count == 0){
				nowConvResource.status.setVisibility(View.GONE);
			}else{
				nowConvResource.status.setVisibility(View.VISIBLE);
				nowConvResource.status.setText(String.valueOf(count));
			}
		}
		
		public void setLayoutFail(boolean fail){
			if(fail){
				if(reSend != null)reSend.setVisibility(View.VISIBLE);
				nowConvResource.statusLayout.setVisibility(View.GONE);
			}else{
				if(reSend != null)reSend.setVisibility(View.GONE);
				nowConvResource.statusLayout.setVisibility(View.VISIBLE);
			}
		}

		public View getMainView() {
			return mainView;
		}
	}
	
	public enum ListElemType{
		MSG,
		MSG_DATE,
		INFO
	}
	
	public class ListElem implements Comparator<ListElem>{
		
		public ListElemType elemType;
		public OTMsgBase msg;
		public String infoString;
		public String msgDate;
		
		public long time;
		
		public ListElem(){}
		
		@Override
		public int compare(ListElem lhs, ListElem rhs) {
			long lTime = 0, rTime = 0;
			if(lhs.msg != null) lTime = lhs.msg.getTime();
			else lTime = lhs.time;
			if(rhs.msg != null) rTime = rhs.msg.getTime();
			else rTime = rhs.time;
			
			if(lhs.msg != null && rhs.msg != null){
				if(lhs.msg.getId() == -1 && rhs.msg.getId() == -1){
					return (lTime < rTime)?-1:1;
				}else if(lhs.msg.getId() == -1){
					return 1;
				}else if(rhs.msg.getId() == -1){
					return -1;
				}
			}else if(lhs.msg != null){
				if(lhs.msg.getId() == -1){
					return 1;
				}
			}else if(rhs.msg != null){
				if(rhs.msg.getId() == -1){
					return -1;
				}
			}
			
			return (lTime < rTime)?-1:1;
		}
		
		public ListElem(OTMsgBase msg){
			this.msg = msg;
			setupListElem(msg, this);
		}
		public ListElem(String data, ListElemType elemType, long time){
			this.time = time;
			if(elemType == ListElemType.INFO){
				this.infoString = data;
			}
			if(elemType == ListElemType.MSG_DATE){
				this.msgDate = data;
			}
			this.elemType = elemType;
		}
	}
	
	public abstract boolean hasPendingMsg(OTMsgBase msg);
	
	public class MsgListAdapater extends ArrayAdapter<ListElem>{
		List<ListElem> items;
		public MsgListAdapater(Context context, int textViewResourceId, List<ListElem> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects; 
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			MsgHolder holder = null;
			if(convertView == null){
				FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.ot_conv_inside_elem_base, null);
				
				LinearLayout left = (LinearLayout) getLayoutInflater().inflate(R.layout.ot_conv_inside_elem_left, null);
				left.setId(R.id.oto_conv_left_view);
				
				LinearLayout right = (LinearLayout) getLayoutInflater().inflate(R.layout.ot_conv_inside_elem_right, null);
				right.setId(R.id.oto_conv_right_view);
				right.setGravity(Gravity.RIGHT);
				layout.addView(left);
				
				LinearLayout.LayoutParams rightParam = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				rightParam.gravity = Gravity.RIGHT;
				layout.addView(right, rightParam);
				
				convertView = layout;
				
				holder = new MsgHolder(convertView);
				convertView.setTag(holder);
			}else{
				holder = (MsgHolder) convertView.getTag();
			}
			
			ListElem elem = items.get(position);
			holder.setListElem(elem);
			setupConvView(elem, holder, this);
			if(elem.elemType == ListElemType.MSG){
				OTMsgBase msg = elem.msg;
				if(msg.isImgMsg()){
					if(msg.getImg_url() == null){
						//uploading
						if(hasPendingMsg(msg)){
							holder.setImgBodyProg(true);
						}else{
							holder.setImgBodyProg(false);
							holder.setLayoutFail(true);
							if(msg.getPreSendImg_url() != null){
								JSONArray realUrls = new JSONArray();
								for(int i=0;i<msg.getPreSendImg_url().length();++i){
									try {
										String nowUrl = msg.getPreSendImg_url().getString(i);
										String imageUrl = "file://" + nowUrl;
										realUrls.put(imageUrl);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
								setupViewImage(realUrls, holder, this);
							}
						}
					}else{
						holder.setImgBodyProg(false);
						JSONArray realUrls = new JSONArray();
						for(int i=0;i<msg.getImg_url().length();++i){
							try {
								String nowUrl = msg.getImg_url().getString(i);
								String imageUrl = TASatelite.makeImageUrl(nowUrl);
								realUrls.put(imageUrl);
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
						setupViewImage(realUrls, holder, this);
					}
					if(msg.getMsg().length() == 0){
						holder.nowConvResource.body.setVisibility(View.GONE);
					}
				}
			}
			
			return holder.getMainView();
		}
	}
	
	void setupViewImage(JSONArray imgUrls, MsgHolder holder, ArrayAdapter<?> adapter){
		try {			
			if(imgUrls.length() == 1){
				LayoutParams param = new LinearLayout.LayoutParams((int)PLEtcUtilMgr.dpToPx(getResources(), 160),
						(int)PLEtcUtilMgr.dpToPx(getResources(), 160));
				holder.nowConvResource.bodyImg.setLayoutParams(param);
			}else if(imgUrls.length() == 2){
				LayoutParams param = new LinearLayout.LayoutParams((int)PLEtcUtilMgr.dpToPx(getResources(), 75),
						(int)PLEtcUtilMgr.dpToPx(getResources(), 75));
				holder.nowConvResource.bodyImg.setLayoutParams(param);
				
				LayoutParams param2 = new LinearLayout.LayoutParams((int)PLEtcUtilMgr.dpToPx(getResources(), 75),
						(int)PLEtcUtilMgr.dpToPx(getResources(), 75));
				param2.leftMargin = (int)PLEtcUtilMgr.dpToPx(getResources(), 10);
				holder.nowConvResource.bodyImg2.setLayoutParams(param2);
			}else{
				LayoutParams param = new LinearLayout.LayoutParams((int)PLEtcUtilMgr.dpToPx(getResources(), 60),
						(int)PLEtcUtilMgr.dpToPx(getResources(), 60));
				holder.nowConvResource.bodyImg.setLayoutParams(param);
				
				LayoutParams param2 = new LinearLayout.LayoutParams((int)PLEtcUtilMgr.dpToPx(getResources(), 60),
						(int)PLEtcUtilMgr.dpToPx(getResources(), 60));
				param2.leftMargin = (int)PLEtcUtilMgr.dpToPx(getResources(), 10);
				holder.nowConvResource.bodyImg2.setLayoutParams(param2);
			}
			
			holder.nowConvResource.bodyImgDetail.setVisibility(View.GONE);
			holder.nowConvResource.bodyImg.setVisibility(View.VISIBLE);
			loadImageOnList(imgUrls.getString(0), holder.nowConvResource.bodyImg, R.drawable.oto_friend_img_01, adapter, true, false);
			if(imgUrls.length() > 1){
				holder.nowConvResource.bodyImg2.setVisibility(View.VISIBLE);
				loadImageOnList(imgUrls.getString(1), holder.nowConvResource.bodyImg2, R.drawable.oto_friend_img_01, adapter, true, false);
				if(imgUrls.length() > 2){
					holder.nowConvResource.bodyImgDetail.setVisibility(View.VISIBLE);
					holder.nowConvResource.bodyImgDetail.setText(
							String.format(getString(R.string.oto_image_count_detail), imgUrls.length() - 2));
				}
			}else{
				holder.nowConvResource.bodyImg2.setVisibility(View.GONE);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override public void onDialogSelected(int dialogId, int pos) { }
	@Override public void onWithViewDialogSelected(int dialogId, int pos, View bodyView) {}
	
	@Override
	public void onDialogSelectedWithData(int dialogId, int pos, Object data) {
		Long transact_id = (Long)data;
		
		switch(dialogId){
		case DIALOG_RESEND:
			if(pos == DialogInterface.BUTTON_POSITIVE){
				OnReSendButtonPressed(transact_id);
			}else if(pos == DialogInterface.BUTTON_NEGATIVE){
				OnDelMsgButtonPressed(transact_id);
			}
			break;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		OTOApp.getInstance().getConvMgr().setRoomScreenOff(true);
	}
	
	protected void onResume() {
		super.onResume();
		OTOApp.getInstance().getConvMgr().setRoomScreenOff(false);
	}

	@Override
	public void onKeyBoardUp() {
		new Handler().post(new Runnable() {
			@Override public void run() {
				convListScrollToBottom();
			}
		});
	}

	@Override
	public void onKeyBoardDown() {
		// TODO Auto-generated method stub
		
	}
	
	public void convListScrollToBottom(){
		convListView.setSelection(msgListAdapter.getCount() - 1);
		convListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				convListView.setSelection(msgListAdapter.getCount() - 1);
			}
		}, 30L);
	}

	@Override
	public void OnImageLoadComplete(List<String> ImagePaths) {
		for(String imagePath : ImagePaths){
			OnImageLoadComplete(imagePath);
		}
	}

	@Override
	public void OnImageLoadComplete(String ImagePath) {
		imageBarLayout.setVisibility(View.VISIBLE);
		LayoutInflater lif = getLayoutInflater();
		View imageLayout = lif.inflate(R.layout.ot_conv_inside_added_image, null);
		imageLayout.setTag(ImagePath);
		ImageView img = (ImageView) imageLayout.findViewById(R.id.oto_conv_detail_image_image);
		View closeView = imageLayout.findViewById(R.id.oto_conv_detail_image_cancel);
		closeView.setTag(imageLayout);
		
		closeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				View layout = (View) view.getTag();
				imageBarView.removeView(layout);
				if(imageBarView.getChildCount() == 0){
					imageBarLayout.setVisibility(View.GONE);
				}
			}
		});
		Bitmap nMap = TAImgDownloader.decodeBitmapProperly(ImagePath, false);
		if(nMap != null){
			roundDisplayer.display(nMap, new ImageViewAware(img), null);
		}
		
		int dp_100 = (int)PLEtcUtilMgr.dpToPx(getResources(), 100.0f);
		LinearLayout.LayoutParams param = new LayoutParams(dp_100, dp_100);
		imageBarView.addView(imageLayout, param);
		
		//sendImgMsg(ImagePath);
	}
	
	long getCalcBaseTime(long time){
		return getCalcTimeDate(getCalcFormatDate(time));
	}
	
	String getCalcFormatDate(long time){
		return new SimpleDateFormat("yyyy.MM.dd").format(time);
	}
	long getCalcTimeDate(String value){
		try {
			return new SimpleDateFormat("yyyy.MM.dd").parse(value).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return -1L;
	}
	
	public String getBaseDatePartOne(long time){
		if(Locale.getDefault().getLanguage().equalsIgnoreCase("ko")){
			return new SimpleDateFormat("yyyy" + getString(R.string.oto_year) + "  MM" + getString(R.string.oto_month) + "  dd"  + getString(R.string.oto_day) + "  E" + getString(R.string.oto_day_of_week)).format(time);
		}else{
			return new SimpleDateFormat("MM/dd/yyyy").format(time);
		}
	}
	
	public String getSimpleDatePartTwo(long time){
		return new SimpleDateFormat("aa hh:mm").format(time);
	}
	public int pxToDip(int val){
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
	}
	public float pxToFloatDip(int val){
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
	}
}

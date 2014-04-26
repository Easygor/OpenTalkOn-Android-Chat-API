package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.clientpackets.protobuf.ClientPackets.Notify;
import com.thinkspace.common.util.PLDialogListener;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.common.util.PLUIUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTComMsg;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.helper.OTHorizontalScrollView;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper.OnLoadUserImageUrl;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.ui.helper.ImageCacheActivity;
import com.thinkspace.opentalkon.ui.helper.RoundedBitmapDisplayer;
import com.thinkspace.pushservice.satelite.PLNotifyHandler;

public class OTOpenTalkDetail extends ImageCacheActivity implements TADataHandler, PLNotifyHandler {
	long post_id;
	boolean click_reply;
	OTComMsg msg;
	TASatelite satelite;
	RoundedBitmapDisplayer roundDisplayer = new RoundedBitmapDisplayer(10);
	SimpleBitmapDisplayer displayer = new SimpleBitmapDisplayer();
	
	ImageView senderImageView;
	TextView senderNickNameView;
	TextView sendTimeView;
	ViewGroup mainView;
	ScrollView mainScrollView;
	OTHorizontalScrollView imageBarScrollView;
	ViewGroup imageBarView;
	TextView bodyView;
	
	View likeBtnView;
	View commentBtnView;
	View deleteBtnView;
	
	TextView likeCntView;
	TextView likeTextView;
	ViewGroup commentBarView;
	
	ViewGroup replySendLayout;
	EditText replyEditView;
	View replySendBtnView;
	ViewGroup funcLayout;
	
	boolean goBottom;
	boolean isCreating;
	boolean authority;
	
	int imagePos;
	boolean shotImagePos;
	Handler handler;
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if(msg != null){
			if(OTOApp.getInstance().getConvMgr().hasCommunity(msg.getCommunity_id()) == false){
				Intent acintent = new Intent(this, OTOpenTalkRoom.class);
				acintent.putExtra("community_id", msg.getCommunity_id());
				startActivity(acintent);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isCreating = true;
		setContentView(R.layout.ot_community_post_detail);
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		handler = new Handler();
		
		Intent intent = getIntent();
		if(intent == null || intent.hasExtra("post_id") == false)
			finish();
		
		post_id = intent.getLongExtra("post_id", -1L);
		if(post_id == -1L)
			finish();
		
		click_reply = intent.getBooleanExtra("click_reply", false);
		authority = intent.getBooleanExtra("authority", true);
		int tableIdx = intent.getIntExtra("tableIdx", -1);
		if(tableIdx != -1){
			OTOApp.getInstance().getCacheCtrl().setCheckNotification(tableIdx, true);
		}
		
		imagePos = intent.getIntExtra("imagePos", -1);
		
		senderImageView = (ImageView) findViewById(R.id.oto_com_post_detail_sender_img);
		senderNickNameView = (TextView) findViewById(R.id.oto_com_post_detail_sender_nickname);
		sendTimeView = (TextView) findViewById(R.id.oto_com_post_detail_sender_time);
		mainView = (ViewGroup) findViewById(R.id.oto_com_post_detail_main);
		mainScrollView = (ScrollView) findViewById(R.id.oto_com_post_detail_main_scroll);
		imageBarScrollView = (OTHorizontalScrollView)findViewById(R.id.oto_com_post_detail_image_bar_scroll);
		imageBarView = (ViewGroup) findViewById(R.id.oto_com_post_detail_image_bar);
		bodyView = (TextView) findViewById(R.id.oto_com_post_detail_msg_body);
		
		likeBtnView = findViewById(R.id.oto_com_post_detail_like_btn);
		commentBtnView = findViewById(R.id.oto_com_post_detail_comment_btn);
		deleteBtnView = findViewById(R.id.oto_com_post_detail_delete_btn);
		
		likeCntView = (TextView) findViewById(R.id.oto_com_post_detail_like_count);
		likeTextView = (TextView) findViewById(R.id.oto_com_post_detail_like_text);
		commentBarView = (ViewGroup) findViewById(R.id.oto_com_post_detail_reply_bar);
		replyEditView = (EditText)findViewById(R.id.oto_com_post_detail_reply_edittext);
		replySendBtnView = findViewById(R.id.oto_com_post_detail_reply_send_btn);
		funcLayout = (ViewGroup) findViewById(R.id.oto_com_post_detail_func_layout);
		replySendLayout = (ViewGroup) findViewById(R.id.oto_com_post_detail_reply_layout);
		satelite = new TASatelite(this, true);
		//EmoticonTextHelper.applyTextEmoticon(this, bodyView);
		
		OTOApp.getInstance().getPushClient().registerNotifyHandler(this);
		satelite.doSetCommunityDetailFeed(OTOApp.getInstance().getToken(), post_id, true);
		OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
		satelite.doGetCommunityPost(OTOApp.getInstance().getToken(), post_id);
	}
	
	@Override
	public void onNotify(Notify packet) {
		if(packet.hasPostChanged()){
			long post_id = packet.getPostChanged().getPostId();
			if(post_id != this.post_id) return;
			satelite.doGetCommunityPost(OTOApp.getInstance().getToken(), post_id);
		}
	}

	@Override
	protected void onDestroy() {
		OTOApp.getInstance().getPushClient().unRegisterNotifyHandler(this);
		satelite.doSetCommunityDetailFeed(OTOApp.getInstance().getToken(), post_id, false);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(isCreating == false){
			satelite.doGetCommunityPost(OTOApp.getInstance().getToken(), post_id);
		}
		if(isCreating)isCreating = false;
	}

	public void setupReplyView(ViewGroup parentView, JSONObject reply, LayoutInflater li, boolean isAdmin) throws JSONException{
		final long id = reply.getLong("id");
		long send_time = reply.getLong("send_time");
		boolean deleted = reply.getBoolean("deleted");
		
		View replyView = li.inflate(R.layout.ot_community_post_detail_reply, null);
		final ImageView img = (ImageView) replyView.findViewById(R.id.oto_reply_img);
		TextView nick = (TextView) replyView.findViewById(R.id.oto_reply_nickname);
		TextView body = (TextView) replyView.findViewById(R.id.oto_reply_body);
		View close = replyView.findViewById(R.id.oto_reply_close);
		TextView time = (TextView) replyView.findViewById(R.id.oto_reply_time);
		
		time.setText(PLEtcUtilMgr.getDateFormat(send_time));
		if(deleted == false){
			final long sender_id = reply.getLong("sender_id");
			TAUserInfo senderInfo = TASateliteDispatcher.dispatchUserInfo(reply.getJSONObject("sender_info"), true);
			img.setImageResource(R.drawable.oto_friend_img_01);
			UserImageUrlHelper.loadUserImage(sender_id, new OnLoadUserImageUrl() {
				@Override
				public void onLoad(long user_id, String url, boolean fromCache) {
					if(url.length() == 0){
						img.setImageResource(R.drawable.oto_friend_img_01);
					}else{
						img.setImageResource(R.drawable.oto_friend_img_01);
						OTOApp.getInstance().getImageDownloader().requestImgDownload(url, new TAImageDataHandler() {
							@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
								roundDisplayer.display(bitmap, new ImageViewAware(img), null);
							}
							@Override public void onHttpImageException(Exception ex) {
								img.setImageResource(R.drawable.oto_friend_img_01);
							}
						});
					}
				}
			});
			nick.setText(senderInfo.getNickName());
			body.setText(reply.getString("msg"));
			
			if(sender_id == OTOApp.getInstance().getId() || isAdmin){
				close.setVisibility(View.VISIBLE);
				close.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						OTOApp.getInstance().getDialogMaker().makeYesNoDialog(
							getString(R.string.oto_delete_reply),
							getString(R.string.oto_delete_reply_description),
							new PLDialogListener() {
								@Override public void onWithViewDialogSelected(int dialogId, int pos, View bodyView) {
									
								}
								@Override public void onDialogSelected(int dialogId, int pos) {
									if(pos == DialogInterface.BUTTON_POSITIVE){
										OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTOpenTalkDetail.this);
										satelite.doDelCommunityReply(OTOApp.getInstance().getToken(), id);
									}
								}
								@Override public void onDialogSelectedWithData(int dialogId, int pos, Object data) {
									
								}
							}, OTOpenTalkDetail.this, 0);
					}
				});
			}else{
				close.setVisibility(View.INVISIBLE);
			}
			img.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(OTOpenTalkDetail.this, OTFriendPopup.class);
					Bundle bundle = new Bundle();
					bundle.putLong("user_id", sender_id);
					bundle.putBoolean("authority", authority);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
		}else{
			img.setImageResource(R.drawable.oto_friend_img_01);
			nick.setText(getString(R.string.oto_unknown));
			body.setText(getString(R.string.oto_reply_deleted));
			close.setVisibility(View.INVISIBLE);
		}
		
		parentView.addView(replyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}
	
	public void drawLayout(final OTComMsg msg, boolean likeThis, boolean canReply, boolean isAdmin, JSONArray replys){
		UserImageUrlHelper.loadUserImage(msg.getSender_id(), new OnLoadUserImageUrl() {
			@Override
			public void onLoad(long user_id, String url, boolean fromCache) {
				if(url.length() == 0){
					senderImageView.setImageResource(R.drawable.oto_friend_img_01);
				}else{
					senderImageView.setImageResource(R.drawable.oto_friend_img_01);
					OTOApp.getInstance().getImageDownloader().requestImgDownload(url, new TAImageDataHandler() {
						@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
							roundDisplayer.display(bitmap, new ImageViewAware(senderImageView), null);
						}
						@Override public void onHttpImageException(Exception ex) {
							senderImageView.setImageResource(R.drawable.oto_friend_img_01);
						}
					});
				}
			}
		});
		
		if(canReply){
			replyEditView.setVisibility(View.VISIBLE);
			replySendBtnView.setVisibility(View.VISIBLE);
		}else{
			replyEditView.setVisibility(View.INVISIBLE);
			replySendBtnView.setVisibility(View.INVISIBLE);
		}	
		
		String nickName = TAUserNick.getInstance().getUserInfo(msg.getSender_id());
		senderNickNameView.setText(nickName);
		sendTimeView.setText(PLEtcUtilMgr.getDateFormat(msg.getTime()));
		if(msg.getImg_url() == null || msg.getImg_url().length() == 0){
			imageBarScrollView.setVisibility(View.GONE);
		}else{
			imageBarView.setVisibility(View.VISIBLE);
			imageBarView.removeAllViews();
			try {
				final ArrayList<String> imgUrlArr = new ArrayList<String>();
				for(int i=0;i<msg.getImg_url().length();++i){
					imgUrlArr.add(msg.getImg_url().getString(i));
				}
				
				if(imgUrlArr.size() == 1){
					Display display = getWindowManager().getDefaultDisplay();
					Point screenSize = new Point();
					screenSize = PLUIUtilMgr.getDisplaySize(display);
					
					final ImageView imgView = new ImageView(this);
					imgView.setScaleType(ScaleType.FIT_XY);
					LayoutParams param = new LinearLayout.LayoutParams(screenSize.x, screenSize.x);
					imageBarView.addView(imgView, param);
					imgView.setImageResource(R.drawable.oto_friend_img_01);
					OTOApp.getInstance().getImageDownloader().requestImgDownload(TASatelite.makeImageUrl(imgUrlArr.get(0)), new TAImageDataHandler() {
						@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
							displayer.display(bitmap, new ImageViewAware(imgView), null);
						}
						@Override public void onHttpImageException(Exception ex) {
							imgView.setImageResource(R.drawable.oto_friend_img_01);
						}
					});
					imageBarScrollView.setBlockVerticalScroll(false);
					imageBarScrollView.setLayoutParams(new LinearLayout.LayoutParams(screenSize.x, screenSize.x));
					imageBarView.setLayoutParams(new FrameLayout.LayoutParams(screenSize.x, screenSize.x));
					
					imgView.setOnClickListener(new OnClickListener() {
						@Override public void onClick(View v) {
							Intent intent = new Intent(OTOpenTalkDetail.this, OTEntireImageView.class);
							intent.putExtra("img_path", imgUrlArr);
							intent.putExtra("img_pos", 0);
							
							OTOpenTalkDetail.this.startActivity(intent);
						}
					});
				}else{
					final int size = imageBarView.getHeight();
					for(int i=0;i<imgUrlArr.size();++i){
						final int imgPos = i;
						final ImageView imgView = new ImageView(this);
						imgView.setScaleType(ScaleType.FIT_XY);
						LayoutParams param = new LinearLayout.LayoutParams(size, size);
						if(i != msg.getImg_url().length() - 1) { param.rightMargin = 5; }
						
						imageBarView.addView(imgView, param);
						imgView.setImageResource(R.drawable.oto_friend_img_01);
						OTOApp.getInstance().getImageDownloader().requestImgDownload(TASatelite.makeImageUrl(imgUrlArr.get(i)), new TAImageDataHandler() {
							@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
								displayer.display(bitmap, new ImageViewAware(imgView), null);
							}
							@Override public void onHttpImageException(Exception ex) {
								imgView.setImageResource(R.drawable.oto_friend_img_01);
							}
						});
						imgView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Intent intent = new Intent(OTOpenTalkDetail.this, OTEntireImageView.class);
								intent.putExtra("img_path", imgUrlArr);
								intent.putExtra("img_pos", imgPos);
								
								OTOpenTalkDetail.this.startActivity(intent);
							}
						});
					}
					if(imagePos != -1 && shotImagePos == false){
						shotImagePos = true;
						handler.postDelayed(new Runnable() {
							@Override public void run() {
								imageBarScrollView.scrollTo(size * imagePos, 0);
							}
						}, 100);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			imageBarView.invalidate();
		}
		if(msg.getMsg().length() != 0){
			bodyView.setVisibility(View.VISIBLE);
			bodyView.setText(msg.getMsg());
		}else{
			bodyView.setVisibility(View.GONE);
		}
		if(likeThis){
			likeTextView.setText(getString(R.string.oto_cancel_like));
			if(msg.getLike_count() - 1 <= 0){
				likeCntView.setText(String.format(getString(R.string.oto_likecnt_string3)));
			}else{
				likeCntView.setText(String.format(getString(R.string.oto_likecnt_string), msg.getLike_count() - 1));
			}
		}else{
			likeTextView.setText(getString(R.string.oto_like));
			likeCntView.setText(String.format(getString(R.string.oto_likecnt_string2), msg.getLike_count()));
		}
		
		commentBarView.removeAllViews();
		LayoutInflater li = getLayoutInflater();
		for(int i=0;i<replys.length();++i){
			try {
				JSONObject reply = replys.getJSONObject(i);
				setupReplyView(commentBarView, reply, li, isAdmin);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		likeCntView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(OTOpenTalkDetail.this, OTLike.class);
				intent.putExtra("post_id", post_id);
				startActivity(intent);
			}
		});
		
		senderImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(OTOpenTalkDetail.this, OTFriendPopup.class);
				Bundle bundle = new Bundle();
				bundle.putLong("user_id", msg.getSender_id());
				bundle.putBoolean("authority", authority);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		
		likeBtnView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				satelite.doLikeCommunity(OTOApp.getInstance().getToken(), msg.getId());
			}
		});
		
		if(msg.getSender_id() == OTOApp.getInstance().getId() || isAdmin){
			deleteBtnView.setVisibility(View.VISIBLE);
			deleteBtnView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					OTOApp.getInstance().getDialogMaker().makeYesNoDialog(getString(R.string.oto_lib_name),
						getString(R.string.oto_delete_post_question), new PLDialogListener() {
							@Override public void onWithViewDialogSelected(int dialogId, int pos, View bodyView) {}
							@Override public void onDialogSelectedWithData(int dialogId, int pos, Object data) {}
							@Override
							public void onDialogSelected(int dialogId, int pos) {
								if(pos == DialogInterface.BUTTON_POSITIVE){
									satelite.doDelCommunityPost(OTOApp.getInstance().getToken(), post_id);
								}
							}
						}, OTOpenTalkDetail.this, 0);
				}
			});
		}else{
			deleteBtnView.setVisibility(View.GONE);
		}
		
		commentBtnView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		        imm.showSoftInput(replyEditView, 0);
		        handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mainScrollView.scrollTo(mainView.getScrollX(), mainView.getHeight());
					}
				}, 200);
			}
		});
		
		replyEditView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mainScrollView.scrollTo(mainView.getScrollX(), mainView.getHeight());
					}
				}, 200);
			}
		});
		
		replySendBtnView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				String textMsg = replyEditView.getText().toString();
				if(textMsg.length() != 0){
					replyEditView.getText().clear();
					OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTOpenTalkDetail.this);
					satelite.doReplyCommunity(OTOApp.getInstance().getToken(), msg.getId(), textMsg);
				}
			}
		});
		
		if(goBottom){
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mainScrollView.scrollTo(mainView.getScrollX(), mainView.getHeight());
					if(click_reply){
						InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				        imm.showSoftInput(replyEditView, 0);
				        click_reply = false;
					}
				}
			}, 200);
			
			goBottom = false;
		}
		
		if(click_reply){
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
			        imm.showSoftInput(replyEditView, 0);
			        click_reply = false;
				}
			}, 200);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mainScrollView.scrollTo(mainView.getScrollX(), mainView.getHeight());
				}
			}, 400);
		}
		
		if(authority == false){
			replySendLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		try{
			String state = data.getString("state");
			String location = data.getString("location");
			if(location.equals(TASatelite.getName(TASatelite.GET_COMMUNITY_POST))){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					if(realData.has("deleted")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_lib_name),
							getString(R.string.oto_deleted_post), new PLDialogListener() {
								@Override public void onWithViewDialogSelected(int dialogId, int pos, View bodyView) {}
								@Override public void onDialogSelectedWithData(int dialogId, int pos, Object data) {}
								@Override
								public void onDialogSelected(int dialogId, int pos) {
									Intent intent = new Intent();
									intent.putExtra("post_id", post_id);
									setResult(RESULT_OK, intent);
									finish();
								}
							}, 0);
					}else{
						boolean like_this = realData.getBoolean("like_this");
						boolean is_ban = realData.getBoolean("is_ban");
						boolean is_admin = realData.getBoolean("is_admin");
						msg = OTOpenTalkRoom.parseComMsg(realData);
						drawLayout(msg, like_this, is_ban == false, is_admin, realData.getJSONArray("replys"));
					}
				}
			}else if(location.equals(TASatelite.getName(TASatelite.LIKE_COMMUNITY))){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					String status = realData.getString("status");
					int like_count = realData.getInt("like_count");
					if(status.equals("like")){
						msg.setLike_count(like_count);
						likeTextView.setText(getString(R.string.oto_cancel_like));
						if(msg.getLike_count() - 1 <= 0){
							likeCntView.setText(String.format(getString(R.string.oto_likecnt_string3)));
						}else{
							likeCntView.setText(String.format(getString(R.string.oto_likecnt_string), msg.getLike_count() - 1));
						}
					}else{
						msg.setLike_count(like_count);
						likeTextView.setText(getString(R.string.oto_like));
						likeCntView.setText(String.format(getString(R.string.oto_likecnt_string2), msg.getLike_count()));
					}
				}else{
					if(state.equals("app_code is not valid")){
						OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_app_code_is_not_valid), this);
					}
				}
			}else if(location.equals(TASatelite.getName(TASatelite.REPLY_COMMUNITY))){
				if(state.equals("ok")){
					satelite.doGetCommunityPost(OTOApp.getInstance().getToken(), post_id);
					goBottom = true;
				}
			}else if(location.equals(TASatelite.getName(TASatelite.DEL_COMMUNITY_REPLY))){
				if(state.equals("ok")){
					satelite.doGetCommunityPost(OTOApp.getInstance().getToken(), post_id);
				}
			}else if(location.equals(TASatelite.getName(TASatelite.DEL_COMMUNITY_POST))){
				if(state.equals("ok")){
					Intent intent = new Intent();
					intent.putExtra("post_id", post_id);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
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
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
}

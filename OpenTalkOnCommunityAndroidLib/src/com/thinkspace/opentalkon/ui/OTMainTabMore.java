package com.thinkspace.opentalkon.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.opentalkon.facebook.Request;
import com.opentalkon.facebook.Response;
import com.opentalkon.facebook.Session;
import com.opentalkon.facebook.Session.StatusCallback;
import com.opentalkon.facebook.SessionState;
import com.opentalkon.facebook.model.GraphUser;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAPreference;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroupView;
import com.thinkspace.opentalkon.ui.helper.RoundedBitmapDisplayer;
public class OTMainTabMore extends PLActivityGroupView implements TADataHandler {
	RoundedBitmapDisplayer roundDisplayer = new RoundedBitmapDisplayer(10);
	ImageView userImg;	
	View profileView;
	View ignoreView;
	
	View option1View;
	ImageView option1ImgView;
	View option2View;
	ImageView option2ImgView;
	View option3View;
	ImageView option3ImgView;
	View option4View;
	ImageView option4ImgView;
	View option5View;
	ImageView option5ImgView;
	
	View inviteOption1View;
	ImageView inviteOption1ImgView;
	
	View inviteOption2View;
	ImageView inviteOption2ImgView;
	
	View versionView;
	View moreAppView;
	
	TextView nickNameView;
	TextView introduceView;
	
	View facebook;
	
	View facebookPublicOption;
	ImageView facebookPublicOptionImg;
	ViewGroup facebookPublicOptionLayout;
	
	TASatelite satelite;
	boolean loadingFinish;
	boolean imgDeleted;
	TAUserInfo user_info;
	
	Uri mImageCaptureUri;
	String imgPath;
	
	@Override
	public void onTabDestoryed() {
		super.onTabDestoryed();
		clearCacheFromThis();
	}
	
	public void makeSelectDialog(String[] array, String titleName, DialogInterface.OnClickListener handler){
		AlertDialog.Builder ab = new Builder(OTMainTabMore.this.getContext());
		ab.setTitle(titleName);
		ab.setItems(array, handler);
		ab.show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_sub_tab_more);
		isCreating = true;
		
		userImg = (ImageView)findViewById(R.id.oto_myinfo_image);
		
		profileView = (View)findViewById(R.id.oto_myinfo_profile);
		profileView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(user_info != null){
					Intent intent = new Intent(OTMainTabMore.this, OTSettingMyInfo.class);
					intent.putExtra("is_my_info", true);
					intent.putExtra("user_info", user_info);
					OTMainTabMore.this.startActivity(intent);
				}
			}
		});
		ignoreView = (View)findViewById(R.id.oto_myinfo_ignore);
		ignoreView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(OTMainTabMore.this, OTSettingBlockUser.class);
				OTMainTabMore.this.startActivity(intent);
			}
		});
		
		satelite = new TASatelite(this);
		facebook = findViewById(R.id.oto_oto_my_facebook);
		option1View = (View)findViewById(R.id.oto_myinfo_option1);
		option1ImgView = (ImageView)findViewById(R.id.oto_myinfo_option1_img);
		option2View = (View)findViewById(R.id.oto_myinfo_option2);
		option2ImgView = (ImageView)findViewById(R.id.oto_myinfo_option2_img);
		option3View = (View)findViewById(R.id.oto_myinfo_option3);
		option3ImgView = (ImageView)findViewById(R.id.oto_myinfo_option3_img);
		option4View = (View)findViewById(R.id.oto_myinfo_option4);
		option4ImgView = (ImageView)findViewById(R.id.oto_myinfo_option4_img);
		option5View = (View)findViewById(R.id.oto_myinfo_option5);
		option5ImgView = (ImageView)findViewById(R.id.oto_myinfo_option5_img);
		
		inviteOption1View = (View)findViewById(R.id.oto_invite_option1);
		inviteOption1ImgView = (ImageView)findViewById(R.id.oto_invite_option1_img);
		inviteOption2View = (View)findViewById(R.id.oto_invite_option2);
		inviteOption2ImgView = (ImageView)findViewById(R.id.oto_invite_option2_img);
		
		facebookPublicOption = findViewById(R.id.oto_myinfo_facebook_public_option);
		facebookPublicOptionImg = (ImageView)findViewById(R.id.oto_myinfo_facebook_public_option_img);
		facebookPublicOptionLayout = (ViewGroup)findViewById(R.id.oto_myinfo_facebook_public_option_layout);
		
		boolean logon = false;
		Session session = Session.openActiveSessionFromCache(this);
		if(OTOApp.getInstance().getPref().getFacebook_id().getValue().length() > 0){
			if(session != null && session.isOpened()){
				logon = true;
			}
		}
		
		setupFacebookLayout(logon);
		
		option1View.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				boolean val = OTOApp.getInstance().getPref().getSetting_chat_notifiy().getValue();
				OTOApp.getInstance().getPref().getSetting_chat_notifiy().setValue(!val);
				reDrawCheckBox();
			}
		});
		
		option2View.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				boolean val = OTOApp.getInstance().getPref().getSetting_opentalk_new_post_notifiy().getValue();
				OTOApp.getInstance().getPref().getSetting_opentalk_new_post_notifiy().setValue(!val);
				reDrawCheckBox();
			}
		});
		
		option3View.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				boolean val = OTOApp.getInstance().getPref().getSetting_opentalk_my_like_notifiy().getValue();
				OTOApp.getInstance().getPref().getSetting_opentalk_my_like_notifiy().setValue(!val);
				reDrawCheckBox();				
			}
		});
		
		option4View.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				boolean val = OTOApp.getInstance().getPref().getSetting_opentalk_my_reply_notifiy().getValue();
				OTOApp.getInstance().getPref().getSetting_opentalk_my_reply_notifiy().setValue(!val);
				reDrawCheckBox();				
			}
		});
		
		option5View.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				boolean val = OTOApp.getInstance().getPref().getSetting_opentalk_reply_notifiy().getValue();
				OTOApp.getInstance().getPref().getSetting_opentalk_reply_notifiy().setValue(!val);
				reDrawCheckBox();				
			}
		});
		
		reDrawCheckBox();
		
		versionView = (View)findViewById(R.id.oto_myinfo_version);
		versionView.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				Intent intent = new Intent(OTMainTabMore.this, OTVersion.class);
				OTMainTabMore.this.startActivity(intent);
			}
		});
		
		moreAppView = findViewById(R.id.oto_myinfo_more_app);
		moreAppView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(OTMainTabMore.this, OTMoreApp.class));
			}
		});
		
		nickNameView = (TextView)findViewById(R.id.oto_myinfo_nickname);
		introduceView = (TextView)findViewById(R.id.oto_myinfo_introduce);
		
		imgDeleted = false;
	}
	
	private void setupFacebookLayout(boolean logon){
		OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), getContext());
		satelite.doGetUserInfo(OTOApp.getInstance().getToken(), OTOApp.getInstance().getPref().getUser_id().getValue());
		if(logon){
			satelite.doGetFacebookPublic(OTOApp.getInstance().getToken());
			facebook.setBackgroundResource(R.drawable.oto_facebook_logout_selector);
			facebook.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View v) {
					Session.getActiveSession().closeAndClearTokenInformation();
					TAPreference pref = OTOApp.getInstance().getPref();
					if(pref.getHas_backup().getValue()){
						OTOApp.getInstance().changeUserDatabase(pref.getUser_id().getValue(), pref.getUser_id_backup().getValue());
						pref.getUser_id().setValue(pref.getUser_id_backup().getValue());
						pref.getToken().setValue(pref.getToken_backup().getValue());
						pref.getNickName().setValue(pref.getNickName_backup().getValue());
						
						pref.getHas_backup().setValue(false);
						pref.getUser_id_backup().setValue(-1L);
						pref.getToken_backup().setValue("");
						pref.getNickName_backup().setValue("");
						if(pref.getNickName().getValue().length() > 0){
							OTOApp.getInstance().startPushService(false);
							OTOApp.getInstance().getCacheCtrl().getUnReadMsg(null, null, false);
						}else{
							finish();
						}
					}else{
						pref.getUser_id().setValue(-1L);
						pref.getToken().setValue("");
						pref.getNickName().setValue("");
						finish();
					}
					setupFacebookLayout(false);
				}
			});
		}else{
			facebookPublicOptionLayout.setVisibility(View.GONE);
			facebook.setBackgroundResource(R.drawable.oto_facebook_login_selector);
			facebook.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					Session.openActiveSession(getMyActivityGroup(), true, new StatusCallback() {
						@Override public void call(Session session, SessionState state, Exception exception) {
							OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), getContext());
							Request.newMeRequest(session, new Request.GraphUserCallback() {
								@Override public void onCompleted(GraphUser user, Response response) {
									OTOApp.getInstance().getUIMgr().dismissDialogProgress();
									if (user != null) {
										OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), getContext());
										new TASatelite(new TADataHandler() {
											@Override
											public void onHttpPacketReceived(JSONObject data) {
												OTOApp.getInstance().getUIMgr().dismissDialogProgress();
												try {
													String state = data.getString("state");
													if(state.equals("ok")){
														TAPreference pref = OTOApp.getInstance().getPref();
														JSONObject userInfo = data.getJSONObject("data");
														long id = userInfo.getLong("id");
														String token = userInfo.getString("token");
														String nickName = userInfo.getString("nick_name");
														String facebook_id = userInfo.getString("facebook_id");
														
														if(id != OTOApp.getInstance().getId()){
															//페이스북을 로긴함으로써 사용자가 바뀌었을경우
															OTOApp.getInstance().changeUserDatabase(pref.getUser_id().getValue(), id);
															
															pref.getHas_backup().setValue(true);
															pref.getUser_id_backup().setValue(pref.getUser_id().getValue());
															pref.getToken_backup().setValue(pref.getToken().getValue());
															pref.getNickName_backup().setValue(pref.getNickName().getValue());
															
															pref.getUser_id().setValue(id);
															pref.getToken().setValue(token);
															pref.getNickName().setValue(nickName);
														}
														
														pref.getFacebook_id().setValue(facebook_id);
														OTOApp.getInstance().startPushService(false);
														OTOApp.getInstance().getCacheCtrl().getUnReadMsg(null, null, false);
														setupFacebookLayout(true);
													}
												} catch (JSONException e) {
													e.printStackTrace();
												}
											}
											@Override public void onTokenIsNotValid(JSONObject data) {
												
											}

											@Override public void onLimitMaxUser(JSONObject data) {
												
											}

											@Override
											public void onHttpException(Exception ex, TAMultiData data, String addr) {
												OTOApp.getInstance().getUIMgr().dismissDialogProgress();
											}
											
											@Override
											public void onHttpException(Exception ex, JSONObject data, String addr) {
												OTOApp.getInstance().getUIMgr().dismissDialogProgress();
											}
										}).loginFacebook(OTOApp.getInstance().getToken(), user.getId());
									}
								}
							}).executeAsync();
						}
					});
				}
			});
		}
	}
	
	
	
	@Override
	public void onActivityResultGroup(int requestCode, int resultCode,Intent data) {
		super.onActivityResultGroup(requestCode, resultCode, data);
		Session session = Session.getActiveSession();
		if(session != null){
			session.onActivityResult(this, requestCode, resultCode, data);
		}
	}

	public void reDrawCheckBox(){
		if(OTOApp.getInstance().getPref().getSetting_chat_notifiy().getValue()){
			option1ImgView.setImageResource(R.drawable.oto_check_s);
		}else{
			option1ImgView.setImageResource(R.drawable.oto_check_n);
		}
		
		if(OTOApp.getInstance().getPref().getSetting_opentalk_new_post_notifiy().getValue()){
			option2ImgView.setImageResource(R.drawable.oto_check_s);
		}else{
			option2ImgView.setImageResource(R.drawable.oto_check_n);
		}
		
		if(OTOApp.getInstance().getPref().getSetting_opentalk_my_like_notifiy().getValue()){
			option3ImgView.setImageResource(R.drawable.oto_check_s);
		}else{
			option3ImgView.setImageResource(R.drawable.oto_check_n);
		}
		
		if(OTOApp.getInstance().getPref().getSetting_opentalk_my_reply_notifiy().getValue()){
			option4ImgView.setImageResource(R.drawable.oto_check_s);
		}else{
			option4ImgView.setImageResource(R.drawable.oto_check_n);
		}
		
		if(OTOApp.getInstance().getPref().getSetting_opentalk_reply_notifiy().getValue()){
			option5ImgView.setImageResource(R.drawable.oto_check_s);
		}else{
			option5ImgView.setImageResource(R.drawable.oto_check_n);
		}
	}
	boolean isCreating;
	
	@Override
	protected void onResume() {
		super.onResume();
		if(isCreating == false){
			satelite.doGetUserInfo(OTOApp.getInstance().getToken(), OTOApp.getInstance().getPref().getUser_id().getValue());
			loadingFinish = false;
		}
		if(isCreating) isCreating = false;
	}
	
	public void dataToLayout(){  
		if(loadingFinish == false) return;
		if(user_info == null) return;
		
		nickNameView.setText(user_info.getNickName());
		
		introduceView.setText(user_info.getIntroduce());
		
		if(user_info.isDeny_invitation()){
			inviteOption1ImgView.setImageResource(R.drawable.oto_check_s);
		}else{
			inviteOption1ImgView.setImageResource(R.drawable.oto_check_n);
		}
		
		inviteOption1View.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				satelite.doSetUserOption(OTOApp.getInstance().getToken(), "deny_invitation", !user_info.isDeny_invitation());
			}
		});
		
		if(user_info.isInvite_from_friend()){
			inviteOption2ImgView.setImageResource(R.drawable.oto_check_s);
		}else{
			inviteOption2ImgView.setImageResource(R.drawable.oto_check_n);
		}
		
		inviteOption2View.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				satelite.doSetUserOption(OTOApp.getInstance().getToken(), "invite_from_friend", !user_info.isInvite_from_friend());
			}
		});
		
		if(user_info.getImagePath().length() != 0){
			String img_path = TASatelite.makeImageUrl(user_info.getImagePath());
			userImg.setImageResource(R.drawable.oto_friend_img_01);
			OTOApp.getInstance().getImageDownloader().requestImgDownload(img_path, new TAImageDataHandler() {
				@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
					roundDisplayer.display(bitmap, new ImageViewAware(userImg), null);
				}
				@Override public void onHttpImageException(Exception ex) {
					userImg.setImageResource(R.drawable.oto_friend_img_01);
				}
			});
		}else{
			userImg.setImageResource(R.drawable.oto_friend_img_01);
		}
	}
	
	public void setTextViewFromStringArray(TextView textView, int pos, String[] array){
		if(array.length > pos && pos >= 0){
			textView.setText(array[pos]);
		}
	}

	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		try{
			String location = data.getString("location");
			DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
			if(dData.hasLocation("getuserinfo")){
				dData.setLocation("getuserinfo");
				if(dData.isOK()){
					loadingFinish = true;
					user_info = (TAUserInfo)dData.getData();
					dataToLayout();
				}else{
					if(dData.getState().equals("user_id is not valid")){
						return;
					}
				}
			}else if(dData.hasLocation("setuserinfo")){
				dData.setLocation("setuserinfo");
				if(dData.isOK()){
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_success_modify_user_info_popup), OTMainTabMore.this.getContext());
					user_info = (TAUserInfo)dData.getData();
					dataToLayout();
				}
			}else if(dData.hasLocation("registeruserid")){
				dData.setLocation("registeruserid");
				if(dData.isOK()){
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_success_register_user_id), OTMainTabMore.this.getContext());
					user_info = (TAUserInfo)dData.getData();
					dataToLayout();
				}else{
					String state = dData.getState();
					if(state.equals("already exist")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(getParent(),
								getString(R.string.oto_register_userid), getString(R.string.oto_register_user_id_already_exist));
					}else if(state.equals("user_id length is not valid")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(getParent(),
								getString(R.string.oto_register_userid), getString(R.string.oto_register_user_id_length_limit));
					}
				}
			}else if(dData.hasLocation("getfacebookpublic") || dData.hasLocation("setfacebookpublic") ){
				if(dData.isOK()){
					try {
						final boolean setting = data.getJSONObject("data").getBoolean("setting");
						facebookPublicOptionLayout.setVisibility(View.VISIBLE);
						if(setting){
							facebookPublicOptionImg.setImageResource(R.drawable.oto_check_s);
						}else{
							facebookPublicOptionImg.setImageResource(R.drawable.oto_check_n);
						}
						facebookPublicOption.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								satelite.doSetFacebookPublic(OTOApp.getInstance().getToken(), !setting);
							}
						});
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}else if(TASatelite.SET_UESR_OPTION.endsWith(location)){
				if(dData.isOK()){
					JSONObject realData = data.getJSONObject("data");
					String option = realData.getString("option");
					boolean flag = realData.getBoolean("flag");
					if(option.equals("deny_invitation")){
						user_info.setDeny_invitation(flag);
					}else if(option.equals("invite_from_friend")){
						user_info.setInvite_from_friend(flag);
					}
					dataToLayout();
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

	@Override public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_network_fail), OTOApp.getInstance().getContext());
	}
	@Override public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_network_fail), OTOApp.getInstance().getContext());
	}
}

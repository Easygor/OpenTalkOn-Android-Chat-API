package com.thinkspace.opentalkon.ui;

import java.io.File;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.thinkspace.opentalkon.data.TASetUserInfoData;
import com.thinkspace.opentalkon.data.TAUserInfo;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TAImgDownloader;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.ui.OTImageLoadBase.ImageLoadHandler;
import com.thinkspace.opentalkon.ui.helper.RoundedBitmapDisplayer;

public class OTSettingMyInfo extends OTImageLoadBase implements TADataHandler, ImageLoadHandler{
	RoundedBitmapDisplayer roundDisplayer = new RoundedBitmapDisplayer(10);
	ImageView image;
	EditText nickName;
	EditText introduce;
	Button next;
	Button cancel;
	
	View termsOfUse;
	boolean noTermsOfUse;
	
	boolean imgDeleted;
	
	TASatelite satelite;
	TAUserInfo user_info;
	boolean is_my_info;
	String imgPath;
	
	ImageView agreementCheck;
	TextView agreement1;
	TextView agreement2;
	TextView agreementOk;
	boolean aCheck;
	
	View facebook;
	
	String prevNickName;
	boolean networkGuard;
	
	public OTSettingMyInfo() {
		setImageLoadHandler(this);
	}

	public void makeSelectDialog(String[] array, String titleName, DialogInterface.OnClickListener handler){
		AlertDialog.Builder ab = new Builder(OTSettingMyInfo.this);
		ab.setTitle(titleName);
		ab.setItems(array, handler);
		ab.show();
	}
	
	public void setTextViewFromStringArray(TextView textView, int pos, String[] array){
		if(array.length > pos && pos >= 0){
			textView.setText(array[pos]);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_setting_myinfo);
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		satelite = new TASatelite(this);
		Intent intent = getIntent();
		if(intent != null){
			is_my_info = intent.getBooleanExtra("is_my_info", false);
			user_info = intent.getParcelableExtra("user_info");
		}else is_my_info = false;
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		termsOfUse = findViewById(R.id.oto_oto_setting_terms_of_use);
		facebook = findViewById(R.id.oto_oto_setting_facebook);
		nickName = (EditText) findViewById(R.id.oto_myprofile_nickname);
		introduce =(EditText)findViewById(R.id.oto_myprofile_introduce);
		next = (Button) findViewById(R.id.oto_myprofile_next);
		cancel = (Button) findViewById(R.id.oto_myprofile_cancel);
		image = (ImageView) findViewById(R.id.oto_myprofile_image);
		
		agreementCheck = (ImageView) findViewById(R.id.oto_myprofile_agreement_check);
		agreement1 = (TextView) findViewById(R.id.oto_myprofile_agreement1);
		agreement2 = (TextView) findViewById(R.id.oto_myprofile_agreement2);
		agreementOk = (TextView) findViewById(R.id.oto_myprofile_agreement_ok);
		
		int val = getResources().getInteger(R.integer.oto_no_terms_of_use);
		if(val == 1){
			noTermsOfUse = true;
		}else{
			noTermsOfUse = false;
		}
		
		if(noTermsOfUse){
			termsOfUse.setVisibility(View.GONE);
		}else{
			termsOfUse.setVisibility(View.VISIBLE);
		}
		
		if(is_my_info){
			agreementCheck.setVisibility(View.GONE);
			agreementOk.setVisibility(View.GONE);
			facebook.setVisibility(View.GONE);
		}else{
			agreementCheck.setVisibility(View.VISIBLE);
			agreementOk.setVisibility(View.VISIBLE);
			facebook.setVisibility(View.VISIBLE);
			
			Session session = Session.getActiveSession();
			if(session != null){
				session.closeAndClearTokenInformation();
			}
			
			facebook.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View arg0) {
					Session.openActiveSession(OTSettingMyInfo.this, true, new StatusCallback() {
						@Override public void call(Session session, SessionState state, Exception exception) {
							OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTSettingMyInfo.this);
							Request.newMeRequest(session, new Request.GraphUserCallback() {
								@Override public void onCompleted(final GraphUser user, Response response) {
									OTOApp.getInstance().getUIMgr().dismissDialogProgress();
									if (user != null) {
										OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTSettingMyInfo.this);
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
														
														pref.getFacebook_id().setValue(facebook_id);
														OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_facebook_login_ok), OTSettingMyInfo.this);
														
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
															finishGuard = true;
															startActivity(new Intent(OTSettingMyInfo.this, OTMain.class));
															finish();
														}else{
															OTSettingMyInfo.this.nickName.setText(user.getName());
															facebook.setVisibility(View.GONE);
														}
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
		
		agreementCheck.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(aCheck){
					aCheck = false;
					agreementCheck.setImageResource(R.drawable.oto_check_n);
				}else{
					aCheck = true;
					agreementCheck.setImageResource(R.drawable.oto_check_s);
				}
			}
		});
		
		agreement1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(OTSettingMyInfo.this, OTAgreement.class).putExtra("type", 0));
			}
		});
		
		agreement2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(OTSettingMyInfo.this, OTAgreement.class).putExtra("type", 1));
			}
		});
		
		image.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				AlertDialog.Builder ab = new Builder(OTSettingMyInfo.this);
				ab.setTitle(getString(R.string.oto_add_picture));
				ab.setItems(getResources().getStringArray(R.array.oto_post_item_method),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch(which){
						case 0:
							doTakeAlbumAction();
							break;
						case 1:
							doTakePhotoAction();
							break;
						case 2:
							image.setImageResource(R.drawable.oto_friend_img_01);
							imgDeleted = true;
							break;
						}
					}
				});
				ab.show();
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		nickName.setEnabled(true);
		
		dataToLayout();	
		next.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(networkGuard) return;
				String name = nickName.getText().toString().trim();
				if(name.length() == 0){
					OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTSettingMyInfo.this, getString(R.string.oto_register), getString(R.string.oto_empty_chat_nick_name));
					return;
				}
				TASetUserInfoData data = new TASetUserInfoData();
				if(is_my_info == false){
					if(aCheck == false && noTermsOfUse == false){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTSettingMyInfo.this,
							getString(R.string.oto_lib_name), getString(R.string.oto_agreement_agree_check));
						return;
					}else{
						data.agree_term = true;
					}
				}
				
				data.setToken(OTOApp.getInstance().getToken());
				data.setIntroduce(introduce.getText().toString());
				if(is_my_info){
					if(!prevNickName.equals(nickName.getText().toString().trim())){
						data.setNick_name(nickName.getText().toString().trim());
					}
				}else{
					data.setNick_name(nickName.getText().toString().trim());
				}
				
				data.setDeleted(imgDeleted);
				if(imgPath != null && imgPath.length() != 0){
					data.setImg(new File(imgPath));
				}
				
				OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), getApplicationContext());
				satelite.doSetUserInfo(data);
				networkGuard = true;
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session session = Session.getActiveSession();
		if(session != null){
			session.onActivityResult(this, requestCode, resultCode, data);
		}
	}

	public void dataToLayout(){
		if(is_my_info){
			if(user_info.getImagePath().length() != 0){
				String img_path = TASatelite.makeImageUrl(user_info.getImagePath());
				image.setImageResource(R.drawable.oto_friend_img_01);
				OTOApp.getInstance().getImageDownloader().requestImgDownload(img_path, new TAImageDataHandler() {
					@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
						roundDisplayer.display(bitmap, new ImageViewAware(image), null);
					}
					@Override public void onHttpImageException(Exception ex) {
						image.setImageResource(R.drawable.oto_friend_img_01);
					}
				});
			}else{
				image.setImageResource(R.drawable.oto_friend_img_01);
			}
			prevNickName = user_info.getNickName();
			nickName.setText(user_info.getNickName());
			introduce.setText(user_info.getIntroduce());
		}
	}
	@Override
	protected void onDestroy() {
		//PLEtcUtilMgr.deleteAllFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +BASE_SEND_IMG_PATH);
		super.onDestroy();
	}

	public void hideKeyboard(){
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(introduce.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
		networkGuard = false;
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
		
		if(dData.getLocation().equals(TASatelite.getName(TASatelite.SET_USER_INFO_URL))){
			if(dData.isOK()){
				if(!is_my_info){
					startActivity(new Intent(this, OTMain.class));
					OTOApp.getInstance().getPref().getAgree_term().setValue(aCheck);
				}
				UserImageUrlHelper.flushUserIdMap();
				OTOApp.getInstance().getPref().getNickName().setValue(nickName.getText().toString().trim());
				finish();
			}else{
				String state = dData.getState();
				if(state.equals("nick_name is already exists")){
					OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_nick_name), getString(R.string.oto_nick_name_already));
				}else if(state.equals("limit_max_user")){
					startActivity(new Intent(this, OTLimit.class));
					finish();
				}else{
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_network_fail), this);
				}
			}
		}
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		networkGuard = false;
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_network_fail), this);
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		networkGuard = false;
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_network_fail), this);
	}
	
	@Override
	public void OnImageLoadComplete(String ImagePath) {
		Bitmap nMap = TAImgDownloader.decodeBitmapProperly(ImagePath, false);
		if(nMap != null){
			roundDisplayer.display(nMap, new ImageViewAware(image), null);
		}else{
			OTOApp.getInstance().getImageDownloader().flushCache();
		}
		imgPath = ImagePath;
	}

	@Override
	public void OnImageLoadComplete(List<String> ImagePaths) {
		
	}
}

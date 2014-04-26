package com.thinkspace.opentalkon.ui;

import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.common.util.PLDialogListener;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.TAMakePublicChatData;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.satelite.TAImgDownloader;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.ui.OTImageLoadBase.ImageLoadHandler;


public class OTMakePublicChat extends OTMakeChat implements ImageLoadHandler{
	ViewGroup appendLayout;
	ImageView roomImage;
	TextView roomName;
	String imagePath;
	long community_id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		if(intent == null || intent.hasExtra("community_id") == false){
			finish();
			return;
		}
		
		community_id = intent.getLongExtra("community_id", -1L);
		appendLayout = (ViewGroup)findViewById(R.id.oto_make_chat_append_layout);
		roomImage = (ImageView)findViewById(R.id.oto_make_chat_room_image);
		roomName = (TextView)findViewById(R.id.oto_make_chat_room_name);
		appendLayout.setVisibility(View.VISIBLE);
		setImageLoadHandler(this);
		
		roomImage.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				AlertDialog.Builder ab = new Builder(OTMakePublicChat.this);
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
							roomImage.setImageResource(R.drawable.oto_friend_img_01);
							imagePath = null;
							break;
						}
					}
				});
				ab.show();
			}
		});
	}
	
	@Override
	public void OnImageLoadComplete(String ImagePath) {
		this.imagePath = ImagePath;
		Bitmap nMap = TAImgDownloader.decodeBitmapProperly(ImagePath, false);
		if(nMap != null){
			roundDisplayer.display(nMap, new ImageViewAware(roomImage), null);
		}else{
			OTOApp.getInstance().getImageDownloader().flushCache();
		}
	}

	@Override public void OnImageLoadComplete(List<String> ImagePaths) {}
	
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		super.onHttpPacketReceived(data);
		try{
			String location = data.getString("location");
			String state = data.getString("state");
			if(TASatelite.MAKE_PUBLIC_CHAT.endsWith(location)){
				if(state.equals("ok")){
					JSONObject realData = data.getJSONObject("data");
					final long room_id = realData.getLong("room_id");
					
					boolean low_version_user_exist = realData.getBoolean("low_version_user_exist");
					
					if(low_version_user_exist){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_invite_label_2),
								getString(R.string.oto_invite_low_version), new PLDialogListener() {
									@Override public void onWithViewDialogSelected(int dialogId, int pos, View bodyView) {}
									@Override public void onDialogSelectedWithData(int dialogId, int pos, Object data) {}
									@Override public void onDialogSelected(int dialogId, int pos) {
										Intent intent = new Intent(OTMakePublicChat.this, OTPublicChatRoom.class);
										intent.putExtra("room_id", room_id);
										startActivity(intent);
										finish();
									}
								}, 0);
					}else{
						Intent intent = new Intent(OTMakePublicChat.this, OTPublicChatRoom.class);
						intent.putExtra("room_id", room_id);
						startActivity(intent);
						finish();
					}
				}else{
					if(state.equals("can't make more room")){
						OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_public_opentalk),
								getString(R.string.oto_public_room_cannot_make_more));
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		super.onHttpException(ex, data, addr);
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		super.onHttpException(ex, data, addr);
	}

	@Override
	public void onConfirm() {
		if(roomName.getText().toString().length() == 0){
			OTOApp.getInstance().getDialogMaker().makeAlertDialog(this, getString(R.string.oto_public_opentalk), getString(R.string.oto_insert_room_name));
			return;
		}
		
		JSONArray users = new JSONArray();
		users.put(OTOApp.getInstance().getId());
		for(CheckFriendListElem elem : friendListElems){
			if(elem.isCheck()){
				users.put(elem.getInfo().getId());
			}
		}
		
		TAMakePublicChatData roomData = new TAMakePublicChatData();
		roomData.token = OTOApp.getInstance().getToken();
		roomData.room_name = roomName.getText().toString();
		if(imagePath != null){
			roomData.room_image = new File(imagePath);
		}
		roomData.users = users;
		roomData.community_id = community_id;
		
		OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), this);
		satelite.doMakePublicChat(roomData);
	}
}

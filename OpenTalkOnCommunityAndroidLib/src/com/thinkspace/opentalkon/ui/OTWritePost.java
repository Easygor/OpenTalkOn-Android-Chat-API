package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.thinkspace.opentalkon.ui.helper.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.CommunityData;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper;
import com.thinkspace.opentalkon.helper.UserImageUrlHelper.OnLoadUserImageUrl;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TAImgDownloader;
import com.thinkspace.opentalkon.ui.OTImageLoadBase.ImageLoadHandler;

public class OTWritePost extends OTImageLoadBase implements ImageLoadHandler{
	RoundedBitmapDisplayer roundDisplayer = new RoundedBitmapDisplayer(10);
	ImageView userImage;
	EditText msgView;
	Button sendButton;
	View imageBarLayout;
	ViewGroup imageBarView;
	CommunityData comData;
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String msg = savedInstanceState.getString("msg");
		msgView.setText(msg);
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
		outState.putString("msg", msgView.getText().toString());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_write_post);
		
		Intent intent = getIntent();
		if(intent.hasExtra("community_data") == false){
			finish();
			return;
		}
		int startAction = intent.getIntExtra("startAction", -1);
		comData = intent.getParcelableExtra("community_data");
		userImage = (ImageView)findViewById(R.id.oto_write_post_user_img);
		msgView = (EditText)findViewById(R.id.oto_write_post_body);
		sendButton = (Button)findViewById(R.id.oto_write_post_done);
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				onBackPressed();
			}
		});
		
		findViewById(R.id.oto_write_post_camera).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(imageBarView.getChildCount() < 10){
					doTakePhotoAction();
				}else{
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_picture_limit), OTWritePost.this);
				}
			}
		});
		findViewById(R.id.oto_write_post_image).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(imageBarView.getChildCount() < 10){
					doTakeAlbumAction();
				}else{
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_picture_limit), OTWritePost.this);
				}				
			}
		});
		findViewById(R.id.oto_write_post_image_many).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(imageBarView.getChildCount() < 10){
					doTakeAlbumActionMultiple(imageBarView.getChildCount());
				}else{
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_picture_limit), OTWritePost.this);
				}
			}
		});
		
		imageBarLayout = findViewById(R.id.oto_write_post_image_bar_layout);
		imageBarView = (ViewGroup) findViewById(R.id.oto_write_post_image_bar);
		
		setImageLoadHandler(this);
		
		long user_id = OTOApp.getInstance().getId();
		UserImageUrlHelper.loadUserImage(user_id, new OnLoadUserImageUrl() {
			@Override public void onLoad(long user_id, String url, boolean fromCache) {
				if(url.length() == 0){
					userImage.setImageResource(R.drawable.oto_friend_img_01);
				}else{
					OTOApp.getInstance().getImageDownloader().requestImgDownload(url, new TAImageDataHandler() {
						@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
							roundDisplayer.display(bitmap, new ImageViewAware(userImage), null);
						}
						@Override public void onHttpImageException(Exception ex) {
							userImage.setImageResource(R.drawable.oto_friend_img_01);
						}
					});
				}
			}
		});
		
		sendButton.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				String body = msgView.getEditableText().toString();
				if(imageBarView.getChildCount() == 0){
					if(body.length() == 0) return;
					if(comData.need_picture){
						OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_need_pictrue), OTWritePost.this);
					}else{
						Intent intent = new Intent();
						intent.putExtra("body", body);
						setResult(RESULT_OK, intent);
						finish();
					}
				}else{
					ArrayList<String> imagePaths = new ArrayList<String>();
					for(int i=0;i<imageBarView.getChildCount();++i){
						View view = imageBarView.getChildAt(i);
						String imagePath = (String)view.getTag();
						imagePaths.add(imagePath);
					}
					Intent intent = new Intent();
					intent.putExtra("body", body);
					intent.putStringArrayListExtra("imagePaths", imagePaths);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
		
		switch (startAction) {
		case 0:
			doTakePhotoAction();
			break;
		case 1:
			doTakeAlbumAction();
			break;
		case 2:
			doTakeAlbumActionMultiple(imageBarView.getChildCount());
			break;
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
	}

	@Override
	public void OnImageLoadComplete(List<String> ImagePaths) {
		for(String imagePath : ImagePaths){
			OnImageLoadComplete(imagePath);
		}
	}
}

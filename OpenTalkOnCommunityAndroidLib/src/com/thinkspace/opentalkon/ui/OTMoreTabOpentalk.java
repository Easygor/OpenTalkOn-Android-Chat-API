package com.thinkspace.opentalkon.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTAppInfo;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.ui.helper.PLActivityGroup;

public class OTMoreTabOpentalk extends PLActivityGroup {
	ImageView image;
	TextView name;
	Button market;
	FrameLayout mainLayout;
	OTAppInfo appInfo;
	SimpleBitmapDisplayer displayer = new SimpleBitmapDisplayer();
	
	@Override public void onChangeView(String viewName, Object data) {}

	@Override
	public FrameLayout getFrameLayOut() {
		return mainLayout;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_more_tab_opentalk);		
		
		Intent intent = getIntent();
		if(intent == null || intent.hasExtra("app_info") == false){
			finish();
			return;
		}
		
		appInfo = intent.getParcelableExtra("app_info");
		
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				onBackPressed();
			}
		});
		
		image = (ImageView) findViewById(R.id.oto_ot_more_tab_opentalk_image);
		name = (TextView) findViewById(R.id.oto_ot_more_tab_opentalk_name);
		market = (Button) findViewById(R.id.oto_ot_more_tab_opentalk_go_market);
		mainLayout = (FrameLayout) findViewById(R.id.oto_ot_more_tab_opentalk_layout);
		
		if(appInfo.img_path != null && appInfo.img_path.length() > 0){
			String url = TASatelite.makeCommonImageUrl(appInfo.img_path);
			image.setImageResource(R.drawable.oto_oto_logo);
			OTOApp.getInstance().getImageDownloader().requestImgDownload(url, new TAImageDataHandler() {
				@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
					displayer.display(bitmap, new ImageViewAware(image), null);
				}
				@Override public void onHttpImageException(Exception ex) {
					image.setImageResource(R.drawable.oto_oto_logo);
				}
			});
		}
		name.setText(appInfo.app_name);
		market.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appInfo.package_name));
				marketIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET|Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				startActivity(marketIntent);
			}
		});
		
		Bundle bundle = new Bundle();
		bundle.putLong("app_id", appInfo.id);
		applyPage(OTMainTabOpenTalk.class, bundle);
	}
}

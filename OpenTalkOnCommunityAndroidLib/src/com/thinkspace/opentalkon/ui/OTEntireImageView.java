package com.thinkspace.opentalkon.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.helper.ViewTouchImage;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.ui.helper.RoundedBitmapDisplayer;

public class OTEntireImageView extends Activity{
	ArrayList<String> path;
	int img_back_res;
	
	RoundedBitmapDisplayer roundDisplayer = new RoundedBitmapDisplayer(10);
	SimpleBitmapDisplayer displayer = new SimpleBitmapDisplayer();
	
	ViewPager viewPager;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		
		ArrayList parcelObj = intent.getParcelableArrayListExtra("img_path");
		if(parcelObj == null){
			finish();
			return;
		}
		path = (ArrayList<String>)parcelObj;
		
		int initPos = intent.getIntExtra("img_pos", -1);
		if(initPos == -1){
			finish();
			return;
		}
		if(path.size() == 1){
			View mainView = getLayoutInflater().inflate(R.layout.ot_imageview, null);
			setContentView(mainView);
			new ImageViewHolder(path.get(initPos), mainView);
		}else{
			setContentView(R.layout.ot_imageview_layout);
			viewPager = (ViewPager) findViewById(R.id.oto_oto_imageview_viewpager);
			viewPager.setAdapter(new FullScreenImageAdapter(this, path));
			viewPager.setCurrentItem(initPos);
		}
	}
	
	class ImageViewHolder{
		public View mainView;
		public ViewTouchImage imgView;
		public ProgressBar imgProg;
		public Button saveImage;
		public String imagePath;
		
		public void setProg(boolean enable){
			if(enable){
				imgView.setVisibility(View.GONE);
				imgProg.setVisibility(View.VISIBLE);
			}else{
				imgView.setVisibility(View.VISIBLE);
				imgProg.setVisibility(View.GONE);
			}
		}
		public ImageViewHolder(String imagePath, View mainView){
			this.imagePath = imagePath;
			this.mainView = mainView;
			setupMainView(mainView);
		}
		
		public ImageViewHolder(String imagePath, LayoutInflater inflater){
			this.imagePath = imagePath;
			this.mainView = inflater.inflate(R.layout.ot_imageview, null);
			setupMainView(mainView);
		}
		
		public void setupMainView(final View mainView){
			imgView = (ViewTouchImage) mainView.findViewById(R.id.oto_img_entire_img);
			imgProg = (ProgressBar) mainView.findViewById(R.id.oto_img_entire_prog);
			saveImage = (Button) mainView.findViewById(R.id.oto_img_entire_save_button);
			
			imagePath = TASatelite.makeImageUrl(imagePath.replace("_thumb.png", ""));
			setProg(true);
			OTOApp.getInstance().getImageDownloader().requestImgDownload(imagePath, true, new TAImageDataHandler() {
				@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
					setProg(false);
					displayer.display(bitmap, new ImageViewAware(imgView), null);
				}
				@Override public void onHttpImageException(Exception ex) {
					setProg(false);
				}
			});
			
			saveImage.setOnClickListener(new OnClickListener() {
				@Override public void onClick(View view) {
					if(imgView.getVisibility() != ImageView.VISIBLE) return;
					if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
						OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_wait_moment), OTEntireImageView.this);
						final Handler handler = new Handler(); 
						new Thread(new Runnable() {
							@Override
							public void run() {
								ImageViewToFile(imgView, new saveImageHandler() {
									@Override
									public void onComplete(final boolean complete, final String path) {
										handler.post(new Runnable() {
											@Override
											public void run() {
												OTOApp.getInstance().getUIMgr().dismissDialogProgress();
												if(complete){
													OTOApp.getInstance().getUIMgr().showToast(String.format(getString(R.string.oto_sd_card_save_image_ok), path),
															OTEntireImageView.this);
												}else{
													OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_sd_card_save_image_fail), OTEntireImageView.this);
												}
											}
										});
									}
								});
							}
						}).start();
					}else{
						OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_sd_card_not_found), OTEntireImageView.this);
					}
				}
			});
		}
	}
	
	public class FullScreenImageAdapter extends PagerAdapter {
		 
	    private Activity _activity;
	    private ArrayList<String> _imagePaths;
	    private LayoutInflater inflater;
	 
	    // constructor
	    public FullScreenImageAdapter(Activity activity, ArrayList<String> imagePaths) {
	        this._activity = activity;
	        this._imagePaths = imagePaths;
	        this.inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    }
	 
	    @Override
	    public int getCount() {
	        return this._imagePaths.size();
	    }
	 
	    @Override
	    public boolean isViewFromObject(View view, Object object) {
	        return view == ((RelativeLayout) object);
	    }
	     
	    @Override
	    public Object instantiateItem(ViewGroup container, int position) {
	    	ImageViewHolder imageHolder = new ImageViewHolder(_imagePaths.get(position), inflater);
	        ((ViewPager) container).addView(imageHolder.mainView);
	        return imageHolder.mainView;
	    }
	     
	    @Override
	    public void destroyItem(ViewGroup container, int position, Object object) {
	        ((ViewPager) container).removeView((RelativeLayout) object);
	  
	    }
	}
	
	interface saveImageHandler{
		void onComplete(boolean complete,String path);
	}
	
	public static void ImageViewToFile(ImageView imgView, saveImageHandler endHandler){
		imgView.setDrawingCacheEnabled(true);
		Bitmap bitmap = imgView.getDrawingCache();
		File saveFile = null;
		for(int i=1;;++i){
			String path = String.format(Environment.getExternalStorageDirectory().getAbsolutePath() + "/opentalkon/SavedFile%d.png", i);
			saveFile = new File(path);
			if(saveFile.exists() == false) break; 
		}
		PLEtcUtilMgr.makeDirectoryOnly(saveFile);
		try{
			FileOutputStream os = new FileOutputStream(saveFile);
			bitmap.compress(CompressFormat.PNG, 100, os);
			os.close();
			endHandler.onComplete(true,saveFile.getAbsolutePath());
		}catch(IOException ex){
			ex.printStackTrace();
			endHandler.onComplete(false,null);
		}
	}
}

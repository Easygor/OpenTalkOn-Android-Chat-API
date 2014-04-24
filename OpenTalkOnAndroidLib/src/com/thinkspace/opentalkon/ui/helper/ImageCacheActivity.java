package com.thinkspace.opentalkon.ui.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.satelite.TAImageDataHandler;
import com.thinkspace.opentalkon.satelite.TAImgDownloader.BitmapInfo;

public class ImageCacheActivity extends Activity {
	Map<BitmapInfo, Bitmap> urlCacheMap = OTOApp.getInstance().getImageDownloader().getCacheMap();
	Map<BitmapInfo, Boolean> urlPendingMap = OTOApp.getInstance().getImageDownloader().getPendingMap();
	protected RoundedBitmapDisplayer roundDisplayer = new RoundedBitmapDisplayer(10);
	protected SimpleBitmapDisplayer displayer = new SimpleBitmapDisplayer();
	
	ArrayList<BitmapInfo> localAddedCache = new ArrayList<BitmapInfo>();
	Map<BitmapInfo, Boolean> localAnimDoneCache = new HashMap<BitmapInfo, Boolean>();
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearCacheFromThis();
	}
	
	public void clearCacheFromThis(){
		for(BitmapInfo key : localAddedCache){
			Bitmap map = urlCacheMap.get(key);
			if(map == null) continue;
			urlCacheMap.remove(key);
			map.recycle();
		}
		localAddedCache.clear();
		localAnimDoneCache.clear();
		System.gc();
	}
	
	private boolean drawImageIfCacheHasImage(BitmapInfo bitmapInfo, ImageView imageView, int defaultResource, boolean drawRound){
		ImageViewAware imageViewAware = new ImageViewAware(imageView, false);
		if(urlCacheMap.containsKey(bitmapInfo)){
			Bitmap bitmap = urlCacheMap.get(bitmapInfo);
			if(bitmap.isRecycled() == false){
				if(drawRound){
					roundDisplayer.display(bitmap, imageViewAware, null);
				}else{
					displayer.display(bitmap, imageViewAware, null);
				}
				if(localAnimDoneCache.containsKey(bitmapInfo) == false){
					localAnimDoneCache.put(bitmapInfo, true);
					Animation anim = AnimationUtils.loadAnimation(this, R.anim.oto_fadein);
					imageView.startAnimation(anim);
				}
			}else{
				imageView.setImageResource(defaultResource);
			}
			return true;
		}
		return false;
	}

	public void loadImage(final String imageUrl,final ImageView imageView,final int defaultResource,final boolean drawRound, boolean fullScreen){
		if(imageUrl == null || imageUrl.length() == 0){
			imageView.setImageResource(defaultResource);
			return;
		}
		final BitmapInfo bitmapInfo = new BitmapInfo(imageUrl, fullScreen);
		if(drawImageIfCacheHasImage(bitmapInfo, imageView, defaultResource, drawRound) == false){
			if(urlPendingMap.containsKey(bitmapInfo) == false){
				urlPendingMap.put(bitmapInfo, true);
				imageView.setImageResource(defaultResource);
				OTOApp.getInstance().getImageDownloader().requestImgDownload(imageUrl, fullScreen, new TAImageDataHandler() {
					@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
						urlPendingMap.remove(bitmapInfo);
						urlCacheMap.put(bitmapInfo, bitmap);
						localAddedCache.add(bitmapInfo);
						drawImageIfCacheHasImage(bitmapInfo, imageView, defaultResource, drawRound);
					}
					
					@Override public void onHttpImageException(Exception ex) {
						if(ex != null){
							String message = ex.getMessage();
							if(message != null && message.equals("Image is Null")){
								urlPendingMap.remove(bitmapInfo);
								drawImageIfCacheHasImage(bitmapInfo, imageView, defaultResource, drawRound);
							}
						}
					}
				});
			}
		}
	}
	
	public void loadImageOnList(final String imageUrl, ImageView imageView, int defaultResource, final ArrayAdapter<?> adapter, boolean drawRound, boolean fullScreen){
		if(imageUrl == null || imageUrl.length() == 0){
			imageView.setImageResource(defaultResource);
			return;
		}
		final BitmapInfo bitmapInfo = new BitmapInfo(imageUrl, fullScreen);
		if(drawImageIfCacheHasImage(bitmapInfo, imageView, defaultResource, drawRound) == false){
			if(urlPendingMap.containsKey(bitmapInfo) == false){
				urlPendingMap.put(bitmapInfo, true);
				imageView.setImageResource(defaultResource);
				OTOApp.getInstance().getImageDownloader().requestImgDownload(imageUrl, new TAImageDataHandler() {
					@Override public void onHttpImagePacketReceived(String url, Bitmap bitmap) {
						urlPendingMap.remove(bitmapInfo);
						urlCacheMap.put(bitmapInfo, bitmap);
						localAddedCache.add(bitmapInfo);
						adapter.notifyDataSetChanged();
					}
					
					@Override public void onHttpImageException(Exception ex) {
						if(ex != null){
							String message = ex.getMessage();
							if(message != null && message.equals("Image is Null")){
								urlPendingMap.remove(bitmapInfo);
								adapter.notifyDataSetChanged();
							}
						}
					}
				});
			}
		}
	}
}

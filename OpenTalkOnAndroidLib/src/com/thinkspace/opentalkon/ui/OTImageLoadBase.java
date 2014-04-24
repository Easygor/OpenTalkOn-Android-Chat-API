package com.thinkspace.opentalkon.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.ui.helper.ImageCacheActivity;

public abstract class OTImageLoadBase extends ImageCacheActivity {
	private final static int PICK_FROM_CAMERA = 0;
	private final static int CROP_DONE_FROM_ALBUM = 1;
	private final static int CROP_DONE_FROM_CAMERA = 2;
	public final static int PICK_FROM_ALBUM_MULTIPLE = 3;
	public final static String BASE_SEND_IMG_PATH = "Android/data/com.thinkspace.lolopentalkon/upload/";
	
	ImageLoadHandler imageLoadHandler;
	String sendImagePath;
	
	protected void setImageLoadHandler(ImageLoadHandler imageLoadHandler) {
		this.imageLoadHandler = imageLoadHandler;
	}

	public static interface ImageLoadHandler{
		public void OnImageLoadComplete(String ImagePath);
		public void OnImageLoadComplete(List<String> ImagePaths);
	}
	
	protected OTImageLoadBase() {
		this.sendImagePath = BASE_SEND_IMG_PATH;
	}

	String createSaveCropFile() {
		Uri uri;
		String baseURI = sendImagePath;
		String url = baseURI + String.valueOf(System.currentTimeMillis()) + ".jpg";
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			PLEtcUtilMgr.makeDirectoryOnly(new File(Environment.getExternalStorageDirectory(), url));
			uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
			return uri.getPath();
		}else{
			PLEtcUtilMgr.makeDirectoryOnly(new File(url));
			uri = Uri.fromFile(new File(url));
			return uri.getPath();			
		}
	}
	
	String createCacheImageFile(int idx) {
		Uri uri;
		String baseURI = sendImagePath;
		String url = baseURI + String.valueOf(System.currentTimeMillis() + "_" +String.valueOf(idx));
		
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			PLEtcUtilMgr.makeDirectoryOnly(new File(Environment.getExternalStorageDirectory(), url));
			uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
			return uri.getPath();
		}else{
			PLEtcUtilMgr.makeDirectoryOnly(new File(url));
			uri = Uri.fromFile(new File(url));
			return uri.getPath();			
		}
	}
	
	@SuppressWarnings("unused")
	private Bitmap resizeBitmap(Bitmap resizeImage){
		Bitmap ret = null;
		int height = resizeImage.getHeight();
		int width = resizeImage.getWidth();
		if(resizeImage.getHeight() > 800){
			ret = Bitmap.createScaledBitmap(resizeImage, (width * 800) / height , 800, true);
			resizeImage.recycle();
		}else if(resizeImage.getWidth() > 640){
			ret = Bitmap.createScaledBitmap(resizeImage, 640, (height * 640) / width, true);
			resizeImage.recycle();
		}
		if(ret == null){
			return resizeImage;
		}else{
			resizeImage.recycle();
			return ret;
		}
	}

	protected void doTakeAlbumAction() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
		        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		photoPickerIntent.setType("image/*");
		photoPickerIntent.putExtra("crop", "true");
		photoPickerIntent.putExtra("noFaceDetection", true);  
		String lastCropFilePath = createSaveCropFile();
		OTOApp.getInstance().getPref().getLastCropFilePath().setValue(lastCropFilePath);
		
		photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(lastCropFilePath)));
		startActivityForResult(photoPickerIntent, CROP_DONE_FROM_ALBUM);
	}
	
	protected void doTakeAlbumActionMultiple(int selectedCount) {
		Intent photoPickerIntent = new Intent(this, OTGallery.class);
		photoPickerIntent.putExtra("selectedCount", selectedCount);
		startActivityForResult(photoPickerIntent, PICK_FROM_ALBUM_MULTIPLE);
	}
	
	protected void doTakePhotoAction() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String lastCropFilePath = createSaveCropFile();
		OTOApp.getInstance().getPref().getLastCropFilePath().setValue(lastCropFilePath);
	    
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(lastCropFilePath)));
		startActivityForResult(intent, PICK_FROM_CAMERA);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
		case PICK_FROM_CAMERA: {
				String filePath = OTOApp.getInstance().getPref().getLastCropFilePath().getValue();
				File file = new File(filePath);
				if(file.exists()){
					Intent intent = new Intent("com.android.camera.action.CROP");
					intent.setDataAndType(Uri.fromFile(new File(filePath)), "image/*");
			        intent.putExtra("scale", true);
			        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(OTOApp.getInstance().getPref().getLastCropFilePath().getValue())));
		
					startActivityForResult(intent, CROP_DONE_FROM_CAMERA);
				}
				break;
			}
		case CROP_DONE_FROM_ALBUM:
		case CROP_DONE_FROM_CAMERA:{
			/*
			Bitmap photo = resizeBitmap(BitmapFactory.decodeFile(OTOApp.getInstance().getPref().getLastCropFilePath().getValue()));
			
			File resizeImgFile = new File(OTOApp.getInstance().getPref().getLastCropFilePath().getValue());
			OutputStream out = null;
			try{
				out = new FileOutputStream(resizeImgFile);
				resizeImgFile.createNewFile();
				photo.compress(CompressFormat.PNG, 100, out);
			}catch(IOException ex){
				ex.printStackTrace();
			}finally{
				if(out != null){
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if(imageLoadHandler != null){
				imageLoadHandler.OnImageLoadComplete(OTOApp.getInstance().getPref().getLastCropFilePath().getValue());
			}
			*/
			File imgFile = new File(OTOApp.getInstance().getPref().getLastCropFilePath().getValue());
			if(imgFile.length() <= 1024L*1024L*10L){
				if(imageLoadHandler != null){
					imageLoadHandler.OnImageLoadComplete(OTOApp.getInstance().getPref().getLastCropFilePath().getValue());
				}
			}else{
				OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_img_size_limit), this);
			}
			break;
		}
		case PICK_FROM_ALBUM_MULTIPLE:
			boolean showLimitMessage = false;
			ArrayList<String> ret = data.getStringArrayListExtra("selectedList");
			ArrayList<String> cacheRet = new ArrayList<String>();
			
			for(String path : ret){
				File img = new File(path);
				if(img.length() <= 1024L*1024L*10L){
					cacheRet.add(path);
				}else{
					showLimitMessage = true;
				}
			}
			if(showLimitMessage){
				OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_img_size_limit), this);
			}
			/*
			int idx = 0;
			for(String path : ret){
				Bitmap photo = resizeBitmap(BitmapFactory.decodeFile(path));
				String cachePath = createCacheImageFile(idx++);
				File resizeImgFile = new File(cachePath);
				OutputStream out = null;
				try{
					out = new FileOutputStream(resizeImgFile);
					resizeImgFile.createNewFile();
					photo.compress(CompressFormat.PNG, 100, out);
					cacheRet.add(cachePath);
				}catch(IOException ex){
					ex.printStackTrace();
				}finally{
					if(out != null){
						try {
							out.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			*/
			
			if(imageLoadHandler != null){
				imageLoadHandler.OnImageLoadComplete(cacheRet);
			}
			break;
		}
	}
}

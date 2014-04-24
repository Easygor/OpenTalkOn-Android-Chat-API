package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.satelite.TAImgDownloader;

public class OTGallery extends Activity{
	Map<String, Bitmap> cacheMap = new HashMap<String, Bitmap>();
	Map<String, Boolean> pendingMap = new HashMap<String, Boolean>();
	Map<String, ArrayList<String>> mapImages = new HashMap<String, ArrayList<String>>();
	SimpleBitmapDisplayer displayer = new SimpleBitmapDisplayer();
	
	ListView listView;
	TextView emptyView;
	int selectedCount;
	Handler handler = new Handler();
	ListAdapter adapter;	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		for(Entry<String, Bitmap> entry : cacheMap.entrySet()){
			entry.getValue().recycle();
		}
		cacheMap.clear();
		System.gc();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_gallery_layout);
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		
		Intent intent = getIntent();
		selectedCount = intent.getIntExtra("selectedCount", 0);
		
		listView = (ListView) findViewById(R.id.oto_otgallery_listview);
		emptyView = (TextView) findViewById(R.id.oto_otgallery_empty);
		loadImages();
		
		ArrayList<ImageData> imageDatas = new ArrayList<OTGallery.ImageData>();
		for(Entry<String, ArrayList<String>> imageInfo : mapImages.entrySet()){
			ImageData data = new ImageData();
			data.folderName = imageInfo.getKey();
			data.imagePath = imageInfo.getValue();
			
			String [] splitPath = data.folderName.split("/");
			if(splitPath.length == 0) continue;
			data.showFolderName = splitPath[splitPath.length - 1];
			imageDatas.add(data);
		}
		Collections.sort(imageDatas);
		
		if(imageDatas.size() == 0){
			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}else{
			listView.setVisibility(View.VISIBLE);
			listView.setAdapter(adapter = new ListAdapter(this, -1, imageDatas));
			listView.invalidateViews();
			emptyView.setVisibility(View.GONE);
		}
	}
	
	class ImageData implements Comparable<ImageData>{
		public String showFolderName;
		public String folderName;
		public ArrayList<String> imagePath;
		@Override
		public int compareTo(ImageData another) {
			return showFolderName.compareTo(another.showFolderName);
		}
	}
	
	class ListViewHolder{
		public ViewGroup layout;
		public ImageView image;
		public TextView folderName;
		public TextView folderCount;
		public View mainView;
		public ImageData data;
		
		public ListViewHolder(View mainView){
			this.mainView = mainView;
			layout = (ViewGroup) mainView.findViewById(R.id.oto_otgallery_elem_layout);
			image = (ImageView) mainView.findViewById(R.id.oto_otgallery_elem_image);
			folderName = (TextView) mainView.findViewById(R.id.oto_otgallery_elem_folder_name);
			folderCount = (TextView) mainView.findViewById(R.id.oto_otgallery_elem_folder_count);
			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Intent intent = new Intent(OTGallery.this, OTGallerySelect.class);
					intent.putStringArrayListExtra("imageList", data.imagePath);
					intent.putExtra("showFolderName", data.showFolderName);
					intent.putExtra("selectedCount", selectedCount);
					startActivityForResult(intent, OTImageLoadBase.PICK_FROM_ALBUM_MULTIPLE);
				}
			});
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == OTImageLoadBase.PICK_FROM_ALBUM_MULTIPLE && resultCode == RESULT_OK){
			setResult(resultCode, data);
			finish();
		}
	}

	class ListAdapter extends ArrayAdapter<ImageData>{
		List<ImageData> objects;
		public ListAdapter(Context context, int textViewResourceId, List<ImageData> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ListViewHolder holder;
			if(convertView == null){
				LayoutInflater vi = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.ot_gallery_elem, null);
				holder = new ListViewHolder(convertView);
				convertView.setTag(holder);
			}else{
				holder = (ListViewHolder)convertView.getTag();
			}
			final ImageData nowData = objects.get(position);
			holder.data = nowData;
			holder.folderName.setText(nowData.showFolderName);
			holder.folderCount.setText(String.valueOf(nowData.imagePath.size()));
			
			if(cacheMap.containsKey(nowData.imagePath.get(0))){
				displayer.display(cacheMap.get(nowData.imagePath.get(0)), new ImageViewAware(holder.image), null);
			}else{
				holder.image.setImageResource(R.drawable.oto_friend_img_01);
				if(pendingMap.containsKey(nowData.imagePath.get(0)) == false){
					pendingMap.put(nowData.imagePath.get(0), true);
					OTOApp.getInstance().getImageDownloader().getThreadPool().execute(new Runnable() {
						@Override public void run() {
							Bitmap nMap = TAImgDownloader.decodeBitmapProperly(nowData.imagePath.get(0), false);
							if(nMap != null){
								cacheMap.put(nowData.imagePath.get(0), nMap);
							}else{
								OTOApp.getInstance().getImageDownloader().flushCache();
							}
							handler.post(new Runnable() {
								@Override public void run() {
									adapter.notifyDataSetChanged();
								}
							});
						}
					});
				}
			}
			
			return holder.mainView;
		}
	}
	
	void addImage(String div, String path){
		if(mapImages.containsKey(div) == false){
			mapImages.put(div, new ArrayList<String>());
		}
		
		mapImages.get(div).add(path);
	}
	
	String getFolderName(String path){
		int index = path.lastIndexOf("/");
		return path.substring(0,index);
	}
	
	void loadImages(){
		String[] projection = { MediaStore.Images.Media.DATA};
		
		Cursor cur = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			projection, "", null, MediaStore.Images.Media.DATE_ADDED + " DESC");
		
		try{
			if(cur != null){
				if(cur.moveToFirst()){
					do{
						String path = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.DATA));
						if(path == null || path.length() == 0 || !path.contains("/")) continue;
						addImage(getFolderName(path), path);
					}while(cur.moveToNext());
				}
			}
		}finally {
			if(cur != null){
				cur.moveToLast();
				cur.close();
			}
		}
	}
}

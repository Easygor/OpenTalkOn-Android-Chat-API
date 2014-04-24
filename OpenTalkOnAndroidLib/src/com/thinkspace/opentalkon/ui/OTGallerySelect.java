package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.satelite.TAImgDownloader;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.ui.helper.ImageCacheActivity;

public class OTGallerySelect extends ImageCacheActivity{
	String showFolderName;
	ArrayList<String> imageList;
	boolean isImageViewer;
	boolean isWeb;
	SimpleBitmapDisplayer displayer = new SimpleBitmapDisplayer();
	Map<String, Bitmap> cacheMap = new HashMap<String, Bitmap>();
	Map<String, Boolean> pendingMap = new HashMap<String, Boolean>();
	ArrayList<ImageData> imageDataList = new ArrayList<OTGallerySelect.ImageData>();
	int selectedCount;
	
	TextView title;
	GridView gridView;
	ListAdapter adapter;
	Button done;
	Handler handler = new Handler();
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearAll();
	}
	
	public void clearAll(){
		for(Entry<String, Bitmap> entry : cacheMap.entrySet()){
			entry.getValue().recycle();
		}
		cacheMap.clear();
		System.gc();		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_galleryselect_layout);
		findViewById(R.id.oto_main_cancel_button).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		
		title = (TextView) findViewById(R.id.oto_otgalleryselect_title);
		gridView = (GridView)findViewById(R.id.oto_otgalleryselect_gridview);
		done = (Button)findViewById(R.id.oto_otgalleryselect_done);
		
		Intent intent = getIntent();
		isWeb = intent.getBooleanExtra("isWeb", false);
		isImageViewer = intent.getBooleanExtra("isImageViewer", false);
		selectedCount = intent.getIntExtra("selectedCount", 0);
		
		imageList = intent.getStringArrayListExtra("imageList");
		showFolderName = intent.getStringExtra("showFolderName");
		
		for(String path : imageList){
			ImageData data = new ImageData();
			data.imagePath = path;
			data.selected = false;
			imageDataList.add(data);
		}

		gridView.setAdapter(adapter = new ListAdapter(this, -1, imageDataList));
		if(isImageViewer){
			done.setVisibility(View.GONE);
			title.setText(showFolderName);
		}else{
			setSelectedCount(selectedCount);
			done.setVisibility(View.VISIBLE);
			done.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					ArrayList<String> selectedList = new ArrayList<String>();
					for(ImageData data : imageDataList){
						if(data.selected){
							selectedList.add(data.imagePath);
						}
					}
					Intent intent = new Intent();
					intent.putStringArrayListExtra("selectedList", selectedList);
					setResult(RESULT_OK, intent);
					finish();
				}
			});
		}
	}
	
	void setSelectedCount(int count){
		selectedCount = count;
		title.setText(showFolderName + " " + String.valueOf(count) +"/10");
	}
	
	class ImageData{
		public String imagePath;
		public boolean selected;
	}
	
	class ListViewHolder{
		public ViewGroup layout;
		public ImageView image;
		public View selected;
		public View mainView;
		
		public List<ImageData> objects;
		public int position;
		
		public ListViewHolder(View mainView){
			this.mainView = mainView;
			layout = (ViewGroup) mainView.findViewById(R.id.oto_otgalleryselect_elem_layout);
			image = (ImageView) mainView.findViewById(R.id.oto_otgalleryselect_elem_image);
			selected = mainView.findViewById(R.id.oto_otgalleryselect_elem_selected);
			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					if(isImageViewer){
						Intent intent = new Intent(OTGallerySelect.this, OTEntireImageView.class);
						ArrayList<String> arr = new ArrayList<String>();
						for(ImageData imgData : objects){
							arr.add(imgData.imagePath);
						}
						intent.putExtra("img_path", arr);
						intent.putExtra("img_pos", position);
						startActivity(intent);
					}else{
						ImageData nowData = objects.get(position);
						if(selectedCount == 10 && !nowData.selected) return;
						
						nowData.selected = !nowData.selected;
						if(nowData.selected){
							setSelectedCount(selectedCount + 1);
							selected.setVisibility(View.VISIBLE);
						}else{
							setSelectedCount(selectedCount - 1);
							selected.setVisibility(View.GONE);
						}
						layout.invalidate();
					}
				}
			});
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
				convertView = vi.inflate(R.layout.ot_galleryselect_elem, null);
				holder = new ListViewHolder(convertView);
				convertView.setTag(holder);
			}else{
				holder = (ListViewHolder)convertView.getTag();
			}
			final ImageData nowData = objects.get(position);
			holder.objects = objects;
			holder.position = position;
			
			if(isWeb){
				String url = TASatelite.makeImageUrl(nowData.imagePath);
				loadImageOnList(url, holder.image, R.drawable.oto_friend_img_01, this, false, false);
			}else{
				if(cacheMap.containsKey(nowData.imagePath)){
					displayer.display(cacheMap.get(nowData.imagePath), new ImageViewAware(holder.image), null);
				}else{
					holder.image.setImageResource(R.drawable.oto_friend_img_01);
					if(pendingMap.containsKey(nowData.imagePath) == false){
						pendingMap.put(nowData.imagePath, true);
						OTOApp.getInstance().getImageDownloader().getThreadPool().execute(new Runnable() {
							@Override public void run() {
								Bitmap nMap = TAImgDownloader.decodeBitmapProperly(nowData.imagePath, false);
								if(nMap != null){
									cacheMap.put(nowData.imagePath, nMap);
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
			}
			
			if(isImageViewer == false){
				if(nowData.selected){
					holder.selected.setVisibility(View.VISIBLE);
				}else{
					holder.selected.setVisibility(View.GONE);
				}
			}else{
				holder.selected.setVisibility(View.GONE);
			}
			
			return holder.mainView;
		}
	}
}

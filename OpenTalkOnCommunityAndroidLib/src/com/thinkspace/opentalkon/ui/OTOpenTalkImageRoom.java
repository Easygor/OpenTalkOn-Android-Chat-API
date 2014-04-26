package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.OTComMsg;
import com.thinkspace.opentalkon.satelite.TASatelite;

public class OTOpenTalkImageRoom extends OTOpenTalkRoom {
	ArrayList<ImageListItem> imageItems = new ArrayList<ImageListItem>();
	ListAdapter imageAdapter;
	
	public void onImageClick(ImageListItem item){
		Intent intent = new Intent(this, OTOpenTalkDetail.class);
		intent.putExtra("post_id", item.msg.getId());
		intent.putExtra("authority", authority);
		intent.putExtra("imagePos", item.pos);
		startActivityForResult(intent, OT_CHECK_IS_DELETED);
	}
	
	@Override
	public void onMsgListNotifyDataSetChanged() {
		imageAdapter.notifyDataSetChanged();
	}

	@Override public void convListScrollToBottom() {}
	
	@Override
	public void removeListElem(long msg_id) {
		ArrayList<ImageListItem> removeItem = new ArrayList<ImageListItem>();
		for(ImageListItem item : imageItems){
			if(item.msg.getId().equals(msg_id)){
				removeItem.add(item);
			}
		}
		for(ImageListItem rItem : removeItem){
			imageItems.remove(rItem);
		}
		onMsgListNotifyDataSetChanged();
	}

	@Override
	void addMsgData(List<ListElem> preElemlist, OTComMsg newMsg, boolean sendMessage) {
		if((newMsg.getImg_url() == null || newMsg.getImg_url().length() == 0) &&
			(newMsg.getPreSendImg_url() == null || newMsg.getPreSendImg_url().length() == 0))return;
		
		if(newMsg.getImg_url() != null){
			for(int j=0;j<newMsg.getImg_url().length();++j){
				ImageListItem item = new ImageListItem();
				item.msg = newMsg;
				item.pos = j;
				imageItems.add(0, item);
			}
		}else{
			for(int j=0;j<newMsg.getPreSendImg_url().length();++j){
				ImageListItem item = new ImageListItem();
				item.msg = newMsg;
				item.pos = j;
				imageItems.add(0, item);
			}
		}
		
		onMsgListNotifyDataSetChanged();
	}

	@Override
	protected void onSetupMainAdapter() {
		gridView.setOnScrollListener(new ConvScrollListener(true));
		gridView.setAdapter(imageAdapter = new ListAdapter(this, -1, imageItems));
	}
	

	@Override
	protected void onApplyNoneTypeMsgData(JSONArray postList, AtomicBoolean allLoaded, AtomicLong lastMsgId) throws JSONException {
		if(allLoaded != null){
			if(postList.length() == 0){
				allLoaded.set(true);
				return;
			}
		}
		
		if(OTOApp.getInstance().getDB().beginTransaction()){
			for(int i=0;i<postList.length();++i){
				OTComMsg msg = parseComMsg(postList.getJSONObject(i));
				lastMsgTimeCheckWithNoTransaction(msg.getTime());
				if(msg.getImg_url() == null || msg.getImg_url().length() == 0) continue;
				for(int j=0;j<msg.getImg_url().length();++j){
					ImageListItem item = new ImageListItem();
					item.msg = msg;
					item.pos = j;
					imageItems.add(item);
				}
			}
			OTOApp.getInstance().getDB().endTransaction();
		}
		
		if(lastMsgId != null){
			ImageListItem elem = imageItems.get(imageItems.size() - 1);
			if(elem.msg != null){
				lastMsgId.set(elem.msg.getId());
			}
		}
		
		onMsgListNotifyDataSetChanged();
	}

	@Override
	void doSetListUpper(UpperBodyStage stage) {
		switch(stage){
		case CONV_LIST:
			convListViewLayout.setVisibility(View.VISIBLE);
			gridView.setVisibility(View.VISIBLE);
			convListView.setVisibility(View.GONE);
			searchListView.setVisibility(View.GONE);
			progressView.setVisibility(View.GONE);
			convEmptyView.setVisibility(View.GONE);
			return;
		}
		super.doSetListUpper(stage);
	}
	
	class ImageListItem{
		public OTComMsg msg;
		public int pos;
		public String getImagePath(){
			try {
				if(msg.getImg_url() == null || msg.getImg_url().length() == 0){
					return msg.getPreSendImg_url().getString(pos);
				}else{
					return msg.getImg_url().getString(pos);
				}
			} catch (JSONException e) {
				return "";
			}
		}
	}
	
	class ListViewHolder{
		public ViewGroup layout;
		public ImageView image;
		public View selected;
		public View mainView;
		public ImageListItem listItem;
		
		public ListViewHolder(View mainView){
			this.mainView = mainView;
			layout = (ViewGroup) mainView.findViewById(R.id.oto_otgalleryselect_elem_layout);
			image = (ImageView) mainView.findViewById(R.id.oto_otgalleryselect_elem_image);
			selected = mainView.findViewById(R.id.oto_otgalleryselect_elem_selected);
			layout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					onImageClick(listItem);
				}
			});
		}
	}
	
	class ListAdapter extends ArrayAdapter<ImageListItem>{
		List<ImageListItem> objects;
		public ListAdapter(Context context, int textViewResourceId, List<ImageListItem> objects) {
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
			ImageListItem nowData = objects.get(position);
			holder.listItem = nowData;
			
			String url = TASatelite.makeImageUrl(nowData.getImagePath());
			loadImageOnList(url, holder.image, R.drawable.oto_friend_img_01, this, false, false);
			
			holder.selected.setVisibility(View.GONE);
			
			return holder.mainView;
		}
	}
}

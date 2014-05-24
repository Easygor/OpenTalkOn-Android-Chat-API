package com.thinkspace.opentalkon.ui;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.thinkspace.opentalkon.R;

public class OTVersion extends Activity {
	
	public ArrayList<String> getHaveIntent(Intent intent){
        ArrayList<String> ret = new ArrayList<String>();
        Iterator<ResolveInfo> obj = getPackageManager().queryIntentActivities(intent, 0).iterator();
        if(obj != null){
	        while(obj.hasNext()){
	        	ResolveInfo resolveinfo = (ResolveInfo)obj.next();
	        	if(resolveinfo.activityInfo.packageName != null){
	    			String packName = resolveinfo.activityInfo.packageName;
	    			ret.add(packName);
	        	}
	        }
        }
        return ret;
	}
	
	public Intent getMailIntent(){
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.oto_info_mail)});
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.oto_email_title));
        sendIntent.setType("text/plain");
        return sendIntent;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_version_info);
		
		findViewById(R.id.oto_version_mail).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		         try{
		        	 Intent sendIntent = getMailIntent();
		             ArrayList<String> lst = getHaveIntent(sendIntent);
		             boolean gmailEnable = false;
		             for(String now : lst){
		            	 if(now.equalsIgnoreCase("com.google.android.gm")){
		            		 gmailEnable = true;
		            		 break;
		            	 }
		             }
		             if(gmailEnable){
		            	 startActivity(sendIntent.setPackage("com.google.android.gm"));
		             }else{
		            	 startActivity(sendIntent);
		             }
		         }catch(Exception ex){}
			}
		});
		
		findViewById(R.id.oto_version_homepage).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.oto_info_homepage_address)));
				startActivity(intent);
			}
		});
		
		findViewById(R.id.oto_version_facebook).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i;
				try {
					getPackageManager().getPackageInfo("com.facebook.katana", 0);
					i = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/" + getString(R.string.oto_info_facebook_page_id)));
				} catch (Exception e) {
					i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/pages/OpenTalkOn/" + getString(R.string.oto_info_facebook_page_id)));
				}
				startActivity(i);
			}
		});
		
		findViewById(R.id.oto_version_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
}

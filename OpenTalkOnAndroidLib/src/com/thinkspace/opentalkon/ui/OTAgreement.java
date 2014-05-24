package com.thinkspace.opentalkon.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.thinkspace.opentalkon.R;

public class OTAgreement extends Activity {
	TextView body;
	
	public static String readRawTextFile(Context ctx, int resId)
	{
	    InputStream inputStream = ctx.getResources().openRawResource(resId);

	    InputStreamReader inputreader = new InputStreamReader(inputStream);
	    BufferedReader buffreader = new BufferedReader(inputreader);
	    String line;
	    StringBuilder text = new StringBuilder();

	    try {
	        while (( line = buffreader.readLine()) != null) {
	            text.append(line);
	            text.append('\n');
	        }
	    } catch (IOException e) {
	        return null;
	    }
	    return text.toString();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_agreement);
		
		findViewById(R.id.oto_agreement_confirm).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View arg0) {
				finish();
			}
		});
		
		Intent intent = getIntent();
		if(intent == null){
			finish();
			return;
		}
		int type = intent.getIntExtra("type", 0);
		body = (TextView) findViewById(R.id.oto_agreement_body);
		if(type == 0){
			String text = readRawTextFile(this, R.raw.oto_agreement1);
			body.setText(text);
		}else{
			String text = readRawTextFile(this, R.raw.oto_agreement2);
			body.setText(text);
		}
	}
}

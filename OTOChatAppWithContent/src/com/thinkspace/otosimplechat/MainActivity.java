package com.thinkspace.otosimplechat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.thinkspace.opentalkon.lib.ClientInterface;

public class MainActivity extends Activity {
	
	@Override
	protected void onDestroy() {
		ClientInterface.onDestroySlidingMenu(this);
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ClientInterface.onResumeSlidingMenu(this);
	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ClientInterface.onCreateSlidingMenu(this);
		
		findViewById(R.id.oto_main_menu).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClientInterface.showSlidingMenu(MainActivity.this);
			}
		});
	}
	
}

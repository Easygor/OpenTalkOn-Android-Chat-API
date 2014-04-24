package com.thinkspace.otochatapp;

import com.thinkspace.opentalkon.lib.ClientInterface;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ClientInterface.startOpenTalkOnMain(this, true);
		finish();
	}
}

package com.thinkspace.otochatapp;
import com.thinkspace.opentalkon.lib.ClientInterface;

import android.app.Application;


public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		ClientInterface.InitLibrary(this, "YzNkzWTU9ChvZU/kXtHoTYfLtJXA6CbEDvjC8gZjTFg=", null);
	}
}

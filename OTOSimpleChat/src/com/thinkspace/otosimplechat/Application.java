package com.thinkspace.otosimplechat;

import com.thinkspace.opentalkon.lib.ClientInterface;

public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();
		ClientInterface.InitLibrary(this, "zHt7fpea8iUBFmH8vclzixsZafZaw6Sgkr2s/D7/Le4=");
	}
}

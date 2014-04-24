package com.thinkspace.opentalkon;

import java.util.Locale;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.data.RegsiterData;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher;
import com.thinkspace.opentalkon.satelite.TASateliteDispatcher.DispatchedData;
import com.thinkspace.opentalkon.R;

public class OTPushService extends Service{
	boolean userPress;
	
	public interface FinishListener{
		public void onDone();
		public void onFail();
	}
	
	public PendingIntent getAlarmIntent(){
		Intent intent = new Intent(this, OTPushService.class);
		intent.putExtra("alarm", true);
		PendingIntent pIntent = PendingIntent.getService(this, 0, new Intent(this, OTPushService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		return pIntent;
	}
	
	public void setupAlarm(){
		AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		alarmManager.cancel(getAlarmIntent());
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + Long.valueOf(getString(R.string.oto_broker_service_restart_interval)), getAlarmIntent());
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (OTOApp.getInstance().getPushClient() != null) {
			OTOApp.getInstance().getPushClient().stopTokenClient();
		}
	}
	
	public void startTokenClient(){
		long lastLoginFailTime = OTOApp.getInstance().getPref().getLastPushLoginFailTime().getValue();
		if(lastLoginFailTime == -1L || (lastLoginFailTime != -1L && System.currentTimeMillis() - lastLoginFailTime >= Long.valueOf(getString(R.string.oto_push_service_login_fail_reconnect_interval)))){
			Thread restartTokenClientThread = new Thread(new Runnable() {
				@Override
				public void run() {
					OTOApp.getInstance().getPushClient().restartTokenClient();
				}
			});
			restartTokenClientThread.setName("restartTokenClientThread");
			restartTokenClientThread.start();
		}
	}
	
	public void getValidToken(final FinishListener listener){
		String uniqueKey = PLEtcUtilMgr.getUniqueKeyWithSha();
		String locale = Locale.getDefault().getCountry();
		new TASatelite(new TADataHandler() {
			@Override public void onTokenIsNotValid(JSONObject data) {}
			@Override public void onLimitMaxUser(JSONObject data) { }
			
			@Override
			public void onHttpPacketReceived(JSONObject data) {
				DispatchedData dData = TASateliteDispatcher.dispatchSateliteData(data);
				if(dData.hasLocation(TASatelite.getName(TASatelite.REGISTER_URL))){
					Intent intent = new Intent("com.thinkspace.opentalkon.intent.action.GET_VALID_TOKEN_IS_DONE");
					intent.putExtra("packageName", getPackageName());
					String state = dData.getState();
					if(dData.isOK()){
						RegsiterData rData = (RegsiterData)dData.getData();
						OTOApp.getInstance().getPref().getLastPushLoginFailTime().setValue(-1L);
						OTOApp.getInstance().getPref().getToken().setValue(rData.token);
						OTOApp.getInstance().getPref().getUser_id().setValue(rData.user_id);
						OTOApp.getInstance().getPref().getAppCode().setValue(rData.app_code.intValue());
						OTOApp.getInstance().getPref().getAgree_term().setValue(rData.agree_term);
						if(rData.set_nick_name){
							OTOApp.getInstance().getPref().getNickName().setValue(rData.nick_name);
							listener.onDone();
						}else{
							OTOApp.getInstance().getPref().getNickName().setValue("");
							listener.onFail();
						}
					}else{
						if(state.equals("app_token is not valid")){ 
							listener.onFail();
							if(userPress){
								OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_app_token_is_not_valid), OTPushService.this);
							}
						}else if(state.equals("app_is_deleted")){
							if(userPress){
								OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_app_deleted), OTPushService.this);
							}
						}
					}
					intent.putExtra("result", state);
					sendBroadcast(intent);
				}
			}
			
			@Override
			public void onHttpException(Exception ex, TAMultiData data, String addr) {
				listener.onFail();
			}
			
			@Override
			public void onHttpException(Exception ex, JSONObject data, String addr) {
				listener.onFail();
			}
		}).doRegister(uniqueKey, locale, OTOApp.getInstance().getVersionCode(), OTOApp.getInstance().getPref().getAppToken().getValue());
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null){
			userPress = intent.getBooleanExtra("userPress", false);
		}
		if (OTOApp.getInstance().getToken().length() == 0) {
			getValidToken(new FinishListener() {
				@Override public void onFail() {}
				@Override public void onDone() {
					startTokenClient();
				}
			});
		}else{
			startTokenClient();
		}
		setupAlarm();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override public IBinder onBind(Intent intent) { return null;}
}

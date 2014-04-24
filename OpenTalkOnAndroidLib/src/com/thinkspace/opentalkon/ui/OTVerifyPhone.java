package com.thinkspace.opentalkon.ui;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.thinkspace.common.util.PLEtcUtilMgr;
import com.thinkspace.opentalkon.OTOApp;
import com.thinkspace.opentalkon.R;
import com.thinkspace.opentalkon.data.RegsiterData;
import com.thinkspace.opentalkon.data.TAMultiData;
import com.thinkspace.opentalkon.data.TAUserNick;
import com.thinkspace.opentalkon.lib.ClientInterface;
import com.thinkspace.opentalkon.satelite.TADataHandler;
import com.thinkspace.opentalkon.satelite.TASatelite;

public class OTVerifyPhone extends Activity implements TADataHandler {
	EditText phoneNumber;
	EditText verifyNumber;
	Button verifyButton;
	
	certReceiver smsRecv;
	TASatelite satelite;
	
	boolean aCheck;
	ImageView agreeCheck;
	
	@Override
	public void onHttpPacketReceived(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		try{
			String state = data.getString("state");
			String location = data.getString("location");
			if(TASatelite.SEND_VERIFY_NUMBER.endsWith(location)){
				if(state.equals("ok")){
					verifyButton.setEnabled(true);
					OTOApp.getInstance().getUIMgr().showToast(getString(R.string.oto_send_verify_ok), OTVerifyPhone.this);
				}
			}else if(TASatelite.APPLY_VERIFY_NUMBER.endsWith(location)){
				if(state.equals("ok")){
					JSONObject retData = data.getJSONObject("data");
					RegsiterData regData = new RegsiterData(retData);
					OTOApp.getInstance().getPref().getLastPushLoginFailTime().setValue(-1L);
					OTOApp.getInstance().getPref().getToken().setValue(regData.token);
					OTOApp.getInstance().getPref().getUser_id().setValue(regData.user_id);
					OTOApp.getInstance().getPref().getAppCode().setValue(regData.app_code.intValue());
					OTOApp.getInstance().getPref().getAgree_term().setValue(regData.agree_term);
					OTOApp.getInstance().getPref().getNickName().setValue(regData.nick_name);
					
					OTOApp.getInstance().startPushService(false);
					
					JSONArray contacts = readContacts();
					Map<String, String> conMap = new HashMap<String, String>();
					for(int i=0;i<contacts.length();++i){
						JSONObject contact = contacts.getJSONObject(i);
						String name = contact.getString("name");
						String phone_number = contact.getString("phone_number");
						conMap.put(phone_number, name);
					}
					JSONArray appliedContact = retData.getJSONArray("appliedContact");
					OTOApp.getInstance().getDB().beginTransaction();
					for(int i=0;i<appliedContact.length();++i){
						JSONObject contact = appliedContact.getJSONObject(i);
						long user_id = contact.getLong("id");
						String number = contact.getString("phone_number");
						String conName = conMap.get(number);
						TAUserNick.getInstance().insertWithBeginTransaction(user_id, null, conName);
					}
					OTOApp.getInstance().getDB().endTransaction();
					OTOApp.getInstance().getPref().getVerifiedPhoneNumber().setValue(phoneNumber.getText().toString());
					ClientInterface.startOpenTalkOnMain(this, true);
					finish();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	@Override
	public void onTokenIsNotValid(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onLimitMaxUser(JSONObject data) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}

	@Override
	public void onHttpException(Exception ex, JSONObject data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
		if(TASatelite.SEND_VERIFY_NUMBER.equals(addr)){
			verifyButton.setEnabled(true);
		}
	}

	@Override
	public void onHttpException(Exception ex, TAMultiData data, String addr) {
		OTOApp.getInstance().getUIMgr().dismissDialogProgress();
	}
	
	public class smsData{
		public String mAddress;
		public String mBody;
		
		public smsData(){
			mAddress = "";
			mBody = "";
		}
		
		public void dispatchSms(SmsMessage Sms){
			if(mAddress.equals("")){
				mAddress = Sms.getDisplayOriginatingAddress();
				if(mAddress == null)mAddress = "";
				if(mAddress.contains(";")){
					String [] spt = mAddress.split(";");
					if(spt == null || spt.length == 0)
						mAddress = "";
					else
						mAddress = mAddress.split(";")[0];
				}
			}
			mBody += Sms.getDisplayMessageBody();
		}
	}

	
	public class certReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent == null) return;
			if(intent.getExtras() == null) return;
			Object[] pdusObj = (Object[])intent.getExtras().get("pdus");
			if(pdusObj == null) return;
			smsData sms = new smsData();
			for(Object pdu : pdusObj)
				sms.dispatchSms(SmsMessage.createFromPdu((byte[])pdu));
			
			if(sms.mAddress.equals("0")){
				String number = "";
				for(int i=0;i<sms.mBody.length();++i){
					char ch = sms.mBody.charAt(i);
					if(ch >= '0' && ch <= '9'){
						number += ch;
					}
				}
				
				if(verifyNumber != null){
					verifyNumber.setText(number);
				}
				if(verifyButton != null){
					verifyButton.setEnabled(false);
				}
			}
		}
	}
	
	public JSONArray readContacts(){
		JSONArray contacts = new JSONArray();
		ContentResolver cr = getContentResolver();
		String [] selection = new String[]{ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.HAS_PHONE_NUMBER};
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,selection, null, null, null);

		if (cur.getCount() > 0) {
			while (cur.moveToNext()) {
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
					System.out.println("name : " + name + ", ID : " + id);

					// get the phone number
					Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
							new String[]{id}, null);
					while (pCur.moveToNext()) {
						String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						if(phone == null) continue;
						phone = phone.replace("+82", "0");
						phone = phone.replaceAll("-", "");
						JSONObject contactInfo = new JSONObject();
						try {
							contactInfo.put("name", name);
							contactInfo.put("phone_number", phone);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						contacts.put(contactInfo);
					}
					pCur.close();
				}
			}
		}
		cur.close();
		return contacts;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ot_verify_phone);
		
		phoneNumber = (EditText) findViewById(R.id.oto_verify_phone_number);
		verifyNumber = (EditText) findViewById(R.id.oto_verify_number);
		agreeCheck = (ImageView) findViewById(R.id.oto_verify_agreement_check);
		satelite = new TASatelite(this);
		
		agreeCheck.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(aCheck){
					aCheck = false;
					agreeCheck.setImageResource(R.drawable.oto_check_n);
				}else{
					aCheck = true;
					agreeCheck.setImageResource(R.drawable.oto_check_s);
				}
			}
		});
		
		findViewById(R.id.oto_verify_agreement1).setOnClickListener(new OnClickListener() {			
			@Override public void onClick(View v) {
				startActivity(new Intent(OTVerifyPhone.this, OTAgreement.class).putExtra("type", 0));
			}
		});
		
		findViewById(R.id.oto_verify_agreement2).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				startActivity(new Intent(OTVerifyPhone.this, OTAgreement.class).putExtra("type", 1));
			}
		});
		
		smsRecv = new certReceiver();
		IntentFilter iff = new IntentFilter();
		iff.addAction("android.provider.Telephony.SMS_RECEIVED");
		registerReceiver(smsRecv , iff);
		
		String phoneNumberTxt = PLEtcUtilMgr.getPhoneNumber(this);
		if(phoneNumberTxt != null){
			phoneNumber.setText(phoneNumberTxt);
		}
		
		verifyButton = (Button) findViewById(R.id.oto_verify_send);
		verifyButton.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(phoneNumber.length() > 0){
					verifyButton.setEnabled(false);
					satelite.doSendVerifyNumber(phoneNumber.getText().toString());
					OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTVerifyPhone.this);
				}else{
					OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTVerifyPhone.this, getString(R.string.oto_verify), getString(R.string.oto_insert_phone_number));
				}
			}
		});
		
		findViewById(R.id.oto_verify_confirm).setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				if(aCheck == false){
					OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTVerifyPhone.this, getString(R.string.oto_terms_of_use), getString(R.string.oto_agreement_agree_check));
					return;
				}
				if(verifyNumber.length() > 0){
					new Thread(new Runnable() {
						@Override
						public void run() {
							satelite.doApplyVerifyNumber(
								OTOApp.getInstance().getToken(),
								phoneNumber.getText().toString(),
								verifyNumber.getText().toString(),
								readContacts());
						}
					}).start();
					OTOApp.getInstance().getUIMgr().makeDialogProgress(getString(R.string.oto_networking), OTVerifyPhone.this);
				}else{
					OTOApp.getInstance().getDialogMaker().makeAlertDialog(OTVerifyPhone.this, getString(R.string.oto_verify), getString(R.string.oto_insert_verify_number));
				}
			}
		});
	}

}

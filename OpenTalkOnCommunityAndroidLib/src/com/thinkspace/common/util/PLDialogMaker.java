package com.thinkspace.common.util;

import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;

public class PLDialogMaker implements DialogInterface.OnClickListener, OnKeyListener{

	public static final int TYPE_YESNO = 0;
	public static final int TYPE_OK = 1;
	
	Map<DialogInterface, Pair<Integer, PLDialogListener>> listenerDic =
		new HashMap<DialogInterface, Pair<Integer, PLDialogListener>>();
	
	Map<DialogInterface, Object> dataDic =
			new HashMap<DialogInterface, Object>();
	
	Map<DialogInterface, Pair<Integer, PLDialogListenerEx>> listenerDicEx =
		new HashMap<DialogInterface, Pair<Integer, PLDialogListenerEx>>();
	
	Map<DialogInterface, View> viewInflated = new HashMap<DialogInterface, View>();
	Map<Integer,Boolean> DialogId = new HashMap<Integer, Boolean>();

	public void makeYesNoDialog(String Title, String Body,PLDialogListener listner, 
			Context context, int DialogId, Object data){
		DialogInterface now = new AlertDialog.Builder(context)
		.setMessage(Body)
		.setTitle(Title)
		.setPositiveButton(context.getString(com.thinkspace.opentalkon.R.string.oto_yes), this)
		.setNegativeButton(context.getString(com.thinkspace.opentalkon.R.string.oto_no), this)
		.setOnKeyListener(this)
		.setCancelable(false)
		.show();
		
		addListenerDic(now,DialogId,listner,data);
	}
	
	public void makeReSendDialog(String Title, String Body,PLDialogListener listner, 
			Context context, int DialogId, Long t_id){
		DialogInterface now = new AlertDialog.Builder(context)
		.setMessage(Body)
		.setTitle(Title)
		.setPositiveButton(context.getString(com.thinkspace.opentalkon.R.string.oto_message_re_send), this)
		.setNegativeButton(context.getString(com.thinkspace.opentalkon.R.string.oto_delete), this)
		.setOnKeyListener(this)
		.setCancelable(true)
		.show();
		
		addListenerDic(now,DialogId,listner, t_id);
	}
	
	public void makeYesNoDialog(String Title, String Body,PLDialogListener listner, 
			Context context, int DialogId){
		DialogInterface now = new AlertDialog.Builder(context)
		.setMessage(Body)
		.setTitle(Title)
		.setPositiveButton(context.getString(com.thinkspace.opentalkon.R.string.oto_yes), this)
		.setNegativeButton(context.getString(com.thinkspace.opentalkon.R.string.oto_no), this)
		.setOnKeyListener(this)
		.setCancelable(true)
		.show();
		
		addListenerDic(now,DialogId,listner);
	}
	
	public void makeAlertDialog(Context context, String title, String Body){
		try{
			new AlertDialog.Builder(context)
			.setTitle(title)
			.setMessage(Body)
			.setNeutralButton(context.getString(com.thinkspace.opentalkon.R.string.oto_confirm), this)
			.setOnKeyListener(this)
			.setCancelable(false)
			.show();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void makeAlertDialog(Context context, String title, String Body, PLDialogListener listner, int DialogId){
		DialogInterface now = null;
		try{
			now = new AlertDialog.Builder(context)
			.setTitle(title)
			.setMessage(Body)
			.setNeutralButton(context.getString(com.thinkspace.opentalkon.R.string.oto_confirm), this)
			.setOnKeyListener(this)
			.setCancelable(false)
			.show();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		if(now != null){
			addListenerDic(now,DialogId,listner);
		}
	}
	
	public void addListenerDic(DialogInterface now, Integer DialogId, PLDialogListener listner, Object data){
		listenerDic.put(now,new Pair<Integer,PLDialogListener>(DialogId,listner));
		dataDic.put(now, data);
		this.DialogId.put(DialogId, true);
	}
	
	public void addListenerDic(DialogInterface now, Integer DialogId, PLDialogListener listner){
		listenerDic.put(now,new Pair<Integer,PLDialogListener>(DialogId,listner));
		this.DialogId.put(DialogId, true);
	}
	
	public void addListenerDicEx(DialogInterface now, Integer DialogId, PLDialogListenerEx listner){
		listenerDicEx.put(now,new Pair<Integer,PLDialogListenerEx>(DialogId,listner));
		this.DialogId.put(DialogId, true);
	}
	public void onDicClick(DialogInterface dialog, int which){
		if(viewInflated.containsKey(dialog)){
			listenerDic.get(dialog).second.onWithViewDialogSelected(listenerDic.get(dialog).first, which,viewInflated.get(dialog));
			viewInflated.remove(dialog);
		}else{
			if(dataDic.containsKey(dialog)){
				listenerDic.get(dialog).second.onDialogSelectedWithData(listenerDic.get(dialog).first, which, dataDic.get(dialog));
			}else{
				listenerDic.get(dialog).second.onDialogSelected(listenerDic.get(dialog).first, which);
			}
		}
		DialogId.remove(listenerDic.get(dialog).first);
		listenerDic.remove(dialog);
	}
	
	public void onDicExClick(DialogInterface dialog, int which){
		if(viewInflated.containsKey(dialog)){
			listenerDicEx.get(dialog).second.onWithViewDialogSelected(listenerDicEx.get(dialog).first, which,viewInflated.get(dialog));
			viewInflated.remove(dialog);
		}else{
			listenerDicEx.get(dialog).second.onDialogSelected(listenerDicEx.get(dialog).first, which);
		}
		DialogId.remove(listenerDicEx.get(dialog).first);
		listenerDicEx.remove(dialog);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		if(listenerDic.containsKey(dialog)) onDicClick(dialog, which);
		if(listenerDicEx.containsKey(dialog)) onDicExClick(dialog, which);
	}
	
	public void onDialogViewClicked(DialogInterface dialog, View view) {
		if(listenerDicEx.containsKey(dialog) == false) return;
		listenerDicEx.get(dialog).second.onDialogViewClicked(listenerDicEx.get(dialog).first, view);
	}
	
	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_SEARCH){
			onClick(dialog,DialogInterface.BUTTON_NEGATIVE);
		}
		return false;
	}
}

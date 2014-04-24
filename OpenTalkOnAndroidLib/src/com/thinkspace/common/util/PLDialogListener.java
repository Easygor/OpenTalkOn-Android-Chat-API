package com.thinkspace.common.util;

import android.view.View;

public interface PLDialogListener {
	public void onDialogSelected(int dialogId, int pos);
	public void onDialogSelectedWithData(int dialogId, int pos, Object data);
	public void onWithViewDialogSelected(int dialogId, int pos, View bodyView);
}

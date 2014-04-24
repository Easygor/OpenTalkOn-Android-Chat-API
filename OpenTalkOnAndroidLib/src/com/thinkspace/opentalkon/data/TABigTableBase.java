package com.thinkspace.opentalkon.data;

import android.os.Parcelable;

public abstract class TABigTableBase implements Parcelable{
	int TableIdx;

	public int getTableIdx() {
		return TableIdx;
	}

	public void setTableIdx(int tableIdx) {
		TableIdx = tableIdx;
	}
}

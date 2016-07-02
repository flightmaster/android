package com.gpsaviator.android;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class MultiToggleButton extends Button {

	private String[] labels;
	private int value = 0;

	public MultiToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MultiToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public int getValue() {
		return value;
	}

	public void handleClick() {
		if (++value == labels.length) {
			value = 0;
		}
		this.setText(labels[value]);
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public void setValue(int v) {
		value = v;
		this.setText(labels[value]);
	}
}

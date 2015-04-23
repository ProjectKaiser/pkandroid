package com.projectkaiser.app_android;

public class DrawerItemData {
	private String mText;
	private int mColor;
	
	public String getText(){
		return mText;
	}
	public int getColor(){
		return mColor;
	}
	public void setText(String value){
		mText = value;
	}
	public void setColor(int value){
		mColor = value;
	}
}

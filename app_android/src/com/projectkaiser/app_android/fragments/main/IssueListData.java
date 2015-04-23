package com.projectkaiser.app_android.fragments.main;

public class IssueListData {
	private int itemNumber;
	private boolean bActiveIssue = true;
	
	public void setItemNumber(int value){
		itemNumber = value;
	}

	public int getItemNumber(){
		return itemNumber;
	}
	
	public void setActiveIssue(boolean value){
		bActiveIssue = value;
	}
	
	public boolean getActiveIssue(){
		return bActiveIssue;
	}
	
}

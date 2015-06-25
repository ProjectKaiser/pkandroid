package com.projectkaiser.app_android.fragments.main;

public class IssueListData {
	private int itemNumber;
	private int iActiveIssue = 0;
	
	public void setItemNumber(int value){
		itemNumber = value;
	}

	public int getItemNumber(){
		return itemNumber;
	}
	
	public void setActiveIssue(int value){
		iActiveIssue = value;
	}
	
	public int getActiveIssue(){
		return iActiveIssue;
	}
	
}

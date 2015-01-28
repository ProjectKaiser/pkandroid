package com.projectkaiser.app_android.fragments.issue;
import com.triniforce.dom.dom2html.IUrlEncoder;


public class IssueURLEncoder implements IUrlEncoder {
	@Override
	public boolean isUrlEncoded(String str){
		return true;
	}
	public String urlEncode(String str){
		return str;
	}
	
}

package com.projectkaiser.app_android.fragments.issue;
import com.triniforce.dom.dom2html.IUrlEncoder;

public class IssueURLEncoder implements IUrlEncoder {
	@Override
	public boolean isUrlEncoded(String str){
		return str.contains("{/-");
	}
	@Override
	public String urlEncode(String str){
		String s = str;
		try{
									
			 if (str.contains("/att?name")) {
				 str = "{/-" + str + "-/}";
				 return str;
			 }
			 
			 if (!str.contains("http:")){
				 s = "{/-" + str + "-/}";
			 }

		} catch (Exception e){
		}
		return s;
	}
	
}

package com.projectkaiser.app_android.fragments.issue;
import com.triniforce.dom.dom2html.IUrlEncoder;

public class IssueURLEncoder implements IUrlEncoder {
	boolean m_encoded = false;
	@Override
	public boolean isUrlEncoded(String str){
		return m_encoded;
	}
	@Override
	public String urlEncode(String str){
		String s = str;
		try{
						
			 if (str.contains("/att?name")) {
				 str = "{/-" + str + "-/}";
				 m_encoded = true;
				 return str;
			 }
			 
			 s = java.net.URLEncoder.encode(str, "UTF-8");
			 m_encoded = true;
		} catch (Exception e){
		}
		return s;
	}
	
}

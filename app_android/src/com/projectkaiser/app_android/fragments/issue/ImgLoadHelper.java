package com.projectkaiser.app_android.fragments.issue;

import java.io.File;

import com.projectkaiser.app_android.R;
import com.triniforce.document.elements.TicketDef;
import com.triniforce.dom.DOMGenerator;
import com.triniforce.dom.EWikiError;
import com.triniforce.wiki.WikiParser;

import android.content.Context;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;

public class ImgLoadHelper {

	public static File GetImageDir(Context ctx) {
		File imdir = null;
		if (ctx.getExternalFilesDir(null) == null) {
			imdir = new File(ctx.getFilesDir().getAbsolutePath());
		} else {
			imdir = new File(ctx.getExternalFilesDir(null).getAbsolutePath());
		}
		if (!imdir.exists()) {
			imdir.mkdirs();
		}
		return imdir;
	}

	private static String GetImageShowScript() {
		StringBuilder sb = new StringBuilder();
		sb.append("<script type=\"text/javascript\">");
		sb.append("function toggle(divElem, imgElem, sUrl) {");
		sb.append("if(imgElem) {");
		sb.append("imgElem.src = sUrl;");
		sb.append("imgElem.style.visibility='visible';");
		sb.append("}");
		sb.append("if(divElem) {");
		sb.append("divElem.style.visibility='hidden';");
		sb.append("}");
		sb.append("}");
		sb.append("</script>");
		return  sb.toString();
	}

	public static String GetImageNameFromURL(String url) {
		int idxStart = 0;
		int idxEnd = 0;

		if (url.contains("/att?name")) {
			idxStart = url.indexOf("/att?name") + 10;
			idxEnd = url.indexOf("&", idxStart);
			if (idxEnd > idxStart + 3) {
				return url.substring(idxStart, idxEnd);
			}
		} else if (!url.contains("file:///")) {
			String fileExtenstion = MimeTypeMap.getFileExtensionFromUrl(url);
			return URLUtil.guessFileName(url, null, fileExtenstion);
		}
		return url;
	}

	private static String GetEmptyImageFrom(Context ctx, int idx, String strURL, String id) {
		String imFileName = id + "_"	+ GetImageNameFromURL(strURL);
		boolean bneedscript = false;
		File imgdir = GetImageDir(ctx);
		if (imgdir == null) {
			bneedscript = true;
		}
		;
		File imgfile = null;
		if (!bneedscript) {
			imgfile = new File(imgdir.getAbsolutePath() + "/" + imFileName);
			if (!imgfile.exists()) {
				bneedscript = true;
			}
		}
		if (!bneedscript) {
			return "<IMG src=\"file://" + imgfile.getAbsolutePath() + "\"";
		} else {

			StringBuilder sb = new StringBuilder();
			sb.append("<img id=\"img" + idx
					+ "\" src=\"\" style='visibility:hidden'>");
			sb.append("<div onclick=\"toggle(this,document.getElementById('img"
					+ idx
					+ "'), '"
					+ strURL
					+ "'"
					+ ")\" style=\"height:30px; text-align: center; vertical-align: bottom-text; border: 1px solid; border-radius: 8px; border-color:Grey; cursor: pointer; cursor: hand\">"
					+ ctx.getString(R.string.issue_load_image) + "</div>");
			return sb.toString();
		}
	}

	public static String ParsePictures(Context ctx, String strHTML, String sUrl, String id) {
		int idx = 0;

		while (strHTML.indexOf("<IMG src=\"{/-") > 0) {
			int FirstIdx = strHTML.indexOf("<IMG src=\"{/-");
			int LastImIdx = strHTML.indexOf("-/}", FirstIdx);
			int LastIdx = strHTML.indexOf(" />", LastImIdx);
			String pureImg = strHTML.substring(FirstIdx+13, LastImIdx);
			if (pureImg.indexOf("/att?")==0) pureImg = sUrl +  pureImg;
			String strURL = strHTML.substring(FirstIdx, LastIdx+3);
			String newStr  = GetEmptyImageFrom(ctx, idx, pureImg, id);
			String prevStrHTML = strHTML;	
			strHTML = strHTML.replace(strURL,newStr);
			if (prevStrHTML.equals(strHTML)) return strHTML; 
			idx = +1;
		}
		return GetImageShowScript() + strHTML;
		
	}

	public static String ParseEmail(String strHTML) {
		if (strHTML.indexOf("/-") > 0) {
			int FirstIdx = strHTML.indexOf("/-") + 2;
			int LastIdx = strHTML.indexOf("-/", FirstIdx);
			return strHTML.substring(FirstIdx, LastIdx);
		} else
			return strHTML;
	}
	
	public static TicketDef getTicket(String wiki) {
		DOMGenerator gen = new DOMGenerator();
		WikiParser wparser = new WikiParser();
		wparser.registerListener(gen);
		try {
			wparser.parse(wiki);
		} catch (EWikiError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return gen.getTicket();
	}
	
}

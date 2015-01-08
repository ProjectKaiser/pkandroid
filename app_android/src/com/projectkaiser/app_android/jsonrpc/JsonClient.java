package com.projectkaiser.app_android.jsonrpc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import org.json.JSONObject;

public class JsonClient {

	public static JSONObject makeRequest(String address, JsonObjectBuilder jb) {
		try {
			URL url = new URL(address);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
			try {
				conn.setDoInput(true);
				conn.setRequestMethod("POST");
				conn.setChunkedStreamingMode(0);
				conn.setRequestProperty("Accept", "text/json");
				conn.setRequestProperty("Content-type", "text/json");
				conn.setRequestProperty("Accept-Charset", "utf-8");				
				conn.setRequestProperty("Connection", "close");
				
				byte[] bytes = jb.toByteArray();
				
				conn.setRequestProperty("Content-length", String.valueOf(bytes.length));
				
				conn.getOutputStream().write(bytes);
				conn.getOutputStream().close();
				
			    InputStream in = new BufferedInputStream(conn.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"), 8);
				StringBuilder sb = new StringBuilder();
	
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				in.close();
				return new JSONObject(sb.toString());
			    
			} finally {
				conn.disconnect();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
}

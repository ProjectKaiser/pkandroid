package com.projectkaiser.app_android.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
 
import org.apache.http.util.ByteArrayBuffer;
 
import android.util.Log;
 
public class ImageDownloader {
 
        private String m_FileDir;
        
        public ImageDownloader(String downloadFilesDir){
        	m_FileDir = downloadFilesDir;
        }
        
        public void DownloadFromUrl(String imageURL, String fileName) {  
        	File fDir = new File(m_FileDir);
        	if (fDir.exists()){
        		return;
        	}
            File fullFn = new File(m_FileDir + "/" + fileName);
                try {
                        URL url = new URL(imageURL); 
                        File file = new File(fullFn.getAbsolutePath());
 
                        long startTime = System.currentTimeMillis();
                        /* Open a connection to that URL. */
                        URLConnection ucon = url.openConnection();
                        /*
                         * Define InputStreams to read from the URLConnection.
                         */
                        InputStream is = ucon.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
 
                        /*
                         * Read bytes to the Buffer until there is nothing more to read(-1).
                         */
                        ByteArrayBuffer baf = new ByteArrayBuffer(50);
                        int current = 0;
                        while ((current = bis.read()) != -1) {
                                baf.append((byte) current);
                        }
 
                        /* Convert the Bytes read to a String. */
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(baf.toByteArray());
                        fos.close();
                        Log.d("ImageManager", "download ready in"
                                        + ((System.currentTimeMillis() - startTime) / 1000)
                                        + " sec");
 
                } catch (IOException e) {
                        Log.d("ImageManager", "Error: " + e);
                }
 
        }
}
package com.grantmckenzie.UCSBActivityTrackr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class ATUpload extends Service {

    private String handler = "http://www.geogrant.com/socialtrackr/handlers/loc.php";
    private TelephonyManager tm;
    private ConnectivityManager connectivity;
    private String deviceId;
	
	@Override
	public void onCreate() {
	
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// Get unique device ID
		String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    deviceId = deviceUuid.toString();
	}
	
	public boolean isNetworkAvailable(Context context) {
	    if (connectivity != null) {
	       NetworkInfo[] info = connectivity.getAllNetworkInfo();
	       if (info != null) {
	          for (int i = 0; i < info.length; i++) {
	             if (info[i].getState() == NetworkInfo.State.CONNECTED) {
	                return true;
	             }
	          }
	       }
	    } 
	    return false;
	 }
	
	@Override
	public IBinder onBind(Intent intent) {
	 return null;
	}
	
	@Override
	public void onDestroy() {
		 super.onDestroy();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		 super.onStart(intent, startId);
		 
		 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		 String st_locdata = settings.getString("ST_LOCDATA", "");
		 String st_phonedata = settings.getString("ST_PHONE", "");
		 
		 if (isNetworkAvailable(getApplicationContext())) {
			 if (st_locdata != "" || st_phonedata != "") {
				 String response = sendLocation(deviceId, st_locdata, st_phonedata);
				 if (response != "0") {
					 SharedPreferences.Editor editor = settings.edit();
					 editor.putString("ST_LOCDATA", "");
					 editor.putInt("ST_COORDS", 0);
					 editor.putString("ST_PHONE", "");
					 editor.commit();
					 Toast.makeText( getApplicationContext(),"Data sent to server.",Toast.LENGTH_SHORT).show();
				 }
			 } else {
				 Toast.makeText( getApplicationContext(),"No new data to send.",Toast.LENGTH_SHORT).show();
			 }
		 } else {
			 Toast.makeText( getApplicationContext(),"No Data Connection.\nData not sent to server.",Toast.LENGTH_SHORT).show();
		 }
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		 return super.onUnbind(intent);
	}
	private String sendLocation(String uid, String datavals, String phonedata) {
		HttpClient httpclient = new DefaultHttpClient();  
	    HttpPost httppost = new HttpPost(handler);  
	    HttpResponse response = null;
	    
	    try {  
	        // Add your data  
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
	        nameValuePairs.add(new BasicNameValuePair("uid", uid));  
	        nameValuePairs.add(new BasicNameValuePair("vals", datavals));  
	        nameValuePairs.add(new BasicNameValuePair("phone", phonedata));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
	  
	        // Execute HTTP Post Request  
	        response = httpclient.execute(httppost);  
	          
	    } catch (ClientProtocolException e) {  
	        // TODO Auto-generated catch block  
	    } catch (IOException e) {  
	        // TODO Auto-generated catch block  
	    }  
		return response.toString();
	}
}

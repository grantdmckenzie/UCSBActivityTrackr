package com.grantmckenzie.UCSBActivityTrackr;

import java.io.IOException;
import java.sql.Timestamp;
import static android.provider.BaseColumns._ID;

import java.util.ArrayList;
import java.util.Calendar;
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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class ATLocation extends Service implements LocationListener{
	
	private LocationManager locationManager;
	private String best;
	private Location currentLocation;
	private String handler = "http://geogremlin.geog.ucsb.edu/grantm/android_server.php";

	Criteria crit = null;

	
	TelephonyManager tm;
	private ConnectivityManager connectivity;
    String deviceId;
    private String vals;
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		crit = new Criteria();
		best = locationManager.getBestProvider(crit, true);
		currentLocation = locationManager.getLastKnownLocation(best);
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// Get unique device ID
		String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    deviceId = deviceUuid.toString();
	    
		/* best = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(best);
		String Text = "Best Provider is: "+best+"\nLast known coords:\nLat= " + location.getLatitude() + "\nLong= " + location.getLongitude();
		Toast.makeText(this, Text, Toast.LENGTH_SHORT).show(); */
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();
		locationManager.removeUpdates(this);
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
		locationManager.requestLocationUpdates(best, 20000, 0, this);
	}
	
	public void onLocationChanged(Location loc) {
			
			Timestamp ts = new Timestamp(Calendar.getInstance().getTime().getTime());	
			double lat = loc.getLatitude();
			double lon = loc.getLongitude();
			// Toast.makeText(this, ""+lat+", "+lon, Toast.LENGTH_SHORT).show();
			// vals = lat + "," + lon + "," + ts.toString();
			storeData(lat+"", lon+"", ts.toString());
	}

	public void onProviderDisabled(String provider) {
			// Toast.makeText( getApplicationContext(),"Gps Disabled",Toast.LENGTH_SHORT ).show();
	}

	public void onProviderEnabled(String provider) {
			// Toast.makeText( getApplicationContext(),"Gps Enabled",Toast.LENGTH_SHORT).show();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
			// Toast.makeText( getApplicationContext(),"Location Service using: "+provider,Toast.LENGTH_SHORT).show();
	}

	
	private void storeData(String lat, String lon, String timest) {
		
		/* SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		String st_locdata = settings.getString("ST_LOCDATA", "");
		int st_coordcounter = settings.getInt("ST_COORDS", 0);
		
	    SharedPreferences.Editor editor = settings.edit();
	    st_locdata = st_locdata + data;
	    st_coordcounter++;
	    
	    editor.putString("ST_LOCDATA", st_locdata);
	    editor.putInt("ST_COORDS", st_coordcounter);
	    
	    editor.commit();
	    Toast.makeText( getApplicationContext(),"Coords: "+data,Toast.LENGTH_SHORT).show(); */
	    
		 if (isNetworkAvailable(getApplicationContext())) {
			 if (lat != "") {
				 String response = sendLocation(deviceId, lat, lon, timest);
				 if (response != "0") {
					 /* editor = settings.edit();
					 editor.putString("ST_LOCDATA", "");
					 editor.putInt("ST_COORDS", 0);
					 editor.putString("ST_PHONE", "");
					 editor.commit(); */
					 Toast.makeText( getApplicationContext(),"Data sent to server.",Toast.LENGTH_SHORT).show();
				 }
			 } else {
				 Toast.makeText( getApplicationContext(),"No new data to send.",Toast.LENGTH_SHORT).show();
			 }
		 } else {
			 Toast.makeText( getApplicationContext(),"No Data Connection.\nData not sent to server.",Toast.LENGTH_SHORT).show();
		 }

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
	
	private String sendLocation(String uid, String lat, String lon, String timest) {
		HttpClient httpclient = new DefaultHttpClient();  
	    HttpPost httppost = new HttpPost(handler);  
	    HttpResponse response = null;
	    
	    try {  
	        // Add your data  
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
	        nameValuePairs.add(new BasicNameValuePair("uid", uid));  
	        nameValuePairs.add(new BasicNameValuePair("lat", lat));
	        nameValuePairs.add(new BasicNameValuePair("lon", lon));
	        nameValuePairs.add(new BasicNameValuePair("t", timest));
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


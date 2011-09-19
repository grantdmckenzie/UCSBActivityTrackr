/*
 * Project: UCSBActivityTrackr
 * Author: Grant McKenzie
 * Date: May 2011
 * Client: GeoTrans Lab @ UCSB
 * 
 */

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

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import com.grantmckenzie.UCSBActivityTrackr.Fix;

public class ATLocation extends Service {
	
	private float MINDISTANCEBTWNFIXES = 150;	// meters
	private float MINDISTANCEBTWNACTS = 150;	// meters
	private float previousActLat = 0;
	private float previousActLng = 0;
	private long previousActTS = 0;
	private boolean activityMode = false;
	
	private NotificationManager mNotificationManager;
	private int SIMPLE_NOTFICATION_ID = 1000;
	private Notification notifyDetails;
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	private String best;
	private Location currentLocation;
	private ArrayList<Fix> latestFixes = new ArrayList<Fix>();
	private Timestamp previousTime;
	private String lastActivityID;
	
	Criteria crit = null;

	
	private TelephonyManager tm;
	private ConnectivityManager connectivity;
    private String deviceId;
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
		
		// Notification stuff
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		notifyDetails = new Notification(R.drawable.icon,"New Alert, Click Me!",System.currentTimeMillis());
		long[] vibrate = {100,100,200,300};
        notifyDetails.vibrate = vibrate;
        notifyDetails.defaults =Notification.DEFAULT_ALL;
        Context context = getApplicationContext();
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// Get unique device ID
		String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    deviceId = deviceUuid.toString();
	    
	    locationListener = new MyLocationListener();
	    
		/* best = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(best);
		String Text = "Best Provider is: "+best+"\nLast known coords:\nLat= " + location.getLatitude() + "\nLong= " + location.getLongitude();
		Toast.makeText(this, Text, Toast.LENGTH_SHORT).show(); */
	}
	
	@Override
	public void onDestroy() {
		// Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();
		locationManager.removeUpdates(locationListener);
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		// Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
		locationManager.requestLocationUpdates(best,0, 0, locationListener);
	}
	
	public class MyLocationListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			
			Timestamp ts = new Timestamp(Calendar.getInstance().getTime().getTime());	
			float lat = (float) loc.getLatitude();
			float lon = (float) loc.getLongitude();
			// storeData(lat, lon, ts.toString());
			parseActivities(lat, lon, ts);
			locationManager.removeUpdates(locationListener);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// Toast.makeText( getApplicationContext(),"Gps Disabled",Toast.LENGTH_SHORT ).show();
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// Toast.makeText( getApplicationContext(),"Gps Enabled",Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Toast.makeText( getApplicationContext(),"Location Service using: "+provider,Toast.LENGTH_SHORT).show();
		}

	}
	
	public void parseActivities(float lat, float lng, Timestamp ts) {

		Fix currentFix = new Fix(lat,lng, ts);	// create new fix object
		
		long currentTSminus5 = ts.getTime() - 300000;		// subtrack 5 minutes from current timestamp

		float totLat = lat;
		float totLng = lng;
		float totDist = 0;
		for(int i=0; i<latestFixes.size();i++) {
			long fixesTS = latestFixes.get(i).getTs().getTime();
			if (fixesTS < currentTSminus5) {
				latestFixes.remove(latestFixes.get(i));
			}
			totDist += calcDist(lat, lng, latestFixes.get(i).getLat(), latestFixes.get(i).getLng());
			totLat += latestFixes.get(i).getLat();
			totLng += latestFixes.get(i).getLng();
			
		}
		float avgDistBTWFixes = totDist / latestFixes.size();
		float avgLat = totLat / (latestFixes.size() + 1);
		float avgLng = totLng / (latestFixes.size() + 1);
		latestFixes.add(currentFix);
		
		float dist2lastAct = calcDist(previousActLat, previousActLng, avgLat, avgLng);
		long timediff = ts.getTime() - previousActTS;
		Toast.makeText( getApplicationContext(),"Array Size: "+latestFixes.size() + "\nAvgDist: " + avgDistBTWFixes + "\nDist2LastAct: " + dist2lastAct + "\nTimeDiff: " + timediff,Toast.LENGTH_SHORT).show();
		
		
		if (avgDistBTWFixes <= MINDISTANCEBTWNFIXES) {
			if (dist2lastAct >= MINDISTANCEBTWNACTS) {
				if (timediff >= 300000) {
					previousActLat = lat;
					previousActLng = lng;
					previousActTS = ts.getTime();
					activityMode = true;
					Toast.makeText( getApplicationContext(),"START of New Activity. \n End of Trip",Toast.LENGTH_LONG).show();
					storeData(""+lat, ""+lng, ""+ts, false, "", false, false);		// store end of trip (no activity, no activity id, not new trip, end of activity)
					storeData(""+lat, ""+lng, ""+ts, true, "", false, false);		// store start activity (yes activity, no activity id, not new trip)
				} else {
					// still within activity, send points to db.
					storeData(""+currentFix.getLat(), ""+currentFix.getLng(), ""+currentFix.getTs(), true, lastActivityID, false, false);
				}
			} else {
				storeData(""+currentFix.getLat(), ""+currentFix.getLng(), ""+currentFix.getTs(), true, lastActivityID, false, false);
			}
		} else {
			if (activityMode) {
				activityMode = false;
				Toast.makeText( getApplicationContext(),"END of Activity.\n START of New Trip",Toast.LENGTH_LONG).show();
				storeData(""+lat, ""+lng, ""+ts, true, lastActivityID, false, true);	// store end of activity
				storeData(""+lat, ""+lng, ""+ts, false, "", true, false);				// store start of trip  (lat,lng, timestamp, activity, activityid, newtrip)
			} else {
				// Go about your business as usual
				if (previousActTS == 0) {
					storeData(""+lat, ""+lng, ""+ts, false, "", true, false);	// new trip
				} else {
					storeData(""+lat, ""+lng, ""+ts, false, "", false, false);	// store fix in TRAVEL_FIXES table 
				}
			}
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
	}
	
	public static float calcDist(double lat1, double lng1, double lat2, double lng2) {
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    int meterConversion = 1609;
	    return new Float(dist * meterConversion).floatValue();
	}
	
	private void storeData(String lat, String lon, String timest, boolean activity, String activityid, boolean newtrip, boolean endactivity) {
		
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
				 String response = sendLocation(deviceId, lat, lon, timest, activity, activityid, newtrip, endactivity);
				 String lines[] = response.split("\\r?\\n");
				 if (response != "0") {
					 /* editor = settings.edit();
					 editor.putString("ST_LOCDATA", "");
					 editor.putInt("ST_COORDS", 0);
					 editor.putString("ST_PHONE", "");
					 editor.commit(); */
					 if (activity && activityid == "") {
						 lastActivityID = lines[0];
						 Toast.makeText( getApplicationContext(),lines[0],Toast.LENGTH_SHORT).show();
						 
						 Intent dialogIntent = new Intent(getBaseContext(), notify1.class);
						 dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						 dialogIntent.putExtra("locations", response);
						 dialogIntent.putExtra("lat", lat);
						 dialogIntent.putExtra("lon", lon);
						 dialogIntent.putExtra("id", lastActivityID);
						 getApplication().startActivity(dialogIntent);
						  
						  /* Context context = getApplicationContext();
			              CharSequence contentTitle = "New Survey";
			              CharSequence contentText = "Get back to Application on clicking me";
			          
			              Intent dialogIntent = new Intent(getBaseContext(), UCSBActivityTrackr.class);
			              PendingIntent intent = PendingIntent.getActivity(this, 0, dialogIntent, android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
			              notifyDetails.setLatestEventInfo(context, contentTitle, contentText, intent);
			              mNotificationManager.notify(SIMPLE_NOTFICATION_ID, notifyDetails); */


					 }
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
		try {
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
		} catch (Exception e) {
			return false;
		}
	 }
	
	private String sendLocation(String uid, String lat, String lon, String timest, boolean activity, String activityid, boolean newtrip, boolean endactivity) {
		String handler;
		if (activity)
			handler = "http://geogremlin.geog.ucsb.edu/android/store_activity.php";
		else {
			handler = "http://geogremlin.geog.ucsb.edu/android/store_fix.php";
		}
		
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
	        nameValuePairs.add(new BasicNameValuePair("id", activityid));
	        nameValuePairs.add(new BasicNameValuePair("new", new Boolean(newtrip).toString()));
	        nameValuePairs.add(new BasicNameValuePair("endact", new Boolean(endactivity).toString()));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
	  
	        // Execute HTTP Post Request  
	        response = httpclient.execute(httppost);  
	          
	    } catch (ClientProtocolException e) {  
	        // TODO Auto-generated catch block  
	    } catch (IOException e) {  
	        // TODO Auto-generated catch block  
	    }  
	    
	    return HTTPHelper.request(response);
	}
}


package com.grantmckenzie.UCSBActivityTrackr;

import java.sql.Timestamp;
import java.util.Calendar;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import android.os.CountDownTimer;

public class ATLocation extends Service {
	private LocationManager locationManager;
	
	private Location currentLocation;
	private LocationListener listenerCoarse;
	private LocationListener listenerFine;
	
	Criteria fine = null;
	Criteria coarse = null;
	
	private boolean isFine;
	
	TelephonyManager tm;
    String deviceId;
    private String vals;
    
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		fine = new Criteria();
		fine.setAccuracy(Criteria.ACCURACY_FINE);
		coarse = new Criteria();
		coarse.setAccuracy(Criteria.ACCURACY_COARSE);
		isFine = true;
		if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(fine, true));
		} else {
			currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(coarse, true));
			Toast.makeText(getBaseContext(),"GPS Disabled",Toast.LENGTH_LONG).show();
		}

		listenerFine = new MyLocationListener();
		listenerCoarse = new MyLocationListener();
		
		new CountDownTimer(20000, 1000) {

		     public void onTick(long millisUntilFinished) {
		    	 // Toast.makeText(getBaseContext(),"seconds remaining: " + millisUntilFinished / 1000,Toast.LENGTH_SHORT).show();
		     }

		     public void onFinish() {
		    	 locationManager.removeUpdates(listenerFine);
		    	 Toast.makeText(getBaseContext(),"Switching to Coarse Provider" ,Toast.LENGTH_SHORT).show();
		    	 locationManager.requestLocationUpdates(locationManager.getBestProvider(coarse, true),1000, 300, listenerCoarse);
		     }
		  }.start();
		
		

		/* best = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(best);
		String Text = "Best Provider is: "+best+"\nLast known coords:\nLat= " + location.getLatitude() + "\nLong= " + location.getLongitude();
		Toast.makeText(this, Text, Toast.LENGTH_SHORT).show(); */
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();
		locationManager.removeUpdates(listenerCoarse);
		locationManager.removeUpdates(listenerFine);
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "Location Service Started", Toast.LENGTH_SHORT).show();
		 // locationManager.requestLocationUpdates(locationManager.getBestProvider(coarse, true),500, 300, listenerCoarse);
		if (isFine)
			locationManager.requestLocationUpdates(locationManager.getBestProvider(fine, true), 0, 0, listenerFine);

	}
	
	public class MyLocationListener implements LocationListener {
		
		@Override
		public void onLocationChanged(Location loc) {
			
			Timestamp ts = new Timestamp(Calendar.getInstance().getTime().getTime());	
			double lat = loc.getLatitude();
			double lon = loc.getLongitude();
			if(isFine)
				vals = lat + "," + lon + "," + ts.toString() + ",1|";
			else 
				vals = lat + "," + lon + "," + ts.toString() + ",0|";
			storeData(vals);
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
			Toast.makeText( getApplicationContext(),"Location Service using: "+provider,Toast.LENGTH_SHORT).show();
		}

	}
	
	private void storeData(String data) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		String st_locdata = settings.getString("ST_LOCDATA", "");
		int st_coordcounter = settings.getInt("ST_COORDS", 0);
		
	    SharedPreferences.Editor editor = settings.edit();
	    st_locdata = st_locdata + data;
	    st_coordcounter++;
	    
	    editor.putString("ST_LOCDATA", st_locdata);
	    editor.putInt("ST_COORDS", st_coordcounter);
	    
	    editor.commit();
	    Toast.makeText( getApplicationContext(),"Coords: "+st_coordcounter,Toast.LENGTH_SHORT).show();
	    locationManager.removeUpdates(listenerFine);
	    locationManager.removeUpdates(listenerCoarse);
	}
}


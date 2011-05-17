package com.grantmckenzie.UCSBActivityTrackr;

import java.util.UUID;

import android.telephony.TelephonyManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.widget.ToggleButton;

public class UCSBActivityTrackr extends Activity implements OnClickListener {

  ToggleButton buttonLocation, buttonSendData;
  private TelephonyManager tm;
  private String deviceId;
  private int resultid = 0;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main); 
    
    tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	// Get unique device ID
	String tmDevice, tmSerial, androidId;
    tmDevice = "" + tm.getDeviceId();
    tmSerial = "" + tm.getSimSerialNumber();
    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
    deviceId = deviceUuid.toString();
    
    buttonLocation = (ToggleButton) findViewById(R.id.toggleLocation);
    buttonLocation.setOnClickListener(this);
    buttonLocation.setText("Location Off");
 
    
  }
 
  public void onClick(View src) {
    switch (src.getId()) {
    case R.id.toggleLocation:
    	if (!buttonLocation.isChecked()) {
    		/* AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
    	    alarmManager.cancel(locpendingIntent); */
    		stopService(new Intent(this, ATLocation.class));
    	    // Toast.makeText(this, "Location Off", Toast.LENGTH_LONG).show(); 
    	    buttonLocation.setText("Location Off");
    	} else {
    		buttonLocation.setText("Location On");
    		
    		/* long firstTime = SystemClock.elapsedRealtime();
			Intent myIntent = new Intent(this, ATLocation.class);
			locpendingIntent = PendingIntent.getService(this, 0, myIntent, 0); */
    		startService(new Intent(this, ATLocation.class));
			/* AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 300000, locpendingIntent); */
    	}
        break;
    }
  }
}
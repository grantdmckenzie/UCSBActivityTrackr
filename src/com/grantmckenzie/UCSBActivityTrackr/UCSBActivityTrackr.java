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

import android.telephony.TelephonyManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class UCSBActivityTrackr extends Activity implements OnClickListener {

  ToggleButton buttonLocation;
  Button buttonLogin;
  EditText username;
  EditText password;
  private TelephonyManager tm;
  private String deviceId;
  private int resultid = 0;
  private String handler = "http://geogremlin.geog.ucsb.edu/grantm/login.php";
  private InputMethodManager imm;
  private ConnectivityManager connectivity;

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
 
    buttonLogin = (Button) findViewById(R.id.Login);
    buttonLogin.setOnClickListener(this);
    buttonLocation.setEnabled(false);
    
    username = (EditText) findViewById(R.id.Username);
    password = (EditText) findViewById(R.id.Password);
    
    
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
    case R.id.Login:
    	String result = checkLogin(username.getText().toString(), password.getText().toString());
    	int resultint = Integer.parseInt(result.replace("\n","").trim());
    	if (resultint == 1) {
    		Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
    		buttonLocation.setEnabled(true);
    	} else {
    		Toast.makeText(this, "There was an error logging you in.  Please try again.", Toast.LENGTH_SHORT).show();
    	}
    	imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
    }
  }
  
  public String checkLogin(String user, String pass) {
	  
	  	tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// Get unique device ID
		String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    deviceId = deviceUuid.toString();
	  
		HttpClient httpclient = new DefaultHttpClient();  
		HttpPost httppost = new HttpPost(handler);  
		HttpResponse response = null;
		
		try {  
		    // Add your data  
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
		    nameValuePairs.add(new BasicNameValuePair("devid", deviceId));  
		    nameValuePairs.add(new BasicNameValuePair("u", user));
		    nameValuePairs.add(new BasicNameValuePair("p", pass));
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
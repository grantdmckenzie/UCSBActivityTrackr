/*
 * Project: UCSBActivityTrackr
 * Author: Grant McKenzie
 * Date: May 2011
 * Client: GeoTrans Lab @ UCSB
 * 
 */

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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UCSBActivityTrackr extends Activity implements OnClickListener {

  Button buttonLogin;
  EditText username;
  EditText password;
  TextView textusername;
  TextView textpassword;
  private TelephonyManager tm;
  private String deviceId;
  private String handler = "http://geogremlin.geog.ucsb.edu/android/login.php";
  private ConnectivityManager connectivity;
  private PendingIntent locpendingIntent;
  private SharedPreferences settings;
  private int at_login;
  Button buttonPostponed;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main); 
    
    settings = PreferenceManager.getDefaultSharedPreferences(this);
    
    tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	// Get unique device ID
	String tmDevice, tmSerial, androidId;
    tmDevice = "" + tm.getDeviceId();
    tmSerial = "" + tm.getSimSerialNumber();
    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
    deviceId = deviceUuid.toString();
    
    buttonLogin = (Button) findViewById(R.id.Login);
    buttonLogin.setOnClickListener(this);
    
    textusername = (TextView) findViewById(R.id.textUsername);
    textpassword = (TextView) findViewById(R.id.txtPassword);
    username = (EditText) findViewById(R.id.Username);
    password = (EditText) findViewById(R.id.Password);
    
    at_login = settings.getInt("AT_LOGINSET", 0);
    
    if (at_login == 1)
    	buttonLogin.setText("Logout");
    else
    	buttonLogin.setText("Login");
    
    buttonPostponed = (Button) findViewById(R.id.Postponed);
    buttonPostponed.setOnClickListener(this);
    buttonPostponed.setVisibility(View.INVISIBLE);
  }
 
  public void onClick(View src) {
	  if (src.getId() == R.id.Login) {
		  	buttonLogin.setEnabled(false);
		  	username.setEnabled(false);
			password.setEnabled(false);
			SharedPreferences.Editor editor = settings.edit();
			if (isNetworkAvailable(getApplicationContext())) {
				if (at_login == 0) {
			    	String result = checkLogin(username.getText().toString(), password.getText().toString());
		
			    	int resultint = Integer.parseInt(result.replace("\n","").trim());
			    	if (resultint == 1) {
			    		Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
			    		long firstTime = SystemClock.elapsedRealtime();
						Intent myIntent = new Intent(this, ATLocation.class);
						locpendingIntent = PendingIntent.getService(this, 0, myIntent, 0);
						AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
						alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 20000, locpendingIntent); 
						buttonLogin.setText("Logout");
						editor.putInt("AT_LOGINSET", 1);
						
						at_login = 1;
						editor.commit();
						username.setVisibility(View.INVISIBLE);
						password.setVisibility(View.INVISIBLE);
						textusername.setVisibility(View.INVISIBLE);
						textpassword.setVisibility(View.INVISIBLE);
						buttonPostponed.setVisibility(View.VISIBLE);
			    	} else {
			    		Toast.makeText(this, "There was an error logging you in.  Please try again.", Toast.LENGTH_SHORT).show();
			    	}
				} else {
					AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		    	    alarmManager.cancel(locpendingIntent);
		    	    Toast.makeText(this, "Location Service Stopped", Toast.LENGTH_SHORT).show();
					editor.putInt("AT_LOGINSET", 0);
					at_login = 0;
					editor.commit();
					
					buttonLogin.setText("Login");
					username.setVisibility(View.VISIBLE);
					password.setVisibility(View.VISIBLE);
					textusername.setVisibility(View.VISIBLE);
					textpassword.setVisibility(View.VISIBLE);
					buttonPostponed.setVisibility(View.INVISIBLE);
					username.setText("");
					password.setText("");
				}
			} else {
				 Toast.makeText( getApplicationContext(),"No Data Connection.\nCould not check credentials",Toast.LENGTH_SHORT).show();
				 editor.putInt("AT_LOGINSET", 0);
			}
			buttonLogin.setEnabled(true);
		    username.setEnabled(true);
		    password.setEnabled(true);
	    } else if (src.getId() == R.id.Postponed) {
	    	Intent dialogIntent = new Intent(getBaseContext(), ATPostponed.class);
	    	dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	getApplication().startActivity(dialogIntent);
       }
     }
	  public String checkLogin(String user, String pass) {
		    
		    // dialog = ProgressDialog.show(this, "","Checking credentials", true);
		  	tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			
			// Get unique device ID
			String tmDevice, tmSerial, androidId;
		    tmDevice = "" + tm.getDeviceId();
		    tmSerial = "" + tm.getSimSerialNumber();
		    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
		    deviceId = deviceUuid.toString();
		  
		    
		    HttpParams httpParameters = new BasicHttpParams();
		    int timeoutConnection = 3000;
		    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
		    int timeoutSocket = 3000;
		    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		    
			HttpClient httpclient = new DefaultHttpClient(httpParameters);  
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
  
}
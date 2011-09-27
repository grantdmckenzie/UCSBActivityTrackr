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
import java.util.HashMap;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ATQuestionnaire extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	private ListView lv;
	Button buttonNotActivity;
	Button buttonHome;
	Button buttonNotListed;
	EditText otheret;
	LayoutInflater factory;
	String selectedLocation;
	String selectedWhom;
	private TelephonyManager tm;
	private ConnectivityManager connectivity;
    private String deviceId;
    private String activityID;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionnaire);
        lv= (ListView)findViewById(R.id.listview);

        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		// Get unique device ID
		String tmDevice, tmSerial, androidId;
	    tmDevice = "" + tm.getDeviceId();
	    tmSerial = "" + tm.getSimSerialNumber();
	    androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
	    UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
	    deviceId = deviceUuid.toString();
        
        // create the grid item mapping
        String[] from = new String[] {"col_1"};
        int[] to = new int[] { R.id.item1 };
        
        Bundle b = this.getIntent().getExtras();
        String logtime = b.getString("logtime");
        String[] actID = logtime.split("&");
        activityID = actID[1];
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(logtime);
	    editor.commit();
        
		String response = b.getString("locations");
		
		
        // String response = "hello world 1\nhello world 2\nhello world 1\nhello world 2\nhello world 1\nhello world 2\nhello world 1\nhello world 2\nhello world 1\nhello world 2"; 
		final String lines[] = response.split("\\r?\\n");
		
        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 1; i < lines.length; i++){
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put("col_1", lines[i]);
        	fillMaps.add(map);
        }
        HashMap<String, String> map = new HashMap<String, String>();
    	map.put("col_1", "Other (Not Listed)");
    	fillMaps.add(map);
    	
    	factory = LayoutInflater.from(this);

        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.grid_item, from, to);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	
	        @Override
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	        { 
	        	String stuff = lv.getItemAtPosition(position).toString();
	        	String[] name = stuff.split("=");
	        	
	        	if (position == lines.length - 1) {
	        		
	                final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
	        		
	        		AlertDialog.Builder adb=new AlertDialog.Builder(ATQuestionnaire.this);
		        	adb.setTitle("TravelerServ");
		        	adb.setMessage("Please enter your location:");
		        	adb.setView(textEntryView);
		        	adb.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                    	EditText otheret = (EditText) textEntryView.findViewById((R.id.otherval));
	                    	selectedLocation = otheret.getText().toString();
	                    	WithWhom();
	                    }
	                });
		        	adb.show();
	        	} else {
	        		selectedLocation = name[1].replace('}', ' ');
	        		WithWhom();
	        	}
	        }
        });
        
    }

	@Override
	public void onClick(View src) {
		/* if (src.getId() == R.id.notactivity) {
			finish();
		} else if (src.getId() == R.id.notlisted) {
			LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.questionnaire_other, null);
            AlertDialog.Builder adb=new AlertDialog.Builder(ATQuestionnaire.this);
                adb.setTitle("TravelerServ");
                adb.setView(textEntryView);
                adb.setPositiveButton("Yes", null);
                adb.show();
		
		} else if (src.getId() == R.id.home) {
            AlertDialog.Builder adb=new AlertDialog.Builder(ATQuestionnaire.this);
        	adb.setTitle("TravelerServ");
        	adb.setMessage("Would you like to set your current location as HOME?");
        	adb.setPositiveButton("Yes", null);
        	adb.setNegativeButton("No", null);
        	adb.show(); 
		} */
	}
	public void WithWhom() {
		selectedWhom = "";
		final View whoareyouwith = factory.inflate(R.layout.whoareyouwith, null);
		AlertDialog.Builder adb=new AlertDialog.Builder(ATQuestionnaire.this);
    	adb.setTitle("TravelerServ");
    	adb.setMessage("With whom?");
    	adb.setView(whoareyouwith);
    	adb.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	CheckBox family = (CheckBox) whoareyouwith.findViewById((R.id.family));
            	CheckBox friend = (CheckBox) whoareyouwith.findViewById((R.id.friend));
            	CheckBox coworker = (CheckBox) whoareyouwith.findViewById((R.id.coworker));
            	CheckBox other = (CheckBox) whoareyouwith.findViewById((R.id.other));
                if (family.isChecked())
                	selectedWhom += "1,";
                else 
                	selectedWhom += "0,";
                if (friend.isChecked())
                	selectedWhom += "1,";
                else 
                	selectedWhom += "0,";
                if (coworker.isChecked())
                	selectedWhom += "1";
                else 
                	selectedWhom += "0";
                if (other.isChecked())
                	selectedWhom += "1";
                else 
                	selectedWhom += "0";
                // Toast.makeText(getApplicationContext(),"ActivityID: "+activityID+"\nLocation: "+selectedLocation+"\nWith: "+selectedWhom,Toast.LENGTH_SHORT).show();
                storeData(activityID, selectedLocation, selectedWhom);
            }
        });
    	adb.show();
	}
	
	private void storeData(String activityID, String location, String whom) {
		
		 if (isNetworkAvailable(getApplicationContext())) {
			 if (location != "") {
				 String response = sendLocation(deviceId, activityID, location, whom);
				 String lines[] = response.split("\\r?\\n");
				 if (response != "0") {
					 Toast.makeText( getApplicationContext(),"Questionnaire sent to server.",Toast.LENGTH_SHORT).show();
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

	private String sendLocation(String uid, String activityID, String location, String whom) {
		String handler = "http://geogremlin.geog.ucsb.edu/android/store_questionnaire.php";

		HttpClient httpclient = new DefaultHttpClient();  
	    HttpPost httppost = new HttpPost(handler);  
	    HttpResponse response = null;
	    try {  
	        // Add your data  
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
	        nameValuePairs.add(new BasicNameValuePair("uid", uid));  
	        nameValuePairs.add(new BasicNameValuePair("id", activityID));
	        nameValuePairs.add(new BasicNameValuePair("loc", location));
	        nameValuePairs.add(new BasicNameValuePair("whom", whom));
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

package com.grantmckenzie.UCSBActivityTrackr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class notify1 extends Activity implements OnClickListener {
	Button btnNow;
	Button btnLater;
	Button btnNever;
	public String response;
	String activityID;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
         setContentView(R.layout.notify1);
         
        
         
         
         btnNow = (Button) findViewById(R.id.now);
         btnNow.setOnClickListener(this);
         btnLater = (Button) findViewById(R.id.later);
         btnLater.setOnClickListener(this);
         btnNever = (Button) findViewById(R.id.never);
         btnNever.setOnClickListener(this);
         Bundle b = this.getIntent().getExtras();
 		 response = b.getString("locations");
 		 String lat = b.getString("lat");
 		 String lon = b.getString("lon");
 		 activityID = b.getString("id");
 		try {
 			ImageView i = (ImageView)findViewById(R.id.imageView1);
 			Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL("http://maps.googleapis.com/maps/api/staticmap?center="+lat+","+lon+"&zoom=12&size=80x80&maptype=roadmap&markers=size:small|color:red|"+lat+","+lon+"&sensor=false").getContent());
 			i.setImageBitmap(bitmap);
 		} catch (MalformedURLException e) {
		  e.printStackTrace();
		} catch (IOException e) {
		  e.printStackTrace();
		}
    }
	@Override
	public void onClick(View v) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		String currentDateTimeString = (String) android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date());
		String at_postponed_dates = settings.getString("AT_POSTPONED_DATES", "");
		at_postponed_dates = at_postponed_dates + "#" + currentDateTimeString + "&" + activityID;
		
		if (v.getId() == R.id.now) {
			 editor.putString(currentDateTimeString, response);
			 editor.putString("AT_POSTPONED_DATES", at_postponed_dates);
			 editor.commit();
			 
			 Intent dialogIntent = new Intent(getBaseContext(), ATQuestionnaire.class);
			 dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 dialogIntent.putExtra("locations", response);
			 dialogIntent.putExtra("logtime", currentDateTimeString + "&" + activityID);
			 getApplication().startActivity(dialogIntent);
		} else if (v.getId() == R.id.later){
			Toast.makeText( getApplicationContext(),"Questionnaire added to queue as \""+currentDateTimeString+"\"",Toast.LENGTH_LONG).show();
			editor.putString(currentDateTimeString, response);
			editor.putString("AT_POSTPONED_DATES", at_postponed_dates);
		    editor.commit();
			finish();
		} else if (v.getId() == R.id.never) {
			AlertDialog.Builder adb=new AlertDialog.Builder(notify1.this);
        	adb.setTitle("TravelerServ");
        	adb.setMessage("Are you sure that this is not a new activity?");
        	adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {  
        	      public void onClick(DialogInterface dialog, int which) {  
        	    	finish();
        	        return;  
        	   } });
        	adb.setNegativeButton("No", new DialogInterface.OnClickListener() {  
      	      public void onClick(DialogInterface dialog, int which) {  
      	        return;  
      	      } });
        	adb.show(); 
        	
		}
	}
	
	

}

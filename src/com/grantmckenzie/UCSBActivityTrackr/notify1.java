package com.grantmckenzie.UCSBActivityTrackr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class notify1 extends Activity implements OnClickListener {
	Button btnYes;
	Button btnNo;
	public String response;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	 super.onCreate(savedInstanceState);
         setContentView(R.layout.notify1);
         
         btnYes = (Button) findViewById(R.id.button1);
         btnYes.setOnClickListener(this);
         btnNo = (Button) findViewById(R.id.button2);
         btnNo.setOnClickListener(this);
         Bundle b = this.getIntent().getExtras();
 		 response = b.getString("locations");
    }
	@Override
	public void onClick(View v) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		String currentDateTimeString = (String) android.text.format.DateFormat.format("yyyy-MM-dd hh:mm:ss", new java.util.Date());
		String at_postponed_dates = settings.getString("AT_POSTPONED_DATES", "");
		at_postponed_dates = at_postponed_dates + "#" + currentDateTimeString;
		editor.putString(currentDateTimeString, response);
		editor.putString("AT_POSTPONED_DATES", at_postponed_dates);
	    editor.commit();
		if (v.getId() == R.id.button1) {
			// TODO Auto-generated method stub
			 Intent dialogIntent = new Intent(getBaseContext(), ATQuestionnaire.class);
			 dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 dialogIntent.putExtra("locations", response);
			 dialogIntent.putExtra("logtime", currentDateTimeString);
			 getApplication().startActivity(dialogIntent);
		} else {
			Toast.makeText( getApplicationContext(),"Questionnaire added to queue.",Toast.LENGTH_SHORT).show();
		}
	}

}

/*
 * Project: UCSBActivityTrackr
 * Author: Grant McKenzie
 * Date: May 2011
 * Client: GeoTrans Lab @ UCSB
 * 
 */

package com.grantmckenzie.UCSBActivityTrackr;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ATQuestionnaire extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	private ListView lv;
	Button buttonNotActivity;
	Button buttonHome;
	Button buttonNotListed;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.questionnaire);
        lv= (ListView)findViewById(R.id.listview);

        buttonNotActivity = (Button) findViewById(R.id.notactivity);
        buttonNotActivity.setOnClickListener(this);
        buttonNotListed = (Button) findViewById(R.id.notlisted);
        buttonNotListed.setOnClickListener(this);
        buttonHome = (Button) findViewById(R.id.home);
        buttonHome.setOnClickListener(this);
        
        // create the grid item mapping
        String[] from = new String[] {"col_1"};
        int[] to = new int[] { R.id.item1 };
        
        Bundle b = this.getIntent().getExtras();
        String logtime = b.getString("logtime");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(logtime);
	    editor.commit();
        
		String response = b.getString("locations");
		
        // String response = "hello world 1\nhello world 2\nhello world 1\nhello world 2\nhello world 1\nhello world 2\nhello world 1\nhello world 2\nhello world 1\nhello world 2"; 
		String lines[] = response.split("\\r?\\n");
		
        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 1; i < lines.length; i++){
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put("col_1", lines[i]);
        	fillMaps.add(map);
        }

        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.grid_item, from, to);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	
	        @Override
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	        { 
	        	String stuff = lv.getItemAtPosition(position).toString();
	        	String[] name = stuff.split("=");
	            AlertDialog.Builder adb=new AlertDialog.Builder(ATQuestionnaire.this);
	        	adb.setTitle("TravelerServ");
	        	adb.setMessage("Selected Location: "+name[1].replace('}', ' '));
	        	adb.setPositiveButton("Ok", null);
	        	adb.show(); 
	        }
        });
    }

	@Override
	public void onClick(View src) {
		if (src.getId() == R.id.notactivity) {
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
		}
	}
}
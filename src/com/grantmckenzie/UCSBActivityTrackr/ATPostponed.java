package com.grantmckenzie.UCSBActivityTrackr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ATPostponed extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
	private ListView lv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.postponed);
        lv= (ListView)findViewById(R.id.listview);
        
        // create the grid item mapping
        String[] from = new String[] {"col_1"};
        int[] to = new int[] { R.id.item1 };
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String at_postponed_dates = settings.getString("AT_POSTPONED_DATES", "");
        
		String datess[] = at_postponed_dates.split("#");
		
        // prepare the list of all records
        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        for(int i = 1; i < datess.length; i++){
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put("col_1", "Activity at " + datess[i]);
        	fillMaps.add(map);
        }

        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.grid_item, from, to);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	
	        @Override
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
	        { 
	        	
	        }
        });
    }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}
}
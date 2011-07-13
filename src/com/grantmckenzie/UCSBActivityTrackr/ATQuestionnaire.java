/*
 * Project: UCSBActivityTrackr
 * Author: Grant McKenzie
 * Date: May 2011
 * Client: GeoTrans Lab @ UCSB
 * 
 */

package com.grantmckenzie.UCSBActivityTrackr;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ATQuestionnaire extends ListActivity {
	
	  EditText locResults;
	
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);

		  Bundle b = this.getIntent().getExtras();
		  String response = b.getString("locations");
		  String lines[] = response.split("\\r?\\n");
		  lines[0] = " Where are you?\n Please select from the nearby places below:";
		  
		  setListAdapter(new ArrayAdapter<String>(this, R.layout.questionnaire, lines));

		  ListView lv = getListView();
		  lv.setTextFilterEnabled(true);

		  lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view,
		        int position, long id) {
		      // When clicked, show a toast with the TextView text
		      Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
		          Toast.LENGTH_SHORT).show();
		    }
		  });
	  }
}

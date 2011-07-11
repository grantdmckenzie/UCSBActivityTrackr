/*
 * Project: UCSBActivityTrackr
 * Author: Grant McKenzie
 * Date: May 2011
 * Client: GeoTrans Lab @ UCSB
 * 
 */

package com.grantmckenzie.UCSBActivityTrackr;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class ATQuestionnaire extends Activity implements OnClickListener {
	
	  EditText locResults;
	
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.questionnaire); 
		    Bundle b = this.getIntent().getExtras();
		    String i = b.getString("testkey");
		    // Toast.makeText( getApplicationContext(),i,Toast.LENGTH_LONG).show();
		    locResults = (EditText) findViewById(R.id.locResults);
		    locResults.setText(i);
	  }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}

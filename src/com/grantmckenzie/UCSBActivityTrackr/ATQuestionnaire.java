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

public class ATQuestionnaire extends Activity implements OnClickListener {
	
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
		    super.onCreate(savedInstanceState);
		    setContentView(R.layout.questionnaire); 
	  }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}

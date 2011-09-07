/*
 * Project: UCSBActivityTrackr
 * Author: Grant McKenzie
 * Date: May 2011
 * Client: GeoTrans Lab @ UCSB
 * 
 */

package com.grantmckenzie.UCSBActivityTrackr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class UCSBActivityTrackr extends Activity implements OnClickListener {

  Button buttonLogin;
  Button buttonPostponed;
  TextView gpstracker;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main); 
    
    buttonLogin = (Button) findViewById(R.id.Login);
    gpstracker = (TextView) findViewById(R.id.gpstracking);
    buttonLogin.setOnClickListener(this);
    buttonPostponed = (Button) findViewById(R.id.Postponed);
    buttonPostponed.setOnClickListener(this);
  }
 
  public void onClick(View src) {
    if (src.getId() == R.id.Login) {
    	Intent dialogIntent = new Intent(getBaseContext(), ATLogin.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(dialogIntent);
    } else if (src.getId() == R.id.Postponed) {
    	Intent dialogIntent = new Intent(getBaseContext(), ATPostponed.class);
		dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplication().startActivity(dialogIntent);
    }
  }
}
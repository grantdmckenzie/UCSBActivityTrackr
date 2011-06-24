/*
 * Project: UCSBActivityTrackr
 * Author: Grant McKenzie
 * Date: May 2011
 * Client: GeoTrans Lab @ UCSB
 * 
 */

package com.grantmckenzie.UCSBActivityTrackr;

import java.sql.Timestamp;

public class Fix {

		private float lat;
		private float lng;
		private Timestamp ts;
		
		public Fix (float slat, float slng, Timestamp sts) {
			lat = slat;
			lng = slng;
			ts = sts;
		}
		public double getLat() {
			return lat;
		}
		public double getLng() {
			return lng;
		}
		public Timestamp getTs() {
			return ts;
		}
}

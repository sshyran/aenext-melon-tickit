package tickit.cinema;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;
import android.widget.Toast;
import android.provider.Settings.System; 

public class TickitCinemaStarter extends Activity {
	//*********************************************************************************************************************** GLOBAL VARIABLE	
	int						default_movie_id=-1;
	String					default_location="";
	ProgressDialog 			mDialog; 
	boolean					stop_thread=false;
	private LocationManager myLocationManager=null;	    
	//*********************************************************************************************************************** handler
    Handler handler=new Handler() { 
        @Override 
        public void handleMessage(Message msg) {
        	try {
        		switch(msg.what){
        			//Error handling    			    			
    				case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_request_movie_update:
    				case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_build_showtime_buttons:
    				case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_mark_showtimes:        				    			    			    			    			    			    			        				
    				case AenextSQALib.AERR_AenextUtilityLib_BuildDayList: 				
    				case AenextSQALib.AERR_AenextUtilityLib_getHTTPCachedImage:		
    				case AenextSQALib.AERR_AenextUtilityLib_requestHTTPImageWithCached:
    				case AenextSQALib.AERR_AenextUtilityLib_getStringFromDoc:
    				case AenextSQALib.AERR_AenextUtilityLib_format_double:        				        		
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_save_location_date:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_save_bundle:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_restore_location_date:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_restore_bundle:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_sync:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_sync_showtimes:        		
        			case AenextSQALib.AERR_Application:
        			case AenextSQALib.AERR_AenextHTTPLib_http_request:
        			case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_FailToSync:
        				//Stop Movie Update thread
        				stop_thread=true;
        				//Display error message
        				Toast.makeText(TickitCinemaStarter.this, "Sorry, Tickit Cinema service is temporarily unavailable this moment. Please retry later!",Toast.LENGTH_LONG).show();
        				//Exit app
        				finish();        		
        				break;
        			case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_FailToDetermineLocation:
        				Toast.makeText(TickitCinemaStarter.this, "Cannot find your location, please pick your location.",Toast.LENGTH_LONG).show();
        				startActivityForResult(new Intent("com.google.app.tickit.cinema.TickitCinemaPickLocation"),0);
        				break;
        		}
        	}catch(Exception e){
        		//Transmit Error for debug
        		AenextSQALib.transmit_error(AenextSQALib.AERR_Application,"", e);
        	}        	
        } 
      };
	//*********************************************************************************************************************** onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
    		super.onCreate(savedInstanceState);               
    		setContentView(R.layout.starter);    		
    		start_location_listener();    		
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
    	}
    }
  //************************************************************************************************************************* onDestroy
	@Override
	public void onDestroy() {
		try {
			super.onDestroy();			
			//Log.d(this.toString(),"onDestroy");
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
		}		
	}    
	//*********************************************************************************************************************** onPause
	@Override
	public void onPause() {
		try {
			super.onPause();
			//Log.d(this.toString(),"onPause");			
			//Turn off Location notifier
			myLocationManager.removeUpdates(onLocationChange);
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
		}		
	}    
	//*********************************************************************************************************************** onLocationChange	
	LocationListener onLocationChange=new LocationListener() {
		//---------------------------------------------------------------------------------------------- onLocationChanged
		public void onLocationChanged(Location location) {
			try {
				//Display prompt dialog box
				showDialog(0);
				//Perform Sync with server
				perform_sync(null,location.getLatitude(),location.getLongitude(),default_movie_id,TickitCinemaStarter.this);
			}catch(Exception e){
				AenextSQALib.report_error(handler, TickitCinemaStarter.this, AenextSQALib.AERR_Application,"", e);
			}					
		}		
		//---------------------------------------------------------------------------------------------- onProviderDisabled
		public void onProviderDisabled(String provider) {
			try {
				//Log.d("starter","location disabled");    
				showDialog(1);
			}catch(Exception e){
				AenextSQALib.report_error(handler, TickitCinemaStarter.this, AenextSQALib.AERR_Application,"", e);
			}					
		}		
		//---------------------------------------------------------------------------------------------- onProviderEnabled
		public void onProviderEnabled(String provider) {
			// required for interface, not used						
		}		
		//---------------------------------------------------------------------------------------------- onStatusChanged
		public void onStatusChanged(String provider, int status,Bundle extras) {
			// required for interface, not used
		}
	};
	//*********************************************************************************************************************** perform_sync	
	void perform_sync(final String location,final Double lat, final Double lng,final int default_movie_id,final Context ctx){
		try {
	    	Thread background=new Thread(new Runnable() { 
	    		public void run() { 	    		
	            	//Log.d("starter",Double.toString(lat)+","+Double.toString(lng));
	            	//Run movie update
	            	libTickitCinemaMemcached memDB=libTickitCinemaUtilityLib.request_movie_update(handler,ctx,location,lat, lng);	            	
	            	if (stop_thread==false){
	            		//Thread were not stopped
    	            	if (memDB!=null){
    	            		//Data in MemDb is ready, => save them to bundle
                  	        Bundle bundle = new Bundle();
                	        memDB.save_bundle(handler,TickitCinemaStarter.this,bundle);        
                	        memDB=null;                	        
                	        bundle.putInt("default_movie_id",default_movie_id);
                	        //Start movie list activity, attached bundle
                	    	startActivityForResult(new Intent("com.google.app.tickit.cinema.TickitCinemaMovieList").putExtras(bundle),0);            	            		
    	            	}            	            	            	            		
	            	}
	    		} 
	    	});             	   
	    	background.start();    
		}catch(Exception e){
			AenextSQALib.report_error(handler, TickitCinemaStarter.this, AenextSQALib.AERR_Application,"", e);
		}						    
	}	
	//*********************************************************************************************************************** start_location_listener
	void start_location_listener(){
		try {
        	myLocationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        	myLocationManager.requestLocationUpdates("network", 0,1000.0f,onLocationChange);		
		}catch(Exception e){
			AenextSQALib.report_error(handler, TickitCinemaStarter.this, AenextSQALib.AERR_Application,"", e);
		}						    
	}
	//*********************************************************************************************************************** onActivityResult
	public static final int TICKIT_EXIT						=2;
	public static final int TICKIT_RESTART					=1;
	public static final int TICKIT_RETURN_TO_PREV_ACTIVITY	=0;
	
    protected void onActivityResult(int request, int result, Intent i){
    	try {
        	super.onActivityResult(request, result, i);
        	//mDialog.dismiss();        		        	
        	if(result==TICKIT_RESTART){
        		//Request for restart
        		showDialog(0);
        		if (i!=null){
        			//Intent i is not null => extract data
        			//default_movie_id=i.getExtras().getInt("default_movie_id");
        			default_location=i.getExtras().getString("default_location");
        		}        	   
        		if (default_location.equals("")){
        			//Use Location manager if default_location data is blank
        			start_location_listener();
        		}else{
        			//Log.d("starter","Use cached location");
        			//request movie update
        			perform_sync(default_location,0.0,0.0,default_movie_id,TickitCinemaStarter.this);
        		}
        	}else{
        		//Not request for restart => Exit app
        		finish();
        	}
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, TickitCinemaStarter.this, AenextSQALib.AERR_Application,"", e);
    	}        
    }
    ////*********************************************************************************************************************** onCreateDialog
    @Override
    protected Dialog onCreateDialog(int id) {   
    	try{
    		switch(id){
    		case 0:
    			mDialog = new ProgressDialog(TickitCinemaStarter.this);
    			mDialog.setCancelable(false);
    			mDialog.setIcon(R.drawable.alert_dialog_icon);
    			mDialog.setIndeterminate(false);
    			mDialog.setMessage("Please wait while loading...");        
    			mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    			mDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int whichButton) {
    					stop_thread=true;
    					mDialog.dismiss();                	
    					finish();
    				}
    			});
    			break;
    		case 1:
    			return new AlertDialog.Builder(TickitCinemaStarter.this)
    			.setIcon(R.drawable.alert_dialog_icon)
    			.setMessage("To use TickitCinema, you must first turn on a Location source in Settings.\n\nGo to Settings and turn it on now?")
    			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int whichButton) {
    					//Turn on Network location
    					System.putString(getContentResolver(), System.LOCATION_PROVIDERS_ALLOWED, "network"+System.getString(getContentResolver(), System.LOCATION_PROVIDERS_ALLOWED));    					
    					start_location_listener();
    				}
    			})
    			.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int whichButton) {
    					//Exit app
    					finish();
    				}
    			})
    			.create();            
    		}    	
    		return mDialog;
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, TickitCinemaStarter.this, AenextSQALib.AERR_Application,"", e);
    		return null;
    	}
    }
    ////*********************************************************************************************************************** END
}
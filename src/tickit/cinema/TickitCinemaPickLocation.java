package tickit.cinema;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TickitCinemaPickLocation extends ListActivity {		
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// GLOBAL VARIAVLES
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final int 		EXIT_ID 		= Menu.FIRST;			    
    private static final int 		RET_ZIPCODE 	= 0;    
	static int 						counter;
	Context 						ctx;
	String 							lat,lng,zipcode;
	private static String[] 		location_items;
	ProgressDialog 					mProgressDialog;	
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// ACTIVITY
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//##################################################################################################################### handler
    Handler handler=new Handler() { 
        @Override 
        public void handleMessage(Message msg) {
        	try {
        		switch(msg.what){
        			case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_FailToDetermineLocation:
        			case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_FailToSync:
        			case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_request_movie_update:
        			case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_build_showtime_buttons:
        			case libTickitCinemaUtilityLib.ERR_TickitCinemaUtilityLib_mark_showtimes:        				
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_save_location_date:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_save_bundle:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_restore_location_date:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_restore_bundle:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_sync:
        			case libTickitCinemaMemcached.AERR_libTickitCinemaMemcached_sync_showtimes:        				
        			case AenextSQALib.AERR_AenextUtilityLib_BuildDayList: 				
        			case AenextSQALib.AERR_AenextUtilityLib_getHTTPCachedImage:		
        			case AenextSQALib.AERR_AenextUtilityLib_requestHTTPImageWithCached:
        			case AenextSQALib.AERR_AenextUtilityLib_getStringFromDoc:
        			case AenextSQALib.AERR_AenextUtilityLib_format_double:        				
        			case AenextSQALib.AERR_Application:
        				Toast.makeText(TickitCinemaPickLocation.this, "Sorry, My Place service is temporarily unavailable this moment. Please retry later!",Toast.LENGTH_LONG).show();
        				setResult(TickitCinemaStarter.TICKIT_RETURN_TO_PREV_ACTIVITY);
        				finish();
        				break;        				
        		}
        	}catch(Exception e){
        		AenextSQALib.transmit_error(AenextSQALib.AERR_Application,"", e);
        	}
        } 
      };

	//#####################################################################################################################  onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try{
    		super.onCreate(savedInstanceState);         
    		libTickitCinemaMemcached memDB=new libTickitCinemaMemcached();
    		memDB.location.restore(this);
    		counter=memDB.locations.size();
    		location_items= new String[counter];
    		for(int i=0;i<counter;i++){
    			location_items[i]=memDB.locations.get(i).zipcode;    			
    		}                
    		setContentView(R.layout.location_pick);
    		setListAdapter(new EfficientAdapter(this));        
    		ImageButton btnEnter = (ImageButton) findViewById(R.id.location_enter);
    		btnEnter.setImageResource(android.R.drawable.ic_menu_edit);        
    		btnEnter.setOnClickListener(
        		new Button.OnClickListener() { 
        			public void onClick(View v) {         				
        				startActivityForResult(new Intent("com.google.app.tickit.cinema.TickitCinemaEnterZipcode"),RET_ZIPCODE);
        			}
        		}
    		);        
    		ImageButton btnSearch = (ImageButton) findViewById(R.id.location_search);
    		btnSearch.setImageResource(android.R.drawable.ic_menu_mylocation);
    		btnSearch.setOnClickListener(
        		new Button.OnClickListener() { 
        			public void onClick(View v) {         				
        				setResult(1,new Intent().putExtra("default_location",""));
        				finish();  				
        			}
        		}
    		);        
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
    	}    
    }
    //##################################################################################################################### onDestroy
	@Override
	public void onDestroy() {		
		super.onDestroy();
	}
	//##################################################################################################################### onSaveInstanceState
	@Override  
	protected void onSaveInstanceState(Bundle outState) {  
		super.onSaveInstanceState(outState);    
	}  	
	//##################################################################################################################### onResume    
    @Override
	public void onResume() {		
		super.onResume();
	}
    //##################################################################################################################### onPause
	@Override
	public void onPause() {		
		super.onPause();
	}			
	//##################################################################################################################### onStart
	@Override
	public void onStart() {
		super.onStart();
	}
	//##################################################################################################################### onRestart
	@Override
	public void onRestart() {
		super.onRestart();
	}
	//##################################################################################################################### onStop
	@Override
	public void onStop() {	
		super.onStop();	
	}    
    //##################################################################################################################### onActivityResult
    @Override
    protected void onActivityResult(int request, int result, Intent i){
    	try{
	    	switch(result){
        	case TickitCinemaStarter.TICKIT_RETURN_TO_PREV_ACTIVITY:
        		break;
        	case TickitCinemaStarter.TICKIT_RESTART:
        	case TickitCinemaStarter.TICKIT_EXIT:    
        		setResult(result,i);
        		finish();
        		break;    	            			
	    	}    	
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
		}    	
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// CONTEXT MENU
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    //##################################################################################################################### onCreateContextMenu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {populateMenu(menu);}
	//##################################################################################################################### onCreateOptionsMenu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {populateMenu(menu);return(super.onCreateOptionsMenu(menu));}
	//##################################################################################################################### onOptionsItemSelected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {return(applyMenuChoice(item) ||super.onOptionsItemSelected(item));}
	//##################################################################################################################### onContextItemSelected
	@Override
	public boolean onContextItemSelected(MenuItem item) {return(applyMenuChoice(item) ||super.onContextItemSelected(item));}
	//##################################################################################################################### populateMenu
	private void populateMenu(Menu menu) {
		try{
			menu.add(Menu.NONE, EXIT_ID, Menu.NONE, "Exit");
			menu.findItem(EXIT_ID).setIcon(android.R.drawable.ic_menu_close_clear_cancel);	
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
		}
	}	
	//##################################################################################################################### applyMenuChoice
	private boolean applyMenuChoice(MenuItem item) {
		try{
			switch (item.getItemId()) {
			case EXIT_ID:
				setResult(TickitCinemaStarter.TICKIT_EXIT);
				finish();
				return(true);						
			}
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
		}		
		return(false);
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// LISTVIEW
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//##################################################################################################################### onListItemClick
    public void onListItemClick(ListView parent, View v, int position,  long id) {
    	try{
    		setResult(TickitCinemaStarter.TICKIT_RESTART,new Intent().putExtra("default_location", location_items[position]));				
    		finish();    			
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,"", e);
		}		
    }	
	//##################################################################################################################### EfficientAdapter
    private class EfficientAdapter extends BaseAdapter {
    	//------------------------------------------------------------------------------------- Variables
    	private LayoutInflater mInflater;
        private Context gcontext;
        //------------------------------------------------------------------------------------- EfficientAdapter
        public EfficientAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            gcontext = context;
        }
        //------------------------------------------------------------------------------------- getCount
        public int getCount() { return counter; }
        //------------------------------------------------------------------------------------- getItem
        public Object getItem(int position) { return position;}
        //------------------------------------------------------------------------------------- getItemId
        public long getItemId(int position) { return position;}
        //------------------------------------------------------------------------------------- getView
        public View getView(int position, View convertView, ViewGroup parent) {
        	try{
        		ViewHolder holder;
        		if (convertView == null) {
        			convertView = mInflater.inflate(R.layout.location_pick_row, null);                
        			holder = new ViewHolder();
        			holder.text = (TextView) convertView.findViewById(R.id.text);
        			holder.icon = (ImageView) convertView.findViewById(R.id.icon);
        			convertView.setTag(holder);
        		} else {
        			holder = (ViewHolder) convertView.getTag();
        		}
        		holder.text.setText(location_items[position]);   
        		holder.icon.setImageBitmap(BitmapFactory.decodeResource(gcontext.getResources(), R.drawable.map_pin_48));            
        		return convertView;
        	}catch(Exception e){
        		AenextSQALib.report_error(handler, TickitCinemaPickLocation.this, AenextSQALib.AERR_Application,"", e);
        		convertView = mInflater.inflate(android.R.layout.activity_list_item, null);        		
        		return convertView;        		
        	}
        }
        //------------------------------------------------------------------------------------- ViewHolder
        class ViewHolder {
            TextView text;
            ImageView icon;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// END
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
package tickit.cinema;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

//import tickit.cinema.libTickitCinemaMemcached.Showtime;

//import android.util.Log;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
//import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
//import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;


public class TickitCinemaMovieDetail extends ListActivity 
{
	//***************************************************************************************************************** GLOBAL VARIABLES	
	private static String 	name;
	private static Bitmap 	image;
	private static String	duration;
	private static String	standard_rating;
	//private static String	trailer_url;
	private static long		release_date;
	private static String	plot;
	private static Float	imdb_rating;
	private static String	mid;			
	private static String	genres;
	private static String	current_location;
			
	ProgressDialog 						mProgressDialog; 	
	static libTickitCinemaMemcached 	memDB;
	private static int					listview_count;

	public static final int 		LOCATION_ID 	= Menu.FIRST+4;		
	public static final int 		REFRESH_ID 		= Menu.FIRST+5;
	public static final int 		EXIT_ID 		= Menu.FIRST+6;		

	private static final int MSG_REDRAW				=0;
	
    //***************************************************************************************************************** handler
    Handler handler=new Handler() { 
        @Override 
        public void handleMessage(Message msg) {
        	try {
        		switch(msg.what){
        			case MSG_REDRAW:        				
        				getListView().invalidateViews();
        				break;
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
        				Toast.makeText(TickitCinemaMovieDetail.this, "Sorry, this movie is temporarily not available. Please retry later!",Toast.LENGTH_LONG).show();
        				setResult(TickitCinemaStarter.TICKIT_RETURN_TO_PREV_ACTIVITY);
        				finish();
        				break;
        			case AenextSQALib.AERR_AenextHTTPLib_http_request:        				
        				Toast.makeText(TickitCinemaMovieDetail.this, "Sorry, unable to get showtime info. PLease retry later!",Toast.LENGTH_SHORT).show();
        				break;
        		}     
        	}catch(Exception e){
        		//Transmit Error for debug
        		AenextSQALib.transmit_error(AenextSQALib.AERR_Application,current_location, e);
        	}        	    		
		}
      };
	//***************************************************************************************************************** onCreate
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);			
			setContentView(R.layout.movie_detail);
		
			//**************************************************************************** RESTORE DATA
			//Get bundle data from Intent Extra
			Bundle bundle = this.getIntent().getExtras();
			//Save movie's properties data for reuse in slow adapter		
			
			name				=	bundle.getString("Movie_name");			
			image				=	AenextUtilityLib.requestHTTPImageWithCached(handler,this,bundle.getString("Movie_image_url"));
			duration			=	bundle.getString("Movie_duration");
			standard_rating		=	bundle.getString("Movie_standard_rating");
			//trailer_url			=	bundle.getString("Movie_trailer_url");
			release_date		=	bundle.getLong("Movie_release_date");
			plot				=	bundle.getString("Movie_movie_plot");
			imdb_rating			=	bundle.getFloat("Movie_imdb_rating");
			mid					=	bundle.getString("Movie_mid");			
			genres				=	bundle.getString("Movie_genre");
			current_location	=	bundle.getString("Movie_current_location");
			
			/*Bitmap abc;
			int[] i;
			
			abc=BitmapFactory.decodeResource(this.getResources(), R.drawable.no_image);
			abc.getPixels(i, 0, 25, 0, 0, 25, 25);
			image.setPixels(i, 0, 25, 0, 0, 25, 25);*/
			
			memDB= new libTickitCinemaMemcached();
			//Display title with current location
			setTitle(current_location);
			
			
			//**************************************************************************** RENDER DATE SPINNER
			//Initialize day list variable with 4 day slot 
			String[] str_dates=new String[4];
			//Using the today date to build day list after that
			str_dates=AenextUtilityLib.BuildDayList(handler,this,7);			
			//Setup spinner controller
			Spinner spinner_date = (Spinner) findViewById(R.id.spinner_date);
			ArrayAdapter<CharSequence> spinnerArrayAdapter = new ArrayAdapter<CharSequence>(this,android.R.layout.simple_spinner_item,str_dates); 
			spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner_date.setAdapter(spinnerArrayAdapter);
			//Build spinner click listener
			spinner_date.setOnItemSelectedListener(
					new Spinner.OnItemSelectedListener(){
						public void onItemSelected(final AdapterView<?> parent, View v, final int position, long id) {
								//Log.d("TEST",parent.getSelectedItem().toString());
								//save selected date from spinner to current_date variable
								//String current_date=parent.getSelectedItem().toString().split("-")[1];
								//define http request thread
								memDB.theaters.clear();
								memDB.showtimes.clear();
								listview_count=2;
								Message m = new Message();
								m.what = MSG_REDRAW; 
								handler.sendMessage(m);
								
								
								Thread background=new Thread(new Runnable() { 
									public void run() { 
										try {              											
											memDB.cache_theater_detail(handler, TickitCinemaMovieDetail.this,mid, current_location, position);
											listview_count=memDB.theaters.size()+1;
											//Process completed, notify handler to refresh ListView
											Message m = new Message();
											m.what = MSG_REDRAW; 
											handler.sendMessage(m);
										} 
										catch (Exception e) {
											AenextSQALib.report_error(handler, TickitCinemaMovieDetail.this, AenextSQALib.AERR_Application,current_location, e);
										} 
									} 
								});   
								//start background thread
								background.start();             	            				
						}			
						//Nothing selected, do nothing
						public void onNothingSelected(AdapterView<?> arg0) {}        				
					}
			);                
			//**************************************************************************** RENDER LISTVIEW
			setListAdapter(new EfficientAdapter(this));
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,current_location, e);
		}
	}
	//***************************************************************************************************************** onSaveInstanceState
	@Override  
	protected void onSaveInstanceState(Bundle outState) {  
		super.onSaveInstanceState(outState);  
	}  	
	//***************************************************************************************************************** onDestroy		
	@Override
	public void onDestroy() {
		super.onDestroy();	
	}
	//***************************************************************************************************************** onResume	
	@Override
	public void onResume() {
		try {
			super.onResume();
/*			Calendar today=Calendar.getInstance();		
			if((today.get(Calendar.YEAR)		>memDB.current_year)
					||(today.get(Calendar.MONTH)		>memDB.current_month)
					||(today.get(Calendar.DAY_OF_MONTH)!= memDB.current_day)){			
				Intent i=new Intent();
				//i.putExtra("default_location", memDB.current_location);
				//i.putExtra("default_movie_id", movie_id);
				setResult(1,i);
				finish();
			}*/		
//			memDB.showtimes=libTickitCinemaUtilityLib.mark_showtimes(handler,this,Calendar.getInstance(),memDB.showtimes);
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,current_location, e);
		}
	}
	//***************************************************************************************************************** onPause	
	@Override
	public void onPause() {
		super.onPause();				
	}			
	//***************************************************************************************************************** onStop	
	@Override
	public void onStop() {		
		super.onStop();		
	}				  
    //***************************************************************************************************************** onCreateDialog
    @Override
    protected Dialog onCreateDialog(int id) {
    	try {
    		mProgressDialog = new ProgressDialog(TickitCinemaMovieDetail.this);
    		mProgressDialog.setCancelable(true);
    		mProgressDialog.setIcon(R.drawable.alert_dialog_icon);
    		mProgressDialog.setIndeterminate(false);
    		mProgressDialog.setMessage("Please wait while loading...");        
    		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		mProgressDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				mProgressDialog.dismiss();
    			}
    		});        
    		return mProgressDialog;
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,current_location, e);
    		return null;
    	}
    }	
	//***************************************************************************************************************** onListItemClick    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {    	
    	super.onListItemClick(l, v, position, id);
    
    }
	//***************************************************************************************************************** EfficientAdapter	
    private class EfficientAdapter extends BaseAdapter {
        //------------------------------------------------------------------------------------------ Variables Declaration
        private LayoutInflater mInflater;                
        //------------------------------------------------------------------------------------------ EfficientAdapter
        public EfficientAdapter(Context context) {
            mInflater = LayoutInflater.from(context);            
        }
        //------------------------------------------------------------------------------------------ getCount, getItem, getItemId       
        public int getCount() { return listview_count;}
        public Object getItem(int position) {return position;}
        public long getItemId(int position) {return position;}
        //------------------------------------------------------------------------------------------ getView       
        public View getView(final int position, View convertView, ViewGroup parent) {
        	try {
        		ViewHolder holder;            
        		if (position==0){//Render Movie Detail Item            	        			
        			convertView = mInflater.inflate(R.layout.movie_detail_first_row, null);
        			holder = new ViewHolder();
        			holder.title 					= (TextView) convertView.findViewById(R.id.name);
        			holder.duration_standard_rating = (TextView) convertView.findViewById(R.id.duration_standard_rating);
        			holder.image 					= (ImageView) convertView.findViewById(R.id.movie_image);            	     	
        			holder.imdb_rating 				= (RatingBar) convertView.findViewById(R.id.imdb_rating);
        			holder.plot						= (TextView) convertView.findViewById(R.id.plot);
        			holder.release_date				= (TextView) convertView.findViewById(R.id.release_date);
        			holder.genres					= (TextView) convertView.findViewById(R.id.genres);            	
        			convertView.setTag(holder);
            	
        			holder.title.setText(name);
        			holder.image.setImageBitmap(image); 
        			holder.image.setOnClickListener(
    					new Button.OnClickListener() { 
    						public void onClick(View v) {    								
    							startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(memDB.movies.get(position).trailer_url))); 
    						}
    					}
        			);
        			
        			holder.duration_standard_rating.setText(duration+" | "+standard_rating);                
        			holder.plot.setText(plot);                
        			//Release date
        			if (release_date==0){
        				holder.release_date.setText("");
        			}else{            		
        				DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        				Calendar date = Calendar.getInstance();
        				date.setTimeInMillis(release_date*1000);
        				holder.release_date.setText(df.format(date.getTime()));            		
        			}
        			//Genre
        			holder.genres.setText(genres);                                
        			holder.imdb_rating.setRating(imdb_rating);
        		}
        		else {//Render Theater Detail Item
        			//Init view
        			convertView 		= mInflater.inflate(R.layout.movie_detail_theater_row, null);        			
        			holder 				= new ViewHolder();
        			holder.title 		= (TextView) convertView.findViewById(R.id.theater_name);
        			holder.map 			= (ImageButton) convertView.findViewById(R.id.theater_map);
        			holder.showtimes		= (TextView) convertView.findViewById(R.id.showtimes);                                                
        			convertView.setTag(holder);
        			//Set data
        			//Theater Map Opener
        			holder.map.setBackgroundColor(Color.TRANSPARENT);
        			if ((memDB.theaters!=null)&&(memDB.theaters.size()>0)){
        				holder.map.setImageBitmap(BitmapFactory.decodeResource(TickitCinemaMovieDetail.this.getResources(), R.drawable.map_pin_48));
            			holder.map.setId(position);
            			holder.map.setOnClickListener(
    						new Button.OnClickListener() { 
    							public void onClick(View v) {
    								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="+memDB.theaters.get(v.getId()-1).address)));
    								//startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.youtube.com/watch?v=hr32TzpOA9k"))); 
    							}
    						}
            			);
        				holder.title.setText(memDB.theaters.get(position-1).name);
        				
        				String strShowtime="";
        				ArrayList<libTickitCinemaMemcached.Showtime> obj_showtimes=memDB.theater.showtimes(memDB.theaters.get(position-1).id);        				
        				for(int i=0;i<obj_showtimes.size();i++){
        					strShowtime=strShowtime+obj_showtimes.get(i).time+ "  ";
        				}
        				holder.showtimes.setText(strShowtime);
        			}        			
        		}                        
        		return convertView;
        	}catch(Exception e){       
        		AenextSQALib.report_error(handler, TickitCinemaMovieDetail.this, AenextSQALib.AERR_Application,current_location, e);
        		convertView = mInflater.inflate(android.R.layout.activity_list_item, null);        		
        		return convertView;
        	}
        }
        //------------------------------------------------------------------------------------------ Class ViewHolder
        class ViewHolder {
            TextView 	title;
            TextView 	showtimes;
            TextView 	duration_standard_rating;             
            TextView 	plot; 
            TextView 	release_date;
            TextView 	genres;
            ImageButton map;
            RatingBar 	imdb_rating;
            GridView 	grid;            
            ImageView	image;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onCreateContextMenu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
		populateMenu(menu);
	}	
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onCreateOptionsMenu	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		populateMenu(menu);return(super.onCreateOptionsMenu(menu));
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onOptionsItemSelected	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return(applyMenuChoice(item) ||super.onOptionsItemSelected(item));
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onContextItemSelected	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return(applyMenuChoice(item) ||super.onContextItemSelected(item));
	}	
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// populateMenu	
	private void populateMenu(Menu menu) {
		try {
			menu.add(Menu.NONE, REFRESH_ID, 	Menu.NONE, "My Location");
			menu.add(Menu.NONE, LOCATION_ID, 	Menu.NONE, "My Places");				
			menu.add(Menu.NONE, EXIT_ID, 		Menu.NONE, "Exit");		
		
			menu.findItem(LOCATION_ID).setIcon(android.R.drawable.ic_menu_myplaces);
			menu.findItem(REFRESH_ID).setIcon(android.R.drawable.ic_menu_mylocation);
			menu.findItem(EXIT_ID).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,current_location, e);
		}		
	}	
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// applyMenuChoice	
	private boolean applyMenuChoice(MenuItem item) {
		try {
			switch (item.getItemId()) {
				case REFRESH_ID:
					setResult(TickitCinemaStarter.TICKIT_RESTART,new Intent().putExtra("default_location", current_location));
					finish();
					return(true);				
				case EXIT_ID:
					setResult(TickitCinemaStarter.TICKIT_EXIT);
					finish();
					return(true);
				case LOCATION_ID:
					startActivityForResult(new Intent(this,TickitCinemaPickLocation.class),1);
					return(true);						
			}
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,current_location, e);
		}
		return(false);
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onActivityResult
    protected void onActivityResult(int request, int result, Intent i){
    	try {
    		super.onActivityResult(request, result, i);
    	    switch(result){
    	        case TickitCinemaStarter.TICKIT_RETURN_TO_PREV_ACTIVITY:
    	        	break;
    	        case TickitCinemaStarter.TICKIT_RESTART:
    	        case TickitCinemaStarter.TICKIT_EXIT:    
    	        	//i.putExtra("default_movie_id", movie_id);
    	        	setResult(result,i);
    	        	finish();
    	        	break;    	            			
    		}
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,current_location, e);
    	}
    }

    //***************************************************************************************************************** END	
}

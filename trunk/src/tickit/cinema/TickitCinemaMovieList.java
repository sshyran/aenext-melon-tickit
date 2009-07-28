package tickit.cinema;

import java.text.DateFormat;
import java.util.Calendar;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
//import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import android.os.Message; 


public class TickitCinemaMovieList extends ListActivity implements OnScrollListener
{   ////////////////////////////////////////////////////////////////////////////////////////////////////////////// GLOBAL VARIABLES
	public static final int 		GENRE_ID 		= Menu.FIRST+1;
	public static final int 		RELEASEDATE_ID 	= Menu.FIRST+2;		
	public static final int 		RATING_ID 		= Menu.FIRST+3;
	public static final int 		LOCATION_ID 	= Menu.FIRST+4;		
	public static final int 		REFRESH_ID 		= Menu.FIRST+5;
	public static final int 		EXIT_ID 		= Menu.FIRST+6;		
	

	libTickitCinemaMemcached 	memDB;    
    private boolean 			mBusy = false;
    Thread 						background;
    ProgressDialog				mProgressDialog;
    boolean						stop_thread=false;
    
    private static final int MSG_REDRAW				=0;        
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// handler
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
        				Toast.makeText(TickitCinemaMovieList.this, "Sorry, Tickit Cinema service is temporarily unavailable this moment. Please retry later!",Toast.LENGTH_LONG).show();
        				setResult(TickitCinemaStarter.TICKIT_EXIT);
        				finish();
        				break;
        				
        		}
        	}catch(Exception e){
        		AenextSQALib.transmit_error(AenextSQALib.AERR_Application,memDB.current_location, e);
        	}
        } 
      };
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onCreate
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
    		super.onCreate(savedInstanceState);
    		Bundle bundle = this.getIntent().getExtras();
    		memDB = new libTickitCinemaMemcached();
    		if (savedInstanceState==null) {  
    			//Log.d("MovieList", "Normal start");                    	
    			memDB.restore_bundle(handler,this,bundle);
    		}  
    		else {  
    			//Log.d("MovieList", "Rotated start");  
    			memDB.restore_bundle(handler,this,savedInstanceState);
    		}         
    		    		
    		setTitle(memDB.current_location);
		
    		setContentView(R.layout.movie_list);
    		setListAdapter(new SlowAdapter(TickitCinemaMovieList.this));
    		getListView().setOnScrollListener((OnScrollListener) TickitCinemaMovieList.this);
    		registerForContextMenu(getListView());
    		
    		//Start request for movie detail in parallel
    		for(int i=0;i<memDB.movies.size();i++){
    					final int pos=i;
    					background=new Thread(new Runnable() { 
    					public void run() {
	    					libTickitCinemaMemcached.Movie movie=memDB.cache_movie_detail(handler, TickitCinemaMovieList.this, memDB.movies.get(pos).mid);        	    				
	    					memDB.movies.get(pos).movie_imdb_rating	=	movie.movie_imdb_rating;
	    					memDB.movies.get(pos).image_url			=	movie.image_url;
	    					memDB.movies.get(pos).image_bmp			=	AenextUtilityLib.requestHTTPImageWithCached(handler,TickitCinemaMovieList.this,movie.image_url);
	    					if (memDB.movies.get(pos).image_bmp==null){
	    						memDB.movies.get(pos).image_bmp=BitmapFactory.decodeResource(TickitCinemaMovieList.this.getResources(), R.drawable.no_image);
	    					}
	    					memDB.movies.get(pos).movie_plot		=	movie.movie_plot;        	    				
	    				
	    					Message m = new Message();
	    					m.what = MSG_REDRAW; 
	    					handler.sendMessage(m);	                  
	    					} 
    					});             	   
    					background.start();        				        				
    		}
    		
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
    	}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onDestroy 
	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
		}
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onSaveInstanceState
	@Override  
	protected void onSaveInstanceState(Bundle outState) {
		try{
			super.onSaveInstanceState(outState);  
			memDB.save_bundle(handler,this,outState);
	    	//Log.d("RotationDemo", "got to Parent onSaveInstanceState()");  
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
		}
	}  	
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onResume 
    @Override
	public void onResume() {		
    	try {
    		super.onResume();
    		//Log.d("MovieList","on Resume");					
    		Calendar today=Calendar.getInstance();		
    		if((today.get(Calendar.YEAR)		>memDB.current_year)
    				||(today.get(Calendar.MONTH)		>memDB.current_month)
    				||(today.get(Calendar.DAY_OF_MONTH)!= memDB.current_day)){
    			setResult(1,new Intent().putExtra("default_location", memDB.current_location));
    			finish();
    		}
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
    	}
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
    				setResult(result,i);
    				finish();
    				break;
    		}
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
    	}
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onPause	
	@Override
	public void onPause() {
		try{
			super.onPause();
			//Log.d("MovieList","onPause");
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
		}
	}			
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onStart
	@Override
	public void onStart() {
		try{
			super.onStart();
			//Log.d("MovieList","onStart");
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
		}
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onRestart	
	@Override
	public void onRestart() {
		try{
			super.onRestart();
			//Log.d("MovieList","onRestart");
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
		}
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onStop
	@Override
	public void onStop() {
		try{
			super.onStop();	
			//Log.d("MovieList","onStop");
		}catch(Exception e){
			AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
		}
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onScroll
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,int totalItemCount) {
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onScrollStateChanged
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    	try {
    		switch (scrollState) {
    		case OnScrollListener.SCROLL_STATE_IDLE:
    			break;
    		case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
    			break;
    		case OnScrollListener.SCROLL_STATE_FLING:
    			break;
    		}
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
    	}        
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// onListItemClick    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	try{
    		super.onListItemClick(l, v, position, id);
    		Intent intent = new Intent(this,TickitCinemaMovieDetail.class);		
    		Bundle bundle = new Bundle();    	
    		
			bundle.putString("Movie_name"				,memDB.movies.get(position).name);
			bundle.putString("Movie_image_url"			,memDB.movies.get(position).image_url);
			bundle.putString("Movie_duration"			,memDB.movies.get(position).duration);
			bundle.putString("Movie_imdb_url"			,memDB.movies.get(position).imdb_url);
			bundle.putString("Movie_standard_rating"	,memDB.movies.get(position).standard_rating);
			bundle.putString("Movie_trailer_url"		,memDB.movies.get(position).trailer_url);
			bundle.putLong  ("Movie_release_date"		,memDB.movies.get(position).release_date);
			bundle.putString("Movie_movie_plot"	 	    ,memDB.movies.get(position).movie_plot);
			bundle.putFloat ("Movie_imdb_rating"		,memDB.movies.get(position).movie_imdb_rating);
			bundle.putString("Movie_mid"				,memDB.movies.get(position).mid);
			bundle.putString("Movie_genre"				,memDB.movies.get(position).genres);
    		bundle.putString("Movie_current_location"	,memDB.current_location);
					
    		intent.putExtras(bundle);
    		startActivityForResult(intent,0);     
    	}catch(Exception e){
    		AenextSQALib.report_error(handler, this, AenextSQALib.AERR_Application,memDB.current_location, e);
    	}    	
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// CLASS SlowAdapter
    private class SlowAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private Context ctx;
        
        public SlowAdapter(Context context) {
        	mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	ctx = context;
        }
        public int getCount() {return memDB.movies.size();}
        public Object getItem(int position) {return position;}
        public long getItemId(int position) {return position;}
        public View getView(final int position, View convertView, ViewGroup parent) {
        	try {
        		ViewHolder holder = new ViewHolder();            
        		if (convertView == null) {
        			convertView = mInflater.inflate(R.layout.movie_list_row, null); 
        			holder.title 			= (TextView) convertView.findViewById(R.id.name);
        			holder.standard_rating 	= (TextView) convertView.findViewById(R.id.duration_standard_rating);
        			holder.release_date 	= (TextView) convertView.findViewById(R.id.release_date);
        			holder.genres 			= (TextView) convertView.findViewById(R.id.genres);
        			holder.movie_image 		= (ImageView) convertView.findViewById(R.id.movie_image);
        			holder.imdb_rating 		= (RatingBar) convertView.findViewById(R.id.imdb_rating);
        			holder.imdb_rating_text	= (TextView) convertView.findViewById(R.id.imdb_rating_text);
        		}
        		if (!mBusy) {
        			holder.title 			= (TextView) convertView.findViewById(R.id.name);
        			holder.standard_rating 	= (TextView) convertView.findViewById(R.id.duration_standard_rating);
        			holder.release_date 	= (TextView) convertView.findViewById(R.id.release_date);
        			holder.genres 			= (TextView) convertView.findViewById(R.id.genres);                
        			holder.movie_image 		= (ImageView) convertView.findViewById(R.id.movie_image);
        			holder.imdb_rating 		= (RatingBar) convertView.findViewById(R.id.imdb_rating);
        			holder.imdb_rating_text	= (TextView) convertView.findViewById(R.id.imdb_rating_text);
            	
        			holder.title.setText(memDB.movies.get(position).name);       
        			if ((memDB.movies.get(position).duration!=null)&&(memDB.movies.get(position).duration.equals(""))||
        				(memDB.movies.get(position).standard_rating!=null)&&(memDB.movies.get(position).standard_rating.equals(""))){
        				
        				holder.standard_rating.setText(memDB.movies.get(position).duration + memDB.movies.get(position).standard_rating);
        			}else{
        				String t_duration="";
        				if (memDB.movies.get(position).duration!=null) {
        					t_duration=memDB.movies.get(position).duration;
        				}
        				String t_standard_rating="";
        				if(memDB.movies.get(position).standard_rating!=null) {
        					t_standard_rating=memDB.movies.get(position).standard_rating;
        				}
        				if (t_duration.equals("")||t_standard_rating.equals("")){
        					holder.standard_rating.setText(t_duration + t_standard_rating);
        				}else{
        					holder.standard_rating.setText(t_duration + " | " + t_standard_rating);	
        				}        				        				
        			}
        			
        			/////////////////////////////////////////// COVER IMAGE        			
        			if ((memDB.movies.get(position).trailer_url!=null)&&(!memDB.movies.get(position).trailer_url.equals(""))){
        				holder.movie_image.setOnClickListener(
        					new Button.OnClickListener() { 
        						public void onClick(View v) {    								
        							startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(memDB.movies.get(position).trailer_url))); 
        						}
        					}
            			);
        			}
        			if (memDB.movies.get(position).image_bmp!=null){
        				holder.movie_image.setImageDrawable(new BitmapDrawable(AenextUtilityLib.playable_image_marker(handler,ctx,memDB.movies.get(position).image_bmp,memDB.movies.get(position).trailer_url)));
        			}else{
        				holder.movie_image.setImageDrawable(new BitmapDrawable(memDB.movies.get(position).image_bmp));
        			}
        			        			
        			//Imdb rating
        			if(memDB.movies.get(position).movie_imdb_rating==-1.0){
        				holder.imdb_rating.setRating((float) 0);
        				holder.imdb_rating_text.setText("(no ratings)");            			
        			}else{
        				holder.imdb_rating.setRating(memDB.movies.get(position).movie_imdb_rating);
        				holder.imdb_rating_text.setText(Float.toString(memDB.movies.get(position).movie_imdb_rating));
        			}
        			//Release date
        			if (memDB.movies.get(position).release_date==0){
        				holder.release_date.setText("");
        			}else{            		
        				DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        				Calendar date = Calendar.getInstance();
        				date.setTimeInMillis(memDB.movies.get(position).release_date*1000);
        				holder.release_date.setText(df.format(date.getTime()));            		
        			}
        			//Genre        			
        			holder.genres.setText(memDB.movies.get(position).genres);
        			///////////////////////////////////////////////////////
        			holder.title.setTag(null);
        			holder.movie_image.setTag(null);
        			holder.imdb_rating.setTag(null);
        			holder.release_date.setTag(null);
        			holder.genres.setTag(null);
        			///////////////////////////////////////////         			
        		}
        		return convertView;
        	}catch(Exception e){
        		AenextSQALib.report_error(handler, TickitCinemaMovieList.this, AenextSQALib.AERR_Application,memDB.current_location, e);
        		convertView = mInflater.inflate(android.R.layout.activity_list_item, null);        		
        		return convertView;        		
        	}        		
        }        		
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// CLASS ViewHolder
    class ViewHolder 
    {
        TextView title;
        TextView standard_rating;
        TextView release_date;
        TextView genres;
        ImageView movie_image;
        RatingBar imdb_rating;
        TextView imdb_rating_text;
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
		try{		
			menu.add(Menu.NONE, RELEASEDATE_ID, Menu.NONE, "New Release");
			menu.add(Menu.NONE, RATING_ID, 		Menu.NONE, "Top Rated");
			menu.add(Menu.NONE, REFRESH_ID, 	Menu.NONE, "My Location");
			menu.add(Menu.NONE, LOCATION_ID, 	Menu.NONE, "My Places");				
			menu.add(Menu.NONE, EXIT_ID, 		Menu.NONE, "Exit");	
		
			menu.findItem(RELEASEDATE_ID).setIcon(android.R.drawable.ic_menu_my_calendar);
			menu.findItem(RATING_ID).setIcon(android.R.drawable.btn_star_big_off);
			menu.findItem(LOCATION_ID).setIcon(android.R.drawable.ic_menu_myplaces);
			menu.findItem(REFRESH_ID).setIcon(android.R.drawable.ic_menu_mylocation);
			menu.findItem(EXIT_ID).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		}catch(Exception e){
			AenextSQALib.report_error(handler, TickitCinemaMovieList.this, AenextSQALib.AERR_Application,memDB.current_location, e);
		}
	}	
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// applyMenuChoice	
	private boolean applyMenuChoice(MenuItem item) {
		try{
			switch (item.getItemId()) {
			case RELEASEDATE_ID:
				memDB.movie.sort_by_release_date_desc();
				getListView().invalidateViews();
				return(true);

			case RATING_ID:
				memDB.movie.sort_by_imdb_rated_desc();
				getListView().invalidateViews();
				return(true);
				
			case REFRESH_ID:
				setResult(TickitCinemaStarter.TICKIT_RESTART,new Intent().putExtra("default_location", memDB.current_location));				
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
			AenextSQALib.report_error(handler, TickitCinemaMovieList.this, AenextSQALib.AERR_Application,memDB.current_location, e);
		}
		return(false);
	}
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////// END   
}


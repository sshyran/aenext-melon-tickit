package tickit.cinema;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.w3c.dom.Document;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
//import android.util.Log;
import android.os.Bundle;
import android.os.Handler;

public class libTickitCinemaMemcached {		
	//############################################################################################################### GLOBAL VARIABLE
	final static String SMALL_SEPARATOR = "_";
	final static String BIG_SEPARATOR 	= "~";
	public final int	UNDEFINE		=-1;
	
	public ArrayList<Movie> 	movies 		= new ArrayList<Movie>();
	public ArrayList<Location> 	locations 	= new ArrayList<Location>();
	public ArrayList<Theater>	theaters	= new ArrayList<Theater>();
	public ArrayList<Showtime>	showtimes	= new ArrayList<Showtime>();
	
	public Movie 		movie  		= new Movie();
	public Location		location	= new Location();
	public Theater		theater		= new Theater();
	public Showtime		showtime	= new Showtime();	
	
	public Double		geo_lat;
	public Double		geo_lng;
	public String		current_location;
	public int		current_day;
	public int		current_month;
	public int		current_year;
	
	public static final int AERR_libTickitCinemaMemcached_save_location_date	=	10000+0+1;
	public static final int AERR_libTickitCinemaMemcached_save_bundle			=	10000+0+2;
	public static final int AERR_libTickitCinemaMemcached_restore_location_date	=	10000+0+3;
	public static final int AERR_libTickitCinemaMemcached_restore_bundle		=	10000+0+4;
	public static final int AERR_libTickitCinemaMemcached_sync					=	10000+0+5;
	public static final int AERR_libTickitCinemaMemcached_sync_showtimes		=	10000+0+6;
	
	//############################################################################################################### TickitCinemaMemcached
	public libTickitCinemaMemcached(){}
	//############################################################################################################### save_location
	public void save_location_date(Handler handler,Context ctx, Bundle bundle){
		try {
			bundle.putDouble("geo_lat", geo_lat);
			bundle.putDouble("geo_lng", geo_lng);
			bundle.putString("current_location", current_location);	
			bundle.putInt("current_day", 	current_day);
			bundle.putInt("current_month", 	current_month);
			bundle.putInt("current_year", 	current_year);
		}catch(Exception e){
			AenextSQALib.report_error(handler, ctx, AERR_libTickitCinemaMemcached_save_location_date,"", e);
		}
	}
	//############################################################################################################### save_bundle
	public void save_bundle(Handler handler, Context ctx, Bundle bundle){
		try {
			movie.save_bundle(movies, bundle);
			//movie_theater.save_bundle(movies_theaters, bundle);
			save_location_date(handler,ctx,bundle);
		}catch(Exception e){
			AenextSQALib.report_error(handler, ctx, AERR_libTickitCinemaMemcached_save_bundle,"", e);
		}
	}
	//############################################################################################################### restore_location
	public void restore_location_date(Handler handler, Context ctx,Bundle bundle){
		try {
			geo_lat=bundle.getDouble("geo_lat");
			geo_lng=bundle.getDouble("geo_lng");
			current_location=bundle.getString("current_location");	
			current_day		=bundle.getInt("current_day");
			current_month	=bundle.getInt("current_month");
			current_year	=bundle.getInt("current_year");
		}catch(Exception e){
			AenextSQALib.report_error(handler, ctx, AERR_libTickitCinemaMemcached_restore_location_date,"", e);
		}
	}
	//############################################################################################################### restore_bundle
	public void restore_bundle(Handler handler, Context ctx, Bundle bundle){
		try {
			movie.restore_bundle(bundle);
			restore_location_date(handler,ctx, bundle);
		}catch(Exception e){
			AenextSQALib.report_error(handler, ctx, AERR_libTickitCinemaMemcached_restore_bundle,"", e);
		}
	}
	//############################################################################################################### cache_movie_list
	public libTickitCinemaMemcached cache_movie_list(Handler handler, Context ctx, String strLocation, int date_offset)	
	{       
		String URI="";
		try {			
							
			URI = AenextHTTPLib.ROOT_URI 	+ "/tickit_cinema_requests/request_movie_list?location="
											+URLEncoder.encode(strLocation, "UTF-8")+"&date_offset="
											+Integer.toString(date_offset);
			
			Document doc = AenextHTTPLib.http_request(handler, ctx,URI);
			if(doc==null) {
				return null;
			}
			//Log.d(this.toString(),"START PARSE XML");
			
			//--------------------------------------------------------------------------------- INPUT DATA
			if (doc.getElementsByTagName("name").item(0).getChildNodes().item(0)!=null){
				//Log.d(this.toString(),"parse movies");
				//----------------------------------------------------------------------------- MOVIES DATA
				int movie_count				= doc.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue().split(BIG_SEPARATOR).length;
				
				String names[] 				= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "name",										movie_count);								
				String durations[]		 	= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "duration",									movie_count);
				String images[] 			= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "image",										movie_count);				
				String trailer_urls[] 		= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "android_trailer_url",	movie_count);
				String standard_ratings[] 	= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "standard_rating",					movie_count);
				String mids[] 				= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "mid",											movie_count);
				String genres[] 			= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "genre",										movie_count);
				

				for (int i=0;i<movie_count;i++){					
					Movie movie = new Movie();					
					long release_date=0;
					Float imdb_rating=(float) -1.0;					
					movie.create(
						i,
    					URLDecoder.decode(names[i],"UTF-8"),
    					images[i],
    					durations[i],
    					"",
    					standard_ratings[i],
    					trailer_urls[i],
    					release_date,
    					"",
    					imdb_rating,
    					mids[i],
    					genres[i]);
				}		
			}
			//Log.d(this.toString(),"FINISH PARSE XML");
			return this;
		} catch (Exception e) {			
			AenextSQALib.report_error(handler, ctx, AERR_libTickitCinemaMemcached_sync,URI, e);
			return null;
		}       
		
	}
	//############################################################################################################### cache_movie_detail
	public Movie 					cache_movie_detail(Handler handler, Context ctx, String mid){
		String URI="";
		try {
			Movie m	=	this.movie.find_by_mid(mid);			
							
			URI = AenextHTTPLib.ROOT_URI 	+ "/tickit_cinema_requests/request_movie_detail?mid="+mid;
			
			Document doc = AenextHTTPLib.http_request(handler, ctx,URI);
			if(doc==null) {
				return null;
			}
			//Log.d(this.toString(),"START PARSE XML");
			if ((doc.getElementsByTagName("response").item(0).getChildNodes().item(0)!=null)&&(m!=null)){
				//Log.d(this.toString(),"parse movies");
				if (doc.getElementsByTagName("plot").item(0).getChildNodes().item(0)!=null){
					m.movie_plot			=	URLDecoder.decode(doc.getElementsByTagName("plot").item(0).getChildNodes().item(0).getNodeValue(),"UTF-8");}
				if (doc.getElementsByTagName("cover_image_url").item(0).getChildNodes().item(0)!=null){
					m.image_url				=	doc.getElementsByTagName("cover_image_url").item(0).getChildNodes().item(0).getNodeValue();}
				if (doc.getElementsByTagName("imdb_rating").item(0).getChildNodes().item(0)!=null){
					m.movie_imdb_rating		=	Float.parseFloat(doc.getElementsByTagName("imdb_rating").item(0).getChildNodes().item(0).getNodeValue());}
				if (doc.getElementsByTagName("release_date").item(0).getChildNodes().item(0)!=null){
					m.release_date			=	Long.parseLong(doc.getElementsByTagName("release_date").item(0).getChildNodes().item(0).getNodeValue());}
			}
			//Log.d(this.toString(),"FINISH PARSE XML");
			return m;
		} catch (Exception e) {			
			AenextSQALib.report_error(handler, ctx, AERR_libTickitCinemaMemcached_sync_showtimes,URI, e);
			return null;
		}       
		
	}
	//############################################################################################################### 
	public ArrayList<Theater> 		cache_theater_detail(Handler handler, Context ctx, String mid, String location, int date_offset){
		String URI="";
		try {			
							
			URI = AenextHTTPLib.ROOT_URI 	+ "/tickit_cinema_requests/request_theaters_showtimes?mid="+mid+"&location="+URLEncoder.encode(location, "UTF-8")+"&date_offset="+date_offset;
			
			Document doc = AenextHTTPLib.http_request(handler, ctx,URI);
			if(doc==null) {
				return null;
			}
			//Log.d(this.toString(),"START PARSE XML");
			if ((doc.getElementsByTagName("response").item(0).getChildNodes().item(0)!=null)){
				// THEATERS
				int count				= doc.getElementsByTagName("theaters").item(0).getChildNodes().item(0).getNodeValue().split(BIG_SEPARATOR).length;				
				String theater_infos[] 	= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "theaters", count);																
				for (int i=0;i<count;i++){					
					Theater theater = new Theater();
					String temp="";
					if (theater_infos[i].split(SMALL_SEPARATOR).length>2) {
						temp=theater_infos[i].split(SMALL_SEPARATOR)[2];
					}
					theater.create(
							Integer.parseInt(theater_infos[i].split(SMALL_SEPARATOR)[0]), 
							URLDecoder.decode(theater_infos[i].split(SMALL_SEPARATOR)[1]), 
							temp, 
							"", 
							"", 
							0.0, 
							0.0);
				}
				//SHOWTIMES
				count						= doc.getElementsByTagName("showtimes").item(0).getChildNodes().item(0).getNodeValue().split(BIG_SEPARATOR).length;				
				String showtime_infos[] 	= AenextUtilityLib.getStringFromDoc(handler,ctx, doc, BIG_SEPARATOR, "showtimes", count);																
				
				for (int i=0;i<count;i++){					
					String temp="";
					if (showtime_infos[i].split(SMALL_SEPARATOR).length>2) {
						temp=showtime_infos[i].split(SMALL_SEPARATOR)[2];
					}
					if (showtime_infos[i].split(SMALL_SEPARATOR).length>1){
						Showtime showtime = new Showtime();
						showtime.create(
							i,
							showtime_infos[i].split(SMALL_SEPARATOR)[1], 
							temp, 							  
							Integer.parseInt(showtime_infos[i].split(SMALL_SEPARATOR)[0]));
					}
				}												
			}
			//Log.d(this.toString(),"FINISH PARSE XML");
			return theaters;
		} catch (Exception e) {			
			AenextSQALib.report_error(handler, ctx, AERR_libTickitCinemaMemcached_sync_showtimes,URI, e);
			return null;
		}       
	}
	//############################################################################################################### CLASS Location
	public class Location {
		public int id					=0;
		public double	geo_lat			=0;
		public double	geo_lng			=0;
		public String	name			="";
		public String	zipcode			="";
		public String	state			="";
		//--------------------------------------------------------------------------- Location
		public Location(){}
		//--------------------------------------------------------------------------- create
		public Location create(
				double 	str_geo_lat,
				double 	str_geo_lng,				
				String 	str_name, 
				String	str_zipcode,
				String  str_state) {			
			this.name 				= 	str_name;
			this.geo_lat 			= 	str_geo_lat;
			this.geo_lng			=	str_geo_lng;
			this.zipcode			=	str_zipcode;	
			this.state				=	str_state;
			locations.add(this);
			return this;
		}		
		//--------------------------------------------------------------------------- save
		public int save(ArrayList<Location> local_locations,Context ctx){
			SQLiteDatabase DB = ctx.openOrCreateDatabase("TickitCinema", 0,null);
			init_table(DB);
			DB.execSQL("delete from memcached_Locations;");
			for(int i=0;i<local_locations.size();i++) {
				DB.execSQL("insert into memcached_Locations (name, geo_lat, geo_lng, zipcode, state) values(\""
						+local_locations.get(i).name+"\",\""
						+local_locations.get(i).geo_lat+"\",\""
						+local_locations.get(i).geo_lng+"\",\""
						+local_locations.get(i).zipcode+"\",\""
						+local_locations.get(i).state+"\");");				
			}			
			DB.close();
			return 0;
		}
		//--------------------------------------------------------------------------- restore
		public ArrayList<Location> restore(Context ctx){
			ArrayList<Location> results=new ArrayList<Location>();
			SQLiteDatabase DB = ctx.openOrCreateDatabase("TickitCinema", 0,null);
			init_table(DB);
			String fields[] = {"id", "name", "geo_lat", "geo_lng", "zipcode", "state"}; 
			Cursor aCur = DB.query("memcached_Locations", fields, null, null, null, null, "zipcode ASC");
			if (aCur.getCount()!=0){
				aCur.moveToFirst();						
				do{
					Location local_location=new Location();
					local_location.id 			= Integer.parseInt(aCur.getString(aCur.getColumnIndex("id")));
					local_location.name 		= aCur.getString(aCur.getColumnIndex("name"));
					local_location.geo_lat 		= aCur.getDouble(aCur.getColumnIndex("geo_lat"));
					local_location.geo_lng 		= aCur.getDouble(aCur.getColumnIndex("geo_lng"));
					local_location.zipcode	 	= aCur.getString(aCur.getColumnIndex("zipcode"));
					local_location.state		= aCur.getString(aCur.getColumnIndex("state"));
					results.add(local_location);
				}
				while(aCur.moveToNext());	
				locations=results;
				aCur.close();
				DB.close();
				return results;				
			}else{
				aCur.close();
				DB.close();
				return null;
			}
		}		
		//--------------------------------------------------------------------------- init_table
		private void init_table(SQLiteDatabase DB){
			DB.execSQL("create table if not exists memcached_Locations("+
					"id integer primary key,"		+
					"name varchar(255),"	+
					"geo_lat float,"	+
					"geo_lng float,"	+
					"zipcode varchar(255),"	+
					"state varchar(255));");			
		}		
		
	}
	//############################################################################################################### CLASS Movie
	public class Movie{	
		public int 		id					=0;
		public String 	name				="";
		public String 	image_url			="";
		public String 	duration			="";
		public String 	imdb_url			="";
		public String 	standard_rating		="";
		public String 	trailer_url			="";
		public long 	release_date		=0;
		public String 	movie_plot			="";
		public float 	movie_imdb_rating	=0;
		public String	mid					="";
		public String 	genres				="";
		public Bitmap	image_bmp			=null;
		//--------------------------------------------------------------------------- Movie
		public Movie(){}
		//--------------------------------------------------------------------------- find_by_id
		public Movie find_by_id(int id){
			for(int i=0;i<movies.size();i++) {
				if (movies.get(i).id==id) {					
					return movies.get(i);
				}
			}
			return null;
		}		
		//--------------------------------------------------------------------------- find_by_mid
		public Movie find_by_mid(String mid){
			for(int i=0;i<movies.size();i++) {
				if (movies.get(i).mid.equals(mid)) {					
					return movies.get(i);
				}
			}
			return null;
		}		
		//--------------------------------------------------------------------------- create	
		public Movie create(
				int 	int_id,
				String 	str_name,
				String 	str_image_url, 
				String 	str_duration, 
				String 	str_imdb_url, 
				String 	str_standard_rating, 
				String 	str_trailer_url,
				long	int_release_date,
				String	str_movie_plot,
				float	flt_movie_imdb_rating,
				String	str_mid,
				String 	str_genres) 
		{			
			this.id					=	int_id;
			this.name 				= 	str_name;
			this.image_url 			= 	str_image_url;
			this.duration			=	str_duration;
			this.imdb_url			=	str_imdb_url;			
			this.standard_rating 	= 	str_standard_rating;
			this.trailer_url		=	str_trailer_url;
			this.release_date		=	int_release_date;
			this.movie_plot			=	str_movie_plot;
			this.movie_imdb_rating	=	flt_movie_imdb_rating;		
			this.mid				=	str_mid;
			this.genres				=	str_genres;
			movies.add(this);
			return this;
		}		
		//--------------------------------------------------------------------------- save_bundle
		public int save_bundle(ArrayList<Movie> local_movies,Bundle bundle){
			String[] 	ids					=new String[local_movies.size()];
			String[] 	names				=new String[local_movies.size()];
			String[] 	image_urls			=new String[local_movies.size()];
			String[] 	durations			=new String[local_movies.size()];
			String[] 	imdb_urls			=new String[local_movies.size()];
			String[] 	standard_ratings	=new String[local_movies.size()];
			String[] 	trailer_urls		=new String[local_movies.size()];
			long[] 		release_dates		=new long[local_movies.size()];
			String[] 	movie_plots			=new String[local_movies.size()];
			float[] 	movie_imdb_ratings	=new float[local_movies.size()];
			String[]	mids				=new String[local_movies.size()];
			String[]	genres				=new String[local_movies.size()];
			
			for(int i=0;i<local_movies.size();i++) {
				ids[i]		=	Integer.toString(local_movies.get(i).id);
				names[i]	=	local_movies.get(i).name;
				image_urls[i]			=local_movies.get(i).image_url;
				durations[i]			=local_movies.get(i).duration;
				imdb_urls[i]			=local_movies.get(i).imdb_url;
				standard_ratings[i]		=local_movies.get(i).standard_rating;
				trailer_urls[i]			=local_movies.get(i).trailer_url;
				release_dates[i]		=local_movies.get(i).release_date;
				movie_plots[i]			=local_movies.get(i).movie_plot;
				movie_imdb_ratings[i]	=local_movies.get(i).movie_imdb_rating;
				mids[i]					=local_movies.get(i).mid;
				genres[i]				=local_movies.get(i).genres;
			}						
			bundle.putStringArray("Movie_ids"				,ids);
			bundle.putStringArray("Movie_names"				,names);
			bundle.putStringArray("Movie_image_urls"		,image_urls);
			bundle.putStringArray("Movie_durations"			,durations);
			bundle.putStringArray("Movie_imdb_urls"			,imdb_urls);
			bundle.putStringArray("Movie_standard_ratings"	,standard_ratings);
			bundle.putStringArray("Movie_trailer_urls"		,trailer_urls);
			bundle.putLongArray("Movie_release_dates"		,release_dates);
			bundle.putStringArray("Movie_movie_plots"		,movie_plots);
			bundle.putFloatArray("Movie_imdb_ratings"		,movie_imdb_ratings);
			bundle.putStringArray("Movie_mids"				,mids);
			bundle.putStringArray("Movie_genres"			,genres);
			
			return 0;
		}
		//--------------------------------------------------------------------------- restore_bundle		
		public ArrayList<Movie> restore_bundle(Bundle bundle){
			ArrayList<Movie> results=new ArrayList<Movie>();			
			String[] 	ids					=bundle.getStringArray("Movie_ids");
			String[] 	names				=bundle.getStringArray("Movie_names");
			String[] 	image_urls			=bundle.getStringArray("Movie_image_urls");
			String[] 	durations			=bundle.getStringArray("Movie_durations");
			String[] 	imdb_urls			=bundle.getStringArray("Movie_imdb_urls");
			String[] 	standard_ratings	=bundle.getStringArray("Movie_standard_ratings");
			String[] 	trailer_urls		=bundle.getStringArray("Movie_trailer_urls");
			long[] 		release_dates		=bundle.getLongArray("Movie_release_dates");
			String[] 	movie_plots			=bundle.getStringArray("Movie_movie_plots");
			float[] 	movie_imdb_ratings	=bundle.getFloatArray("Movie_imdb_ratings");
			String[]	mids				=bundle.getStringArray("Movie_mids");
			String[]	genres				=bundle.getStringArray("Movie_genres");
			
			
			for(int i=0;i<ids.length;i++) {
					Movie local_movie=new Movie();
					local_movie.id 					= Integer.parseInt(ids[i]);					
					local_movie.name 				= names[i];
					local_movie.image_url 			= image_urls[i];
					local_movie.duration 			= durations[i];
					local_movie.imdb_url	 		= imdb_urls[i];
					local_movie.standard_rating 	= standard_ratings[i];
					local_movie.trailer_url 		= trailer_urls[i];
					local_movie.release_date		= release_dates[i];
					local_movie.movie_plot			= movie_plots[i];
					local_movie.movie_imdb_rating 	= movie_imdb_ratings[i];
					local_movie.mid					= mids[i];
					local_movie.genres				= genres[i];
					results.add(local_movie);
			}
			movies=results;
			return results;
		}		
		//--------------------------------------------------------------------------- save
		public int save(ArrayList<Movie> local_movies,Context ctx){
			SQLiteDatabase DB = ctx.openOrCreateDatabase("TickitCinema", 0,null);
			init_table(DB);
			DB.execSQL("delete from memcached_Movies;");
			for(int i=0;i<local_movies.size();i++) {
				DB.execSQL("insert into memcached_Movies (id, name, image_url, duration, imdb_url, standard_rating, trailer_url, release_date, movie_plot, movie_imdb_rating) values(\""
						+local_movies.get(i).id+"\",\""
						+local_movies.get(i).name+"\",\""
						+local_movies.get(i).image_url+"\",\""
						+local_movies.get(i).duration+"\",\""
						+local_movies.get(i).imdb_url+"\",\""
						+local_movies.get(i).standard_rating+"\",\""
						+local_movies.get(i).trailer_url+"\",\""
						+local_movies.get(i).release_date+"\",\""
						+local_movies.get(i).movie_plot+"\",\""
						+local_movies.get(i).movie_imdb_rating+"\",\""
						+local_movies.get(i).mid+"\",\""
						+local_movies.get(i).genres+"\");");				
			}			
			DB.close();
			return 0;
		}
		//--------------------------------------------------------------------------- restore
		public ArrayList<Movie> restore(Context ctx){
			ArrayList<Movie> results=new ArrayList<Movie>();
			SQLiteDatabase DB = ctx.openOrCreateDatabase("TickitCinema", 0,null);
			init_table(DB);
			String fields[] = {"id", "name", "image_url", "duration", "imdb_url", "standard_rating",
							   "trailer_url", "release_date", "movie_plot", "movie_imdb_rating"}; 
			Cursor aCur = DB.query("memcached_Movies", fields, null, null, null, null, null);
			if (aCur.getCount()!=0){
				aCur.moveToFirst();						
				do{
					Movie local_movie=new Movie();
					local_movie.id 			= Integer.parseInt(aCur.getString(aCur.getColumnIndex("id")));
					local_movie.name 		= aCur.getString(aCur.getColumnIndex("name"));
					local_movie.image_url 	= aCur.getString(aCur.getColumnIndex("image_url"));
					local_movie.duration 	= aCur.getString(aCur.getColumnIndex("duration"));
					local_movie.imdb_url	 		= aCur.getString(aCur.getColumnIndex("imdb_url"));
					local_movie.standard_rating 	= aCur.getString(aCur.getColumnIndex("standard_rating"));
					local_movie.trailer_url 		= aCur.getString(aCur.getColumnIndex("trailer_url"));
					local_movie.release_date		= aCur.getInt(aCur.getColumnIndex("release_date"));
					local_movie.movie_plot			= aCur.getString(aCur.getColumnIndex("movie_plot"));
					local_movie.movie_imdb_rating 	= aCur.getFloat(aCur.getColumnIndex("movie_imdb_rating"));
					local_movie.mid					= aCur.getString(aCur.getColumnIndex("mid"));
					local_movie.genres				= aCur.getString(aCur.getColumnIndex("genres"));
					results.add(local_movie);
				}
				while(aCur.moveToNext());	
				movies=results;
				aCur.close();
				DB.close();
				return results;				
			}else{
				aCur.close();
				DB.close();
				return null;
			}
		}
		//--------------------------------------------------------------------------- sort_by_imdb_rated_desc 
		public ArrayList<Movie> sort_by_imdb_rated_desc(){
			ArrayList<Movie> results=new ArrayList<Movie>();			
			while(movies.size()>0){
				float max=(float)-1;
				int max_position=-1;
				for(int i=0;i<movies.size();i++){
					if (movies.get(i).movie_imdb_rating>=max){						
						max=movies.get(i).movie_imdb_rating;
						max_position=i;
					}
				}
				results.add(movies.get(max_position));
				movies.remove(max_position);
			}
			movies=results;
			return results;
		}
		//--------------------------------------------------------------------------- sort_by_release_date_desc
		public ArrayList<Movie> sort_by_release_date_desc(){
			ArrayList<Movie> results=new ArrayList<Movie>();			
			while(movies.size()>0){				
				long max=-999999999;
				int max_position=-1;
				for(int i=0;i<movies.size();i++){
					if (movies.get(i).release_date>=max){						
						max=movies.get(i).release_date;
						max_position=i;
					}
				}
				results.add(movies.get(max_position));
				movies.remove(max_position);
			}
			movies=results;
			return results;
		}

		//--------------------------------------------------------------------------- init_table
		private void init_table(SQLiteDatabase DB){
			DB.execSQL("create table if not exists memcached_Movies("+
					"id integer primary key,"		+
					"name varchar(255),"	+
					"image_url varchar(255),"	+
					"duration varchar(255),"	+
					"imdb_url varchar(255),"	+
					"standard_rating varchar(255)," +
					"trailer_url varchar(255)," +
					"release_date varchar(255)," +
					"movie_plot varchar(255)," +
					"movie_imdb_rating varchar(255)," +
					"mid varchar(255),"+
					"genre varchar(255);");			
		}		
		//---------------------------------------------------------------------------		
	}
	//############################################################################################################### CLASS Theater
	public class Theater{	
		public int 		id		=0;
		public String 	name	="";
		public String 	address	="";
		public String 	map_url	="";
		public String 	phone	="";
		public Double	geo_lat	=0.0;
		public Double	geo_lng	=0.0;
		public double   distance =0;
		//--------------------------------------------------------------------------- Theater
		public Theater(){}
		//--------------------------------------------------------------------------- create
		public Theater create(
				int 	int_id,
				String 	str_name,
				String 	str_address, 
				String 	str_map_url,
				String 	str_phone,
				double	geo_lat,
				double	geo_lng)
		{
			this.id					=	int_id;
			this.name 				= 	str_name;
			this.address 			= 	str_address;
			this.map_url			=	str_map_url;
			this.phone				=	str_phone;
			this.geo_lat			=	geo_lat;
			this.geo_lng			=	geo_lng;
			theaters.add(this);
			return this;
		}		
		//--------------------------------------------------------------------------- showtimes
		public ArrayList<Showtime> showtimes(int theater_id){
			ArrayList<Showtime> results=new ArrayList<Showtime>();
			for(int i=0;i<showtimes.size();i++){								
				if (showtimes.get(i).theater_id==theater_id){
						results.add(showtimes.get(i));
				}
			}
			return results;
		}
		
		//--------------------------------------------------------------------------- find_by_id
		public Theater find_by_id(int id){
			for(int i=0;i<theaters.size();i++) {
				if (theaters.get(i).id==id) {					
					return theaters.get(i);
				}
			}
			return null;
		}
		//--------------------------------------------------------------------------- sort_by_distance_asc 
		public ArrayList<Theater> sort_by_distance_asc(){
			ArrayList<Theater> results=new ArrayList<Theater>();
			
			while(theaters.size()>0){
				double min=9999999;
				int min_position=-1;
				for(int i=0;i<theaters.size();i++){
					if (theaters.get(i).distance<min){
						min=theaters.get(i).distance;
						min_position=i;
					}
				}
				results.add(theaters.get(min_position));
				theaters.remove(min_position);
			}
			theaters=results;
			return results;
		}
		//--------------------------------------------------------------------------- save_bundle
		public int save_bundle(ArrayList<Theater> local_theaters,Bundle bundle){
			String[] ids				=new String[local_theaters.size()];
			String[] names				=new String[local_theaters.size()];
			String[] address			=new String[local_theaters.size()];
			String[] map_urls			=new String[local_theaters.size()];
			String[] phones				=new String[local_theaters.size()];
			double[] geo_lats			=new double[local_theaters.size()];
			double[] geo_lngs			=new double[local_theaters.size()];
			
			for(int i=0;i<local_theaters.size();i++) {
				ids[i]		=	Integer.toString(local_theaters.get(i).id);
				names[i]	=	local_theaters.get(i).name;
				address[i]	=	local_theaters.get(i).address;
				map_urls[i]	=	local_theaters.get(i).map_url;
				phones[i]	=	local_theaters.get(i).phone;
				geo_lats[i]	=	local_theaters.get(i).geo_lat;
				geo_lngs[i]	=	local_theaters.get(i).geo_lng;
			}						
			bundle.putStringArray("Theater_ids"				,ids);
			bundle.putStringArray("Theater_names"			,names);
			bundle.putStringArray("Theater_address"			,address);
			bundle.putStringArray("Theater_map_urls"		,map_urls);
			bundle.putStringArray("Theater_phones"			,phones);
			bundle.putDoubleArray("Theater_geo_lats"		,geo_lats);
			bundle.putDoubleArray("Theater_geo_lngs"		,geo_lngs);
			return 0;
		}
		//--------------------------------------------------------------------------- restore_bundle		
		public ArrayList<Theater> restore_bundle(Bundle bundle){
			ArrayList<Theater> results=new ArrayList<Theater>();
			if (bundle!=null)
			{
				String[] ids				=bundle.getStringArray("Theater_ids");
				String[] names				=bundle.getStringArray("Theater_names");
				String[] address			=bundle.getStringArray("Theater_address");
				String[] map_urls			=bundle.getStringArray("Theater_map_urls");
				String[] phones				=bundle.getStringArray("Theater_phones");
				double[] geo_lat			=bundle.getDoubleArray("Theater_geo_lats");
				double[] geo_lng			=bundle.getDoubleArray("Theater_geo_lngs");
			
				for(int i=0;i<ids.length;i++) {
					Theater local_theater	= new Theater();
					local_theater.id 		= Integer.parseInt(ids[i]);
					local_theater.name 		= names[i];
					local_theater.address 	= address[i];
					local_theater.map_url 	= map_urls[i];
					local_theater.phone		= phones[i];
					local_theater.geo_lat	= geo_lat[i];
					local_theater.geo_lng	= geo_lng[i];
					results.add(local_theater);
				}
			}
			theaters=results;
			return results;
		}				
		//--------------------------------------------------------------------------- save
		public int save(ArrayList<Theater> local_theaters,Context ctx){
			SQLiteDatabase DB = ctx.openOrCreateDatabase("TickitCinema", 0,null);
			init_table(DB);
			DB.execSQL("delete from memcached_Theaters;");
			for(int i=0;i<local_theaters.size();i++) {
				DB.execSQL("insert into memcached_Theaters (id, name, address ,map_url, geo_lat, geo_lng, phone) values(\""
						+local_theaters.get(i).id+"\",\""
						+local_theaters.get(i).name+"\",\""
						+local_theaters.get(i).address+"\",\""
						+local_theaters.get(i).map_url+"\",\""
						+local_theaters.get(i).geo_lat+"\",\""
						+local_theaters.get(i).geo_lng+"\",\""
						+local_theaters.get(i).phone+"\");");				
			}			
			DB.close();
			return 0;
		}
		//--------------------------------------------------------------------------- restore
		public ArrayList<Theater> restore(Context ctx){
			ArrayList<Theater> results=new ArrayList<Theater>();
			SQLiteDatabase DB = ctx.openOrCreateDatabase("TickitCinema", 0,null);
			init_table(DB);
			String fields[] = {"id","name","address","map_url","phone","geo_lat","geo_lng"}; 
			Cursor aCur = DB.query("memcached_Theaters", fields, null, null, null, null, null);
			if (aCur.getCount()!=0){
				aCur.moveToFirst();						
				do{
					Theater local_theater=new Theater();
					local_theater.id 		= Integer.parseInt(aCur.getString(aCur.getColumnIndex("id")));
					local_theater.name 		= aCur.getString(aCur.getColumnIndex("name"));
					local_theater.address 	= aCur.getString(aCur.getColumnIndex("address"));
					local_theater.map_url 	= aCur.getString(aCur.getColumnIndex("map_url"));
					local_theater.phone	 	= aCur.getString(aCur.getColumnIndex("phone"));
					local_theater.geo_lat	= aCur.getDouble(aCur.getColumnIndex("geo_lat"));
					local_theater.geo_lng	= aCur.getDouble(aCur.getColumnIndex("geo_lng"));
					results.add(local_theater);
				}
				while(aCur.moveToNext());	
				theaters=results;
				aCur.close();
				DB.close();
				return results;				
			}else{
				aCur.close();
				DB.close();
				return null;
			}
		}
		//--------------------------------------------------------------------------- init_table
		private void init_table(SQLiteDatabase DB){
			DB.execSQL("create table if not exists memcached_Theaters("+
					"id integer primary key,"		+
					"name varchar(255),"	+
					"address varchar(255),"	+
					"map_url varchar(255),"	+
					"geo_lat real,"	+
					"geo_lng real,"	+
					"phone varchar(255));");		
		}		
		//---------------------------------------------------------------------------
	}
	//############################################################################################################### CLASS Showtime
	public class Showtime{	
		public int		id				=0;
		public String 	time			="";
		public String 	url				="";
		public int 		theater_id		=0;
		//--------------------------------------------------------------------------- Showtime
		public Showtime(){}
		//--------------------------------------------------------------------------- create
		public Showtime create(int id,String str_time,String str_url, int int_theater_id){
			this.id				=	id;
			this.time			=	str_time;			
			this.url			=	str_url;
			this.theater_id		=	int_theater_id;
			showtimes.add(this);
			return this;
		}
		//--------------------------------------------------------------------------- find_by_theater_id
		public ArrayList<Showtime> find_by_theater_id(int theater_id){
			ArrayList<Showtime> results=new ArrayList<Showtime>();		
			for(int i=0;i<showtimes.size();i++) {
				if (showtimes.get(i).theater_id==theater_id){
					results.add(showtimes.get(i));
				}
			}
			return results;
		}
		//--------------------------------------------------------------------------- save_bundle
		public int save_bundle(ArrayList<Showtime> local_showtimes,Bundle bundle){
			int[] 	 ids			=new int[local_showtimes.size()];
			String[] times			=new String[local_showtimes.size()];
			String[] theater_ids	=new String[local_showtimes.size()];
			String[] urls			=new String[local_showtimes.size()];
			
			for(int i=0;i<local_showtimes.size();i++) {
				ids[i]			=	local_showtimes.get(i).id;
				urls[i]			=	local_showtimes.get(i).url;
				theater_ids[i]	=	Integer.toString(local_showtimes.get(i).theater_id);				
				times[i]		=	local_showtimes.get(i).time;
			}						
			bundle.putIntArray		("Showtime_ids"			,ids);
			bundle.putStringArray	("Showtime_times"		,times);
			bundle.putStringArray	("Showtime_theater_ids"	,theater_ids);
			bundle.putStringArray	("Showtime_urls"		,urls);
						
			return 0;
		}
		//--------------------------------------------------------------------------- restore_bundle		
		public ArrayList<Showtime> restore_bundle(Bundle bundle){
			ArrayList<Showtime> results=new ArrayList<Showtime>();
			if (bundle!=null){
				int[] ids				=bundle.getIntArray		("Showtime_ids");
				String[] times			=bundle.getStringArray	("Showtime_times");				
				int[] theater_ids		=bundle.getIntArray		("Showtime_theater_ids");
				String[] urls			=bundle.getStringArray	("Showtime_urls");
				
				for(int i=0;i<ids.length;i++) {
						Showtime local_showtime		= new Showtime();
						local_showtime.id 			= ids[i];
						local_showtime.time		 	= times[i];
						local_showtime.theater_id 	= theater_ids[i];
						local_showtime.url			= urls[i];						
						results.add(local_showtime);
				}				
			}
			showtimes=results;
			return results;
		}				
		//--------------------------------------------------------------------------- save		
		public int save(ArrayList<Showtime> local_showtimes,Context ctx){
			SQLiteDatabase DB = ctx.openOrCreateDatabase("TickitCinema", 0,null);
			init_table(DB);
			DB.execSQL("delete from memcached_Showtimes;");
			for(int i=0;i<local_showtimes.size();i++) {
				DB.execSQL("insert into memcached_Showtimes (id, time, theater_id, url) values(\""
						+local_showtimes.get(i).id+"\",\""
						+local_showtimes.get(i).time+"\",\""
						+local_showtimes.get(i).theater_id+"\",\""						
						+local_showtimes.get(i).url+"\");");				
			}			
			DB.close();
			return 0;
		}
		//--------------------------------------------------------------------------- restore
		public ArrayList<Showtime> restore(Context ctx){
			ArrayList<Showtime> results=new ArrayList<Showtime>();
			SQLiteDatabase DB = ctx.openOrCreateDatabase("TickitCinema", 0,null);
			init_table(DB);
			String fields[] = {"unique_stamp","movie_id","theater_id"}; 
			Cursor aCur = DB.query("memcached_Showtimes", fields, null, null, null, null, null);
			if (aCur.getCount()!=0){
				aCur.moveToFirst();						
				do{
					Showtime local_showtime=new Showtime();
					local_showtime.id 			= aCur.getInt(aCur.getColumnIndex("id"));
					local_showtime.time 		= aCur.getString(aCur.getColumnIndex("unique_stamp"));
					local_showtime.theater_id 	= aCur.getInt(aCur.getColumnIndex("theater_id"));
					local_showtime.url 			= aCur.getString(aCur.getColumnIndex("url"));					
					results.add(local_showtime);
				}
				while(aCur.moveToNext());	
				showtimes=results;
				aCur.close();
				DB.close();
				return results;				
			}else{
				aCur.close();
				DB.close();
				return null;
			}
		}
		//--------------------------------------------------------------------------- init_table
		private void init_table(SQLiteDatabase DB){
			DB.execSQL("create table if not exists memcached_Showtimes("+
					"id integer" +
					"time varchar(255),"	+
					"theater_id integer,"	+
					"url varchar(255));");			
		}				
		//---------------------------------------------------------------------------
	}

}
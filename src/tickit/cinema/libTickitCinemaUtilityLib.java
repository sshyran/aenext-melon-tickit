package tickit.cinema;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import android.content.Context;
import android.location.Geocoder;
import android.location.Address;
import android.os.Handler;
import android.os.Message;
//import android.util.Log;

public class libTickitCinemaUtilityLib 
{
	public static final int ERR_TickitCinemaUtilityLib_FailToDetermineLocation		=10000+1000+0;//app#10000, activity#0, error#0
	public static final int ERR_TickitCinemaUtilityLib_FailToSync					=10000+1000+1;
	public static final int ERR_TickitCinemaUtilityLib_request_movie_update			=10000+1000+2;
	public static final int ERR_TickitCinemaUtilityLib_build_showtime_buttons		=10000+1000+3;
	public static final int ERR_TickitCinemaUtilityLib_mark_showtimes				=10000+1000+4;
	
	//###################################################################################################################### request_movie_update
	public static libTickitCinemaMemcached request_movie_update(Handler handler,Context ctx,String location, Double lat,Double lng)	{
		try {
			libTickitCinemaMemcached memDB=new libTickitCinemaMemcached();
			List<Address> address;
			Geocoder loc = new Geocoder(ctx);		
			//memDB.restore(ctx);
			memDB.location.restore(ctx);		
			memDB.current_day=Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			memDB.current_month=Calendar.getInstance().get(Calendar.MONTH);
			memDB.current_year=Calendar.getInstance().get(Calendar.YEAR);

			if (location==null){
				try {
					address=loc.getFromLocation(lat, lng, 1);
					memDB.geo_lat=lat;
					memDB.geo_lng=lng;
					memDB.current_location=address.get(0).getAddressLine(address.get(0).getMaxAddressLineIndex()-1);
				} catch(IOException e){
					Message m = new Message();		
					m.what = ERR_TickitCinemaUtilityLib_FailToDetermineLocation;    	
					handler.sendMessage(m);
					return null;
				}
			}else{
				try {
					address=loc.getFromLocationName(location, 1);
					memDB.geo_lat=address.get(0).getLatitude();
					memDB.geo_lng=address.get(0).getLongitude();		
					memDB.current_location=address.get(0).getAddressLine(address.get(0).getMaxAddressLineIndex()-1);				
				} catch(IOException e){
					memDB.geo_lat=0.0;
					memDB.geo_lng=0.0;
					memDB.current_location=location;
					memDB.cache_movie_list(null, ctx, location, 0);	
					for(int i=0;i<memDB.locations.size();i++){
						if(memDB.locations.get(i).zipcode.equals(location)){
							return memDB;
						}
					}
					memDB.location.create(0, 0, null, location, null);
					memDB.location.save(memDB.locations, ctx);					
					return memDB;
				}
			}
			location="";
			if(address.get(0).getPostalCode()!=null){
				location=location + address.get(0).getPostalCode();	
			}
			if(address.get(0).getLocality()!=null){
				location=location + "," + address.get(0).getLocality();
			}
			if(address.get(0).getAdminArea()!=null){
				location=location + "," + address.get(0).getAdminArea();
			}
			if(address.get(0).getCountryName()!=null){
				location=location + "," + address.get(0).getCountryName();
			}
			if(location.equals("")){
				Message m = new Message();		
				m.what = ERR_TickitCinemaUtilityLib_FailToDetermineLocation;    	
				handler.sendMessage(m);			
				return null;
			}		
		//	Sync data
			if (memDB.cache_movie_list(handler, ctx, location, 0)!=null){		
				//Sync success
				for(int i=0;i<memDB.locations.size();i++){
					if(memDB.locations.get(i).zipcode.equals(address.get(0).getAddressLine(address.get(0).getMaxAddressLineIndex()-1))){
						return memDB;
					}
				}			
				memDB.location.create(0, 0, null, address.get(0).getAddressLine(address.get(0).getMaxAddressLineIndex()-1), null);
				memDB.location.save(memDB.locations, ctx);
				return memDB;
			}else{
				//Sync failed			
				return null;
			}
		}catch(Exception e){
			AenextSQALib.report_error(handler, ctx, ERR_TickitCinemaUtilityLib_request_movie_update,location, e);
			return null;
		}
	}
    
    //###################################################################################################################### END
}



package tickit.cinema;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import org.w3c.dom.Document;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
//import android.util.Log;

public class AenextUtilityLib {
	//###################################################################################################################### GLOBAL VARIABLES
	public static String FILE_CACHED_PATH="/data/data/tickit.cinema/files/";
	//###################################################################################################################### BuildDayList
	public static String[] BuildDayList(Handler handler,Context ctx,int day_num){
		try{
			Calendar 	datetime = Calendar.getInstance();
			DateFormat 	fmtDateAndTime=DateFormat.getDateTimeInstance(); 
			String[] 	str_dates=new String[day_num];
			String temp;
        			        
			for(int i = 0;i<day_num;i++){
				temp=fmtDateAndTime.format(datetime.getTime());
				if (i==0){
					str_dates[i]="TODAY - " + temp.substring(0, temp.length()-11);
				}else if(i==1){
					str_dates[i]="TOMORROW - " + temp.substring(0, temp.length()-11);
				}else{
					str_dates[i]=AenextUtilityLib.NumToDay(datetime.get(Calendar.DAY_OF_WEEK)).toUpperCase()
					+" - "+ temp.substring(0, temp.length()-11);
				}                    
				datetime.setTimeInMillis(datetime.getTimeInMillis()+86400000);        	
			}        	
			return str_dates;
		}catch(Exception e){
			AenextSQALib.report_error(handler, ctx, AenextSQALib.AERR_AenextUtilityLib_BuildDayList,"", e);
			return null;
		}
	}
	//###################################################################################################################### NumToDay
	public static String NumToDay(int num){
		switch (num){
			case 1: return "Sunday";
			case 2: return "Monday";
			case 3: return "Tuesday";
			case 4: return "Wednesday";
			case 5: return "Thursday";
			case 6: return "Friday";
			case 7: return "Saturday";		
			default: return null;
		}
	}
	//###################################################################################################################### getHTTPCachedImage
	/*public static Bitmap getHTTPCachedImage(Handler handler, Context ctx, String image_url)
	{		
		try {
			BitmapDrawable bmp_image;
			if ((image_url!="")&&(image_url!=null)&&!(image_url.equals("null"))&&(image_url.length()!=0)){
				//Log.d("Utility","Get image from local:"+FILE_CACHED_PATH + URLEncoder.encode(image_url, "UTF-8")+".png");
				bmp_image = (BitmapDrawable) BitmapDrawable.createFromPath(FILE_CACHED_PATH + URLEncoder.encode(image_url, "UTF-8")+".png");
				return bmp_image.getBitmap();
			}else{
				return BitmapFactory.decodeResource(ctx.getResources(), R.drawable.no_image);
			}
		} catch (Exception e) {
			AenextSQALib.report_error(handler, ctx, AenextSQALib.AERR_AenextUtilityLib_getHTTPCachedImage,image_url, e);
			return null;
		}				
		
	}	*/
	//###################################################################################################################### requestHTTPImageWithCached
	public static Bitmap requestHTTPImageWithCached(Handler handler, Context ctx, String image_url){
        try {
        	if ((image_url!="")&&(image_url!=null)&&!(image_url.equals("null"))&&(image_url.length()!=0)){
        		Bitmap bmp_data;        		
        		//Check if it is local     
        		BitmapDrawable bmp_data_drawable = (BitmapDrawable) BitmapDrawable.createFromPath(FILE_CACHED_PATH + URLEncoder.encode(image_url, "UTF-8")+".png");        		
        		
        		if (bmp_data_drawable==null){
        			//Request data from cloud
        			//Log.d("Utility","Get image from cloud:"+image_url);
        			Object raw_data = null;            	        			
        			URL link = new URL(image_url);						
        			raw_data = link.getContent();
        			InputStream stream = (InputStream) raw_data;	                				
        			bmp_data = BitmapFactory.decodeStream(stream);
    	        
        			//Output it to file system
        			FileOutputStream fOut = ctx.openFileOutput(URLEncoder.encode(image_url, "UTF-8")+".png", Context.MODE_PRIVATE);
        			bmp_data.compress(Bitmap.CompressFormat.PNG, 50, fOut);
        			fOut.flush();
        			fOut.close();
        		}else{
        			//Log.d("Utility","Get image from local:"+FILE_CACHED_PATH + URLEncoder.encode(image_url, "UTF-8")+".png");
        			bmp_data=bmp_data_drawable.getBitmap();
        		}
    			return bmp_data;					        		
        	}
        	return BitmapFactory.decodeResource(ctx.getResources(), R.drawable.no_image);
		} catch (Exception e) {
			AenextSQALib.report_error(handler, ctx, AenextSQALib.AERR_AenextUtilityLib_requestHTTPImageWithCached,image_url, e);
			return null;
		}
	}
	//###################################################################################################################### getStringFromDoc
	public static String[] getStringFromDoc(Handler handler,Context ctx, Document doc,String separator, String tag_name,int array_size)	{
		try {
			String[] tmp	=doc.getElementsByTagName(tag_name).item(0).getChildNodes().item(0).getNodeValue().split(separator);
			String[] data 	= new String[array_size];
			System.arraycopy(tmp, 0, data, 0, tmp.length);
			return data;
		}catch(Exception e){
			AenextSQALib.report_error(handler, ctx, AenextSQALib.AERR_AenextUtilityLib_getStringFromDoc,"", e);
			return null;
		}
	}
	//###################################################################################################################### format_double
	public static String format_double(Handler handler, Context ctx,double d,String strFormat){  
		try {
			NumberFormat formatter = new DecimalFormat (strFormat) ; 
			String s = formatter.format ( d ) ; 
			return s; 
		}catch(Exception e){
			AenextSQALib.report_error(handler, ctx, AenextSQALib.AERR_AenextUtilityLib_format_double,"", e);
			return null;
		}
	} 
	//###################################################################################################################### playable_image_marker
	public static Bitmap playable_image_marker(Handler handler, Context ctx,Bitmap source_image,String trailer_url){
		Bitmap CopySourceImage=source_image.copy(Bitmap.Config.ARGB_8888, true);		
		if ((trailer_url!=null)&&!trailer_url.equals("")){
			float x = source_image.getWidth();
			float y = source_image.getHeight();
			Canvas c = new Canvas(CopySourceImage);
			Paint p = new Paint();        
			p.setAntiAlias(true);
			//p.setAlpha(235);              
			c.drawBitmap(BitmapFactory.decodeResource(ctx.getResources(),android.R.drawable.ic_media_play), x-32, y-32, p);
		}
		return CopySourceImage;
	}
	
	//###################################################################################################################### END
}


